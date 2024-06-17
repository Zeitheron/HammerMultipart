package org.zeith.multipart.impl.parts.entities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.hammerlib.util.java.tuples.Tuples;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.init.PartPlacementsHM;

import java.util.*;
import java.util.function.Function;

public class PartEntitySoulTorch
		extends PartEntity
{
	public final ParticleOptions flameParticle = ParticleTypes.SOUL_FIRE_FLAME;
	
	public PartEntitySoulTorch(PartDefinition definition, PartContainer container, PartPlacement placement)
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
		return Blocks.SOUL_TORCH.defaultBlockState().getDrops(drops);
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
		if(random.nextBoolean()) return;
		
		var level = container.level();
		var pos = container.pos();
		if(placement == PartPlacementsHM.DOWN)
		{
			double d0 = (double) pos.getX() + 0.5D;
			double d1 = (double) pos.getY() + 0.7D;
			double d2 = (double) pos.getZ() + 0.5D;
			level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
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
			level.addParticle(ParticleTypes.SMOKE,
					d0 + 0.27D * (double) direction1.getStepX(),
					d1 + 0.22D, d2 + 0.27D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D
			);
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
		if(waterlogged ||
				!container.level().getBlockState(pos).isFaceSturdy(container.level(), pos, towards.getOpposite()))
		{
			container.queuePartRemoval(placement, true, true, true);
		}
	}
	
	@Override
	public int getLightEmission()
	{
		return 10; // vanilla torch emission
	}
	
	@Nullable
	@Override
	public BlockState getRenderState()
	{
		var dir = placement.getDirection();
		if(dir == null || dir == Direction.DOWN) return Blocks.SOUL_TORCH.defaultBlockState();
		if(dir.getAxis() == Direction.Axis.Y) return null;
		return Blocks.SOUL_WALL_TORCH.defaultBlockState()
				.setValue(WallTorchBlock.FACING, placement.getDirection().getOpposite());
	}
	
	@Override
	public boolean isViewBlocking()
	{
		return false;
	}
}