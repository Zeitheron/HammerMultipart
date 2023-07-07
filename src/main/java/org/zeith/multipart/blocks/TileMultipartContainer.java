package org.zeith.multipart.blocks;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.*;
import org.zeith.hammerlib.api.forge.*;
import org.zeith.hammerlib.api.tiles.IContainerTile;
import org.zeith.hammerlib.tiles.TileSyncableTickable;
import org.zeith.multipart.api.*;

import java.util.Objects;

public class TileMultipartContainer
		extends TileSyncableTickable
		implements IContainerTile
{
	public final PartContainer container;
	
	protected long prevHash;
	
	public TileMultipartContainer(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.container = new PartContainer(pos.immutable(), this::open);
	}
	
	public void open(Player player)
	{
		ContainerAPI.openContainerTile(player, this);
	}
	
	@Override
	public AbstractContainerMenu openContainer(Player player, int windowId)
	{
		var hit = BlockMultipartContainer.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if(hit.getType() != HitResult.Type.BLOCK) return null;
		return container.selectPart(hit.getLocation()).map(e -> e.getValue().openContainer(player, windowId))
				.orElse(null);
	}
	
	@Override
	public void serverTick()
	{
		container.level = level;
		container.waterlogged = getBlockState().getOptionalValue(BlockStateProperties.WATERLOGGED).orElse(false);
		
		container.tickServer();
		if(container.needsSync)
		{
			sync();
			container.needsSync = false;
		}
		
		if(container.isEmpty())
		{
			container.lightLevel = 0;
			level.removeBlock(worldPosition, true);
			updateRedstoneNeighbors();
			return;
		}
		
		sharedTick();
	}
	
	@Override
	public void clientTick()
	{
		container.level = level;
		container.waterlogged = getBlockState().getOptionalValue(BlockStateProperties.WATERLOGGED).orElse(false);
		container.tickClient();
		container.needsSync = false;
		sharedTick();
	}
	
	public boolean tryDisassemble()
	{
		var ps = container.parts();
		
		if(ps.size() == 1)
		{
			var part = ps.iterator().next();
			var simple = part.disassemblePart();
			if(simple.isPresent())
			{
				simple.ifPresent(tup ->
				{
					updateRedstoneNeighbors();
					level.setBlockAndUpdate(worldPosition, tup.a());
					var tile = tup.b();
					if(tile != null)
						level.setBlockEntity(tile.apply(worldPosition));
				});
				return true;
			}
		}
		
		return false;
	}
	
	public void sharedTick()
	{
		if(tryDisassemble()) return;
		
		if(atTickRate(5) || container.causeBlockUpdate || container.needsSync)
		{
			long hash = container.calcPartsHash();
			if(hash != prevHash)
			{
				prevHash = hash;
				container.recalcTintLayers(hash);
				BlockAPI.sendBlockUpdate(level, worldPosition);
			}
		}
		
		if(container.causeRedstoneUpdate)
		{
			updateRedstoneNeighbors();
			container.causeRedstoneUpdate = false;
		}
		
		if(container.causeBlockUpdate)
		{
			if(isOnServer())
			{
				boolean isSource = container.parts().stream().anyMatch(PartEntity::isRedstoneSource);
				int light = Mth.clamp(container.parts().stream().mapToInt(PartEntity::getLightEmission).max()
						.orElse(0), 0, 15);
				
				var st = level.getBlockState(worldPosition);
				level.setBlockAndUpdate(worldPosition,
						st.setValue(BlockMultipartContainer.ALT, !st.getValue(BlockMultipartContainer.ALT))
								.setValue(BlockMultipartContainer.REDSTONE_SOURCE, isSource)
								.setValue(BlockMultipartContainer.LIGHT_LEVEL, light)
				);
				level.updateNeighborsAt(worldPosition, st.getBlock());
				BlockAPI.sendBlockUpdate(level, worldPosition);
				updatePowerStrength();
			}
			container.causeBlockUpdate = false;
		}
	}
	
	@Override
	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		container.onChunkUnloaded();
	}
	
	@Override
	public void onLoad()
	{
		super.onLoad();
		container.onLoad();
	}
	
	public long getHash()
	{
		return prevHash;
	}
	
	public void setLightLevel(int light)
	{
		light = Mth.clamp(light, 0, 15);
		var prop = BlockMultipartContainer.LIGHT_LEVEL;
		var state = getBlockState();
		if(state.hasProperty(prop) && !Objects.equals(state.getValue(prop), light))
			level.setBlockAndUpdate(worldPosition, state.setValue(prop, light));
	}
	
	private final int[] weakRedstones = new int[6], strongRedstones = new int[6];
	
	public void updatePowerStrength()
	{
		boolean upd = false;
		for(int i = 0; i < 6; i++)
		{
			if(weakRedstones[i] != container.weakRedstoneSignals[i])
			{
				weakRedstones[i] = container.weakRedstoneSignals[i];
				upd = true;
				break;
			}
			if(strongRedstones[i] != container.strongRedstoneSignals[i])
			{
				strongRedstones[i] = container.strongRedstoneSignals[i];
				upd = true;
				break;
			}
		}
		
		if(upd) updateRedstoneNeighbors();
	}
	
	public void updateRedstoneNeighbors()
	{
		var state = level.getBlockState(worldPosition);
		level.updateNeighborsAt(worldPosition, state.getBlock());
		for(Direction direction : Direction.values())
			level.updateNeighborsAt(worldPosition.relative(direction), state.getBlock());
	}
	
	@Override
	public CompoundTag writeNBT(CompoundTag nbt)
	{
		var res = container.serializeNBT();
		return nbt.isEmpty() ? res : nbt.merge(res);
	}
	
	@Override
	public void readNBT(CompoundTag nbt)
	{
		container.deserializeNBT(nbt);
	}
	
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		var c = container.getCapability(cap, side);
		if(c.isPresent()) return c;
		return super.getCapability(cap, side);
	}
	
	@Override
	public @NotNull ModelData getModelData()
	{
		return ModelData.builder()
				.with(PartContainer.CONTAINER_PROP, container)
				.with(PartContainer.PART_HASH, prevHash)
				.build();
	}
}