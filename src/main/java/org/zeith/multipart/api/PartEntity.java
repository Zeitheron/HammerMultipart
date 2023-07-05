package org.zeith.multipart.api;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.*;
import org.zeith.hammerlib.api.io.NBTSerializationHelper;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.client.MultipartEffects;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public abstract class PartEntity
		extends CapabilityProvider<PartEntity>
{
	protected final PartDefinition definition;
	protected final PartContainer container;
	protected final PartPlacement placement;
	protected final int[] tintIndices;
	
	protected boolean syncDirty;
	protected boolean isShapeDirty, isCollisionShapeDirty;
	protected VoxelShape cachedShape, cachedCollisionShape;
	
	public PartEntity(PartDefinition definition, PartContainer container, PartPlacement placement)
	{
		super(PartEntity.class);
		this.tintIndices = new int[definition.getTintIndexCount()];
		this.container = container;
		this.definition = definition;
		this.placement = placement;
	}
	
	@NotNull
	public PartPlacement getMainPart()
	{
		return placement;
	}
	
	public Collection<ResourceLocation> getParticleIcons(Set<ResourceLocation> allowed)
	{
		return allowed;
	}
	
	public int[] getTintIndices()
	{
		return tintIndices;
	}
	
	/**
	 * Return the hexadecimal color for tinting your part.
	 * NOTE: -1 WILL be called when particles are spawned.
	 */
	public int getTintLayerColor(@Range(from = -1, to = Integer.MAX_VALUE) int tintLayer)
	{
		return 0xFFFFFF;
	}
	
	protected VoxelShape updateCollisionShape()
	{
		return updateShape();
	}
	
	protected VoxelShape updateShape()
	{
		return placement.getExampleShape();
	}
	
	public void tickServer()
	{
		tickShared();
	}
	
	public void tickClient()
	{
		tickShared();
	}
	
	protected void tickShared()
	{
	}
	
	public Optional<Tuple2<BlockState, Function<BlockPos, BlockEntity>>> disassemblePart()
	{
		return Optional.empty();
	}
	
	public AbstractContainerMenu openContainer(Player player, int windowId)
	{
		return null;
	}
	
	public float getDestroySpeed(Player player)
	{
		return definition.destroySpeed;
	}
	
	public boolean isCorrectToolForDrops(@NotNull Player player)
	{
		return true;
	}
	
	public void onRemovedBy(Player player, boolean willHarvest)
	{
		onRemoved(player, willHarvest && !player.isCreative(), false, true);
		
		var sound = definition().getSoundType(this);
		player.level().playSound(player, container().pos(), sound.getBreakSound(), SoundSource.BLOCKS,
				(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F
		);
	}
	
	public void onRemoved(Player harvester, boolean spawnDrops, boolean playSound, boolean spawnParticles)
	{
		invalidateCaps();
		
		if(playSound)
		{
			var sound = definition().getSoundType(this);
			container().level().playSound(null, container().pos(), sound.getBreakSound(), SoundSource.BLOCKS,
					(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F
			);
		}
		
		if(spawnParticles)
		{
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
			{
				MultipartEffects.spawnBreakFX(this);
			});
		}
		
		if(spawnDrops && container.level instanceof ServerLevel sl)
		{
			var level = container.level();
			var pos = container.pos();
			
			var loot = new LootParams.Builder(sl)
					.withParameter(LootContextParams.TOOL,
							harvester != null ? harvester.getMainHandItem() : ItemStack.EMPTY
					)
					.withOptionalParameter(LootContextParams.THIS_ENTITY, harvester)
					.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
			
			for(ItemStack drop : getDrops(harvester instanceof ServerPlayer sp ? sp : null, loot))
				Block.popResource(level, pos, drop);
		}
	}
	
	public List<ItemStack> getDrops(@Nullable ServerPlayer harvester, LootParams.Builder context)
	{
		return List.of();
	}
	
	public void neighborChanged(@Nullable Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		if(!canSurviveInWater() && waterlogged)
			container.queuePartRemoval(placement, true, true, true);
	}
	
	public int getWeakSignal(Direction towards)
	{
		return 0;
	}
	
	public int getStrongSignal(Direction towards)
	{
		return 0;
	}
	
	public CompoundTag serialize()
	{
		return NBTSerializationHelper.serialize(this);
	}
	
	public void deserialize(CompoundTag tag)
	{
		NBTSerializationHelper.deserialize(this, tag);
	}
	
	public final PartDefinition definition()
	{
		return definition;
	}
	
	public final PartContainer container()
	{
		return container;
	}
	
	public final PartPlacement placement()
	{
		return placement;
	}
	
	public boolean isShapeDirty()
	{
		return isShapeDirty || cachedShape == null;
	}
	
	public boolean isCollisionShapeDirty()
	{
		return isCollisionShapeDirty || cachedCollisionShape == null;
	}
	
	public IndexedVoxelShape getSelectionShape(Player player, BlockHitResult hit)
	{
		return new IndexedVoxelShape(0, getShape());
	}
	
	public final VoxelShape getShape()
	{
		if(isShapeDirty())
		{
			cachedShape = updateShape();
			isShapeDirty = false;
		}
		return cachedShape;
	}
	
	public final VoxelShape getCollisionShape()
	{
		if(isCollisionShapeDirty())
		{
			cachedCollisionShape = updateShape();
			isCollisionShapeDirty = false;
		}
		return cachedCollisionShape;
	}
	
	public VoxelShape getPartOccupiedShape()
	{
		return getCollisionShape();
	}
	
	public InteractionResult use(Player player, InteractionHand hand, BlockHitResult hit, IndexedVoxelShape selection)
	{
		return InteractionResult.PASS;
	}
	
	public void attack(Player player, BlockHitResult hit, IndexedVoxelShape selection)
	{
	}
	
	public boolean syncDirty()
	{
		return syncDirty;
	}
	
	public void markSynced()
	{
		syncDirty = false;
	}
	
	public ItemStack getCloneItemStack(BlockHitResult target, Player player, IndexedVoxelShape selection)
	{
		return definition.getCloneItem();
	}
	
	public void animateTick(RandomSource random)
	{
	}
	
	public boolean isRedstoneSource()
	{
		return false;
	}
	
	public int getLightEmission()
	{
		return 0;
	}
	
	@Nullable
	public BlockState getRenderState()
	{
		return null;
	}
	
	@Nullable
	public BlockState getHardnessState()
	{
		return getRenderState();
	}
	
	public List<ResourceLocation> getRenderModels()
	{
		return List.of();
	}
	
	public void onPlaced()
	{
	}
	
	public void stepOn(Entity entity)
	{
	}
	
	public boolean isLadder(LivingEntity entity)
	{
		return false;
	}
	
	public boolean canHarvestPart(Player player)
	{
		var state = getRenderState();
		if(state != null)
			return state.canHarvestBlock(container.level(), container.pos(), player);
		return false;
	}
	
	public boolean canConnectRedstone(@Nullable Direction direction)
	{
		return false;
	}
	
	public BlockState getAppearance(BlockState state, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos)
	{
		return state;
	}
	
	public boolean makesOpenTrapdoorAboveClimbable(BlockState trapdoorState)
	{
		return false;
	}
	
	public boolean canSurviveInWater()
	{
		return definition.canSurviveInWater(this);
	}
	
	public static VoxelShape createAttachmentShape(Direction dir, float minAxial, float maxAxial, float elevation)
	{
		float invElevation = 1 - elevation;
		VoxelShape ourShape;
		if(dir == null) ourShape = Shapes.empty();
		else ourShape = switch(dir)
		{
			case DOWN -> Shapes.create(minAxial, 0, minAxial, maxAxial, elevation, maxAxial);
			case UP -> Shapes.create(minAxial, invElevation, minAxial, maxAxial, 1, maxAxial);
			case NORTH -> Shapes.create(minAxial, minAxial, 0, maxAxial, maxAxial, elevation);
			case SOUTH -> Shapes.create(minAxial, minAxial, invElevation, maxAxial, maxAxial, 1);
			case WEST -> Shapes.create(0, minAxial, minAxial, elevation, maxAxial, maxAxial);
			case EAST -> Shapes.create(invElevation, minAxial, minAxial, 1, maxAxial, maxAxial);
		};
		return ourShape;
	}
	
	public boolean blocksPlacementFor(PartDefinition definition, PartPlacement definitionPosition)
	{
		return false;
	}
}