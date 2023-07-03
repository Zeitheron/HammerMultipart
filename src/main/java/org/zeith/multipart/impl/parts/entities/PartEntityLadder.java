package org.zeith.multipart.impl.parts.entities;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.util.java.tuples.*;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPlacement;

import java.util.*;
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
		return Blocks.LADDER.getDrops(Blocks.LADDER.defaultBlockState(), drops);
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
	public Optional<Tuple2<BlockState, Function<BlockPos, BlockEntity>>> disassemblePart()
	{
		return Optional.of(Tuples.immutable(getRenderState().setValue(LadderBlock.WATERLOGGED, container.waterlogged), null));
	}
	
	@Override
	public void neighborChanged(@Nullable Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		Direction towards = placement.getDirection();
		if(towards == null) return;
		if(PartRegistries.SIDED_PLACEMENT.apply(towards) != placement) return;
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
		return true;
	}
	
	@Override
	public boolean makesOpenTrapdoorAboveClimbable(BlockState trapdoorState)
	{
		return placement.getDirection() == trapdoorState.getValue(TrapDoorBlock.FACING).getOpposite();
	}
}