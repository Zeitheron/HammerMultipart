package org.zeith.multipart.impl.parts.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.hammerlib.util.java.tuples.Tuples;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.init.PartPlacementsHM;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PartEntityLadder
		extends PartEntity
{
	public PartEntityLadder(PartDefinition definition, PartContainer container, PartPlacement placement)
	{
		super(definition, container, placement);
	}
	
	protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
	protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
	protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
	
	@Override
	public List<ItemStack> getDrops(@Nullable ServerPlayer harvester, LootParams.Builder drops)
	{
		return Blocks.LADDER.defaultBlockState().getDrops(drops);
	}
	
	@Override
	protected VoxelShape updateShape()
	{
		Direction direction = placement.getDirection();
		if(direction == null) return EAST_AABB;
		return switch(direction.getOpposite())
		{
			case NORTH -> NORTH_AABB;
			case SOUTH -> SOUTH_AABB;
			case WEST -> WEST_AABB;
			default -> EAST_AABB;
		};
	}
	
	@Override
	public VoxelShape getPartOccupiedShape()
	{
		return getShape();
	}
	
	@Override
	public Optional<Tuple2<BlockState, Function<BlockPos, BlockEntity>>> disassemblePart()
	{
		return Optional.of(Tuples.immutable(getRenderState().setValue(LadderBlock.WATERLOGGED, container.waterlogged), null));
	}
	
	@Override
	public void neighborChanged(@Nullable Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		Direction towards = placement.getDirection();
		if(towards == null) return;
		if(PartPlacementsHM.SIDED_PLACEMENT.apply(towards) != placement) return;
		BlockPos pos = container.pos().relative(towards);
		BlockState blockstate = container.level().getBlockState(pos);
		if(!blockstate.isFaceSturdy(container.level(), pos, towards.getOpposite()))
		{
			container.queuePartRemoval(placement, true, true, true);
		}
	}
	
	@Nullable
	@Override
	public BlockState getRenderState()
	{
		var dir = placement.getDirection();
		if(dir == null || dir.getAxis() == Direction.Axis.Y) return null;
		return Blocks.LADDER.defaultBlockState()
				.setValue(LadderBlock.FACING, placement.getDirection().getOpposite());
	}
	
	@Override
	public boolean isLadder(LivingEntity entity)
	{
		return entity == null ||
				getCollisionShape().bounds().move(container.pos()).inflate(0.15).intersects(entity.getBoundingBox());
	}
	
	@Override
	public boolean makesOpenTrapdoorAboveClimbable(BlockState trapdoorState)
	{
		return placement.getDirection() == trapdoorState.getValue(TrapDoorBlock.FACING).getOpposite();
	}
	
	@Override
	public boolean isViewBlocking()
	{
		return false;
	}
}