package org.zeith.multipart.api;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;
import org.zeith.multipart.api.placement.*;
import org.zeith.multipart.client.IClientPartDefinitionExtensions;
import org.zeith.multipart.client.rendering.IPartRenderer;
import org.zeith.multipart.tile.BlockMultipartContainer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class PartContainer
{
	public static final Logger LOG = LogManager.getLogger(PartContainer.class);
	public static final ModelProperty<PartContainer> CONTAINER_PROP = new ModelProperty<>();
	public static final ModelProperty<Long> PART_HASH = new ModelProperty<>();
	protected final LinkedHashMap<PartPlacement, PartEntity> parts = new LinkedHashMap<>();
	
	@ApiStatus.Internal
	public final LinkedHashMap<PartPlacement, Object> renderers = new LinkedHashMap<>();
	
	public final int[] weakRedstoneSignals = new int[6];
	public final int[] strongRedstoneSignals = new int[6];
	
	public final BlockPos pos;
	
	public Level level;
	
	public int lightLevel;
	
	public boolean needsSync;
	public boolean causeBlockUpdate;
	public boolean causeRedstoneUpdate;
	public boolean waterlogged;
	
	public final Consumer<Player> openUI;
	
	public PartContainer(BlockPos pos, Consumer<Player> openUI)
	{
		this.pos = pos;
		this.openUI = openUI;
	}
	
	public boolean tryPlacePart(PartDefinition def, IConfiguredPartPlacer placer, PartPlacement placement)
	{
		if(!placement.canBePlacedAlongside(parts.keySet()))
			return false; // Unable to place here due to other parts blocking it.
		if(!def.canPlaceAt(this, placement)) return false; // Unable to place part due to internal checks - reject.
		
		var existingPart = getPartAt(placement);
		
		PartEntity placeEntity;
		if(existingPart != null)
		{
			var opt = def.tryMergeWith(this, placement, existingPart);
			if(opt.isEmpty()) return false; // unable to merge - reject.
			placeEntity = opt.orElseThrow();
		} else
			placeEntity = placer != null ? placer.create(this, placement) : def.createEntity(this, placement);
		if(placeEntity == null) return false; // somehow part was not created - reject.
		
		setPartAt(placement, placeEntity, true);
		placeEntity.onPlaced();
		
		return true;
	}
	
	private PartPlacement[] tintToPlacement = new PartPlacement[0];
	
	public int getColorForTintLayer(int tintLayer)
	{
		if(tintLayer < 0 || tintLayer >= tintToPlacement.length)
			return 0xFFFFFF;
		var part = getPartAt(tintToPlacement[tintLayer]);
		return part != null ? part.getTintLayerColor(tintLayer) : 0xFFFFFF;
	}
	
	protected long lastRecalcHash = -1L;
	
	public void recalcTintLayers(long hash)
	{
		if(hash == lastRecalcHash) return;
		
		while(true)
		{
			try
			{
				int layerCount = parts.values().stream().mapToInt(e -> e.tintIndices.length).sum();
				tintToPlacement = new PartPlacement[layerCount];
				
				int i = 0;
				for(var e : parts.entrySet())
				{
					var pe = e.getValue();
					var ti = pe.tintIndices;
					for(int j = 0; j < ti.length; j++)
					{
						int layer = i++;
						ti[j] = layer;
						tintToPlacement[layer] = e.getKey();
					}
				}
				
				break;
			} catch(ConcurrentModificationException e)
			{
				// Since the game is kinda async, and we call this method from a worker thread (when building chunk FBOs),
				// we must ensure this does never throws an error.
				// Thus, if a change was performed on the game thread, we should try again immediately, instead of causing game crash.
				continue;
			}
		}
		
		lastRecalcHash = hash;
	}
	
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return parts.values().stream().map(pe -> pe.getCapability(cap, side))
				.filter(LazyOptional::isPresent)
				.findFirst()
				.orElseGet(LazyOptional::empty);
	}
	
	public void setPartAt(PartPlacement placement, PartEntity part, boolean shouldUpdate)
	{
		parts.put(placement, part);
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			renderers.put(placement, IClientPartDefinitionExtensions.of(part).createRenderer(part));
		});
		
		if(shouldUpdate)
		{
			needsSync = true;
			causeBlockUpdate = true;
		}
	}
	
	@Nullable
	public PartEntity getPartAt(PartPlacement placement)
	{
		return parts.get(placement);
	}
	
	public void tickServer()
	{
		lightLevel = 0;
		
		boolean updateShape = causeBlockUpdate;
		for(var part : parts.values())
		{
			part.tickServer();
			if(part.isShapeDirty())
				updateShape = true;
			lightLevel = part.getLightEmission();
			if(part.syncDirty())
			{
				needsSync = true;
				part.markSynced();
			}
		}
		
		while(!toRemove.isEmpty())
		{
			var rem = toRemove.remove(0);
			var part = parts.remove(rem.placement);
			if(part != null)
			{
				renderers.remove(rem.placement);
				part.onRemoved(null, rem.drops(), rem.sound(), rem.particles());
				causeBlockUpdate = true;
				updateShape = true;
			}
		}
		
		tickShape(updateShape);
	}
	
	public void tickClient()
	{
		lightLevel = 0;
		
		boolean updateShape = causeBlockUpdate;
		for(var part : parts.values())
		{
			part.tickClient();
			if(part.isShapeDirty())
				updateShape = true;
			lightLevel = part.getLightEmission();
		}
		
		while(!toRemove.isEmpty())
		{
			var rem = toRemove.remove(0);
			var part = parts.remove(rem.placement);
			if(part != null)
			{
				part.onRemoved(null, rem.drops(), rem.sound(), rem.particles());
				causeBlockUpdate = true;
				updateShape = true;
			}
		}
		
		tickShape(updateShape);
		needsSync = false;
	}
	
	protected void tickShape(boolean updateShape)
	{
		if(cachedShape == null || updateShape)
			updateShape();
		if(cachedCollisionShape == null || updateShape)
			updateCollisionShape();
		if(updateShape)
			updateRedstone();
	}
	
	protected VoxelShape cachedShape, cachedCollisionShape;
	
	protected void updateShape()
	{
		cachedShape = parts.values()
				.stream()
				.map(PartEntity::updateShape)
				.reduce(Shapes.empty(), Shapes::or);
	}
	
	protected void updateCollisionShape()
	{
		cachedCollisionShape = parts.values()
				.stream()
				.map(PartEntity::updateCollisionShape)
				.reduce(Shapes.empty(), Shapes::or);
	}
	
	public long calcPartsHash()
	{
		long hash = 1L;
		var pp = PartRegistries.partPlacements();
		var pd = PartRegistries.partDefinitions();
		for(Map.Entry<PartPlacement, PartEntity> entry : parts.entrySet())
		{
			hash *= 31L;
			hash += Objects.hashCode(pp.getKey(entry.getKey()));
			hash *= 31L;
			hash += Objects.hashCode(pd.getKey(entry.getValue().definition()));
		}
		return hash;
	}
	
	/**
	 * Gets the summarized shape of all parts combined.
	 */
	public VoxelShape getShape()
	{
		if(cachedShape == null) updateShape();
		return cachedShape;
	}
	
	/**
	 * Gets the summarized shape of all parts combined for collision checking.
	 */
	public VoxelShape getCollisionShape()
	{
		if(cachedCollisionShape == null) updateCollisionShape();
		return cachedCollisionShape;
	}
	
	public void updateRedstone()
	{
		for(var dir : Direction.values())
		{
			int i = dir.ordinal();
			int j = strongRedstoneSignals[i];
			if((strongRedstoneSignals[i] = parts().stream().mapToInt(p -> p.getStrongSignal(dir)).max().orElse(0)) != j)
				causeBlockUpdate = true;
			j = weakRedstoneSignals[i];
			if((weakRedstoneSignals[i] = parts().stream().mapToInt(p -> p.getWeakSignal(dir)).max().orElse(0)) != j)
				causeBlockUpdate = true;
		}
	}
	
	public CompoundTag serializeNBT()
	{
		var tag = new CompoundTag();
		
		var parts = new ListTag();
		for(var e : this.parts.values())
		{
			var element = new CompoundTag();
			element.putString("Pos", PartRegistries.partPlacements().getKey(e.placement()).toString());
			element.putString("Def", PartRegistries.partDefinitions().getKey(e.definition()).toString());
			element.put("Data", e.serialize());
			element.putIntArray("Tint", e.tintIndices);
			parts.add(element);
		}
		tag.put("Parts", parts);
		tag.putInt("Light", lightLevel);
		tag.putIntArray("WeakRS", weakRedstoneSignals);
		tag.putIntArray("StrongRS", strongRedstoneSignals);
		
		return tag;
	}
	
	protected long prevNetworkHash;
	
	public void deserializeNBT(CompoundTag tag)
	{
		this.parts.clear();
		
		lightLevel = tag.getInt("Light");
		var parts = tag.getList("Parts", Tag.TAG_COMPOUND);
		
		var rs = tag.getIntArray("WeakRS");
		System.arraycopy(rs, 0, weakRedstoneSignals, 0, Math.min(weakRedstoneSignals.length, rs.length));
		
		rs = tag.getIntArray("StrongRS");
		System.arraycopy(rs, 0, strongRedstoneSignals, 0, Math.min(strongRedstoneSignals.length, rs.length));
		
		for(int i = 0; i < parts.size(); i++)
		{
			var element = parts.getCompound(i);
			var pos = ResourceLocation.tryParse(element.getString("Pos"));
			var def = ResourceLocation.tryParse(element.getString("Def"));
			if(pos == null || def == null) continue;
			var placement = PartRegistries.partPlacements().getValue(pos);
			var definition = PartRegistries.partDefinitions().getValue(def);
			if(placement == null || definition == null)
			{
				LOG.warn("Unable to deserialize part with definition {}({}) in {}({}) (at {}): {} unknown", definition, def, placement, pos, pos(),
						placement == null ? (definition == null ? "placement & definition" : "placement") : "definition"
				);
				continue;
			}
			var part = definition.createEntity(this, placement);
			if(part == null)
			{
				LOG.warn("Unable to create part with definition {}", def);
				continue;
			}
			part.deserialize(element.getCompound("Data"));
			setPartAt(placement, part, false);
			
			int[] tints = element.getIntArray("Tint");
			System.arraycopy(tints, 0, part.tintIndices, 0, Math.min(part.tintIndices.length, tints.length));
		}
		
		long newHash = calcPartsHash();
		if(prevNetworkHash != newHash)
		{
			prevNetworkHash = newHash;
			causeBlockUpdate = true;
		}
	}
	
	public Optional<Map.Entry<PartPlacement, PartEntity>> selectPart(Vec3 hitPos)
	{
		double iwx = this.pos.getX(), iwy = this.pos.getY(), iwz = this.pos.getZ();
		final var lpos = hitPos.subtract(iwx, iwy, iwz);
		return parts.entrySet()
				.stream()
				.filter(e -> e.getValue()
						.getShape()
						.toAabbs()
						.stream()
						.anyMatch(a -> a.inflate(0.0000001D).contains(lpos))
				)
				.findFirst()
				.map(part ->
				{
					if(part.getValue().getMainPart() == part.getKey())
						return part;
					return Map.entry(part.getValue().getMainPart(), part.getValue());
				});
	}
	
	public Level level()
	{
		return level;
	}
	
	public BlockPos pos()
	{
		return pos;
	}
	
	public boolean isEmpty()
	{
		return parts.isEmpty();
	}
	
	public Collection<PartEntity> parts()
	{
		return parts.values();
	}
	
	public void breakPart(Player player, boolean willHarvest, PartPlacement placement)
	{
		var ent = parts.get(placement);
		if(ent != null)
		{
			placement = ent.getMainPart();
			ent = parts.remove(placement);
			ent.onRemovedBy(player, willHarvest);
			causeBlockUpdate = true;
		}
	}
	
	protected final List<QueuedPartRemoval> toRemove = new ArrayList<>();
	
	protected record QueuedPartRemoval(PartPlacement placement, boolean drops, boolean sound, boolean particles) {}
	
	public void queuePartRemoval(PartPlacement placement, boolean spawnDrops, boolean playSound, boolean spawnParticles)
	{
		toRemove.add(new QueuedPartRemoval(placement, spawnDrops, playSound, spawnParticles));
	}
	
	public static Optional<PartContainer> turnIntoMultipart(Level level, BlockPos pos)
	{
		var state = level.getBlockState(pos);
		for(PartDefinition definition : PartRegistries.partDefinitions())
		{
			var cfg = definition.convertBlockToPart(level, pos, state).orElse(null);
			if(cfg != null)
			{
				level.setBlockAndUpdate(pos, WorldPartComponents.BLOCK.defaultBlockState(level, pos)
						.setValue(BlockMultipartContainer.LIGHT_LEVEL, 0)
				);
				var pc = BlockMultipartContainer.pc(level, pos);
				if(pc == null) continue;
				var pe = cfg.placer().create(pc, cfg.placement());
				pc.setPartAt(cfg.placement(), pe, true);
				level.setBlockAndUpdate(pos, WorldPartComponents.BLOCK.defaultBlockState(level, pos)
						.setValue(BlockMultipartContainer.LIGHT_LEVEL, pe.getLightEmission())
				);
				return Optional.of(pc);
			}
		}
		return Optional.empty();
	}
	
	public void neighborChanged(Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		for(PartEntity part : parts())
			part.neighborChanged(from, neigborPos, neigborState, waterlogged);
	}
}