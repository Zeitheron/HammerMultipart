package org.zeith.multipart.impl.parts.entities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.abstractions.actions.impl.MethodHandleLevelAction;
import org.zeith.hammerlib.annotations.ExposedToLevelAction;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.util.java.reflection.SerializableMethodHandle;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.hammerlib.util.java.tuples.Tuples;
import org.zeith.hammerlib.util.mcf.LogicalSidePredictor;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.api.placement.PartPos;
import org.zeith.multipart.init.PartPlacementsHM;

import java.util.*;
import java.util.function.Function;

public class PartEntityRedstoneTorch
		extends PartEntity
{
	public final ParticleOptions flameParticle = DustParticleOptions.REDSTONE;
	
	@NBTSerializable
	protected boolean lit = true;
	
	@NBTSerializable
	protected boolean awaitingTick;
	
	protected final List<Toggle> recentToggles = new ArrayList<>();
	
	public PartEntityRedstoneTorch(PartDefinition definition, PartContainer container, PartPlacement placement)
	{
		super(definition, container, placement);
	}
	
	private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(5.5D, 3.0D, 11.0D, 10.5D, 13.0D, 16.0D), Direction.SOUTH, Block.box(5.5D, 3.0D, 0.0D, 10.5D, 13.0D, 5.0D), Direction.WEST, Block.box(11.0D, 3.0D, 5.5D, 16.0D, 13.0D, 10.5D), Direction.EAST, Block.box(0.0D, 3.0D, 5.5D, 5.0D, 13.0D, 10.5D)));
	private static VoxelShape AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);
	
	@Override
	public VoxelShape updateCollisionShape()
	{
		return Shapes.empty();
	}
	
	@Override
	public List<ItemStack> getDrops(@Nullable ServerPlayer harvester, LootParams.Builder drops)
	{
		return Blocks.REDSTONE_TORCH.defaultBlockState().getDrops(drops);
	}
	
	@Override
	protected VoxelShape updateShape()
	{
		if(placement == PartPlacementsHM.DOWN)
		{
			return AABB;
		} else
		{
			Direction direction = placement.getDirection();
			if(direction.getAxis() == Direction.Axis.Y) return super.updateShape();
			return AABBS.get(direction.getOpposite());
		}
	}
	
	@Override
	public VoxelShape getPartOccupiedShape()
	{
		return getShape();
	}
	
	@Override
	public Optional<Tuple2<BlockState, Function<BlockPos, BlockEntity>>> disassemblePart()
	{
		return Optional.of(Tuples.immutable(getRenderState(), null));
	}
	
	@Override
	public void animateTick(RandomSource random)
	{
		if(random.nextBoolean() || !lit) return;
		
		var level = container.level();
		var pos = container.pos();
		if(placement == PartPlacementsHM.DOWN)
		{
			double d0 = (double) pos.getX() + 0.5D;
			double d1 = (double) pos.getY() + 0.7D;
			double d2 = (double) pos.getZ() + 0.5D;
			level.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		} else
		{
			Direction direction = placement.getDirection();
			if(direction == null) return;
			direction = direction.getOpposite();
			double d0 = (double) pos.getX() + 0.5D;
			double d1 = (double) pos.getY() + 0.7D;
			double d2 = (double) pos.getZ() + 0.5D;
			Direction direction1 = direction.getOpposite();
			level.addParticle(this.flameParticle,
					d0 + 0.27D * (double) direction1.getStepX(),
					d1 + 0.22D, d2 + 0.27D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D
			);
		}
	}
	
	@Override
	public void neighborChanged(@Nullable Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		Direction towards = placement.getDirection();
		if(towards == null) return;
		if(PartPlacementsHM.SIDED_PLACEMENT.apply(towards) != placement) return;
		BlockPos pos = container.pos().relative(towards);
		if(waterlogged || !container.level().getBlockState(pos).isFaceSturdy(container.level(), pos, towards.getOpposite()))
		{
			container.queuePartRemoval(placement, true, true, true);
		}
		
		boolean ns = this.hasNeighborSignal(container.level, container.pos);
		if(lit == ns && container.level instanceof ServerLevel sl)
		{
			awaitingTick = true;
			new MethodHandleLevelAction(
					SerializableMethodHandle.create(PartEntityRedstoneTorch.class, "tickRedstoneTorch", null, sl.dimension(), pos())
			).delay(2).enqueue(sl);
		}
	}
	
	protected void scheduledTick()
	{
		awaitingTick = false;
		if(!isAddedToWorld() || container.level == null) return;
		var pLevel = container.level;
		var pPos = container.pos;
		
		boolean neighborSignal = this.hasNeighborSignal(pLevel, pPos);
		List<Toggle> list = recentToggles;
		while(list != null && !list.isEmpty() && pLevel.getGameTime() - (list.get(0)).when > 60L)
			list.remove(0);
		
		if(lit)
		{
			if(neighborSignal)
			{
				lit = false;
				if(isToggledTooFrequently(pLevel, true))
				{
					pLevel.levelEvent(1502, pPos, 0);
					pLevel.scheduleTick(pPos, pLevel.getBlockState(pPos).getBlock(), 160);
				}
			}
		} else if(!neighborSignal && !isToggledTooFrequently(pLevel, false))
		{
			lit = true;
		}
		
		container.updateRedstone();
		container.owner.syncContainer(true);
	}
	
	private boolean isToggledTooFrequently(Level level, boolean pLogToggle)
	{
		List<Toggle> list = recentToggles;
		
		if(pLogToggle)
			list.add(new Toggle(level.getGameTime()));
		
		int i = 0;
		
		for(int j = 0; j < list.size(); ++j)
		{
			++i;
			if(i >= 8)
				return true;
		}
		
		return false;
	}
	
	@Override
	public int getLightEmission()
	{
		return lit ? 7 : 0; // vanilla torch emission
	}
	
	protected boolean hasNeighborSignal(Level pLevel, BlockPos pPos)
	{
		var dir = placement.getDirection();
		if(dir == null || dir == Direction.DOWN)
			return pLevel.hasSignal(pPos.below(), Direction.DOWN);
		return pLevel.hasSignal(pPos.relative(dir), dir);
	}
	
	@Override
	public int getStrongSignal(Direction towards)
	{
		return towards == Direction.DOWN ? getWeakSignal(towards) : 0;
	}
	
	@Override
	public int getWeakSignal(Direction towards)
	{
		var dir = placement.getDirection();
		if(dir == null) return 0;
		
		return lit && dir.getOpposite() != towards ? 15 : 0;
	}
	
	@Override
	public boolean canConnectRedstone(@Nullable Direction direction)
	{
		return true;
	}
	
	@Nullable
	@Override
	public BlockState getRenderState()
	{
		var dir = placement.getDirection();
		if(dir == null || dir == Direction.DOWN) return Blocks.REDSTONE_TORCH.defaultBlockState().setValue(RedstoneTorchBlock.LIT, lit);
		if(dir.getAxis() == Direction.Axis.Y) return null;
		return Blocks.REDSTONE_WALL_TORCH.defaultBlockState()
				.setValue(RedstoneWallTorchBlock.FACING, placement.getDirection().getOpposite())
				.setValue(RedstoneTorchBlock.LIT, lit);
	}
	
	@Override
	public boolean isViewBlocking()
	{
		return false;
	}
	
	@Override
	public boolean isRedstoneSource()
	{
		return true;
	}
	
	@ExposedToLevelAction
	public static void tickRedstoneTorch(ResourceKey<Level> dimension, PartPos part)
	{
		ServerLevel level = LogicalSidePredictor.getLevel(dimension);
		if(level == null) return;
		var torch = part.getOfType(level, PartEntityRedstoneTorch.class);
		if(!torch.isAddedToWorld()) torch.setAddedToWorld(true);
		if(torch.container.level == null) torch.container.level = level;
		if(torch != null) torch.scheduledTick();
	}
	
	public static class Toggle
	{
		final long when;
		
		public Toggle(long pWhen)
		{
			this.when = pWhen;
		}
	}
}