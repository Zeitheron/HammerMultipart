package org.zeith.multipart.impl.parts.entities;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.util.java.tuples.*;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPlacement;

import java.util.*;
import java.util.function.Function;

public class PartEntityChain
		extends PartEntity
{
	@NBTSerializable("Axis")
	public Direction.Axis axis = Direction.Axis.Y;
	
	public PartEntityChain(PartDefinition definition, PartContainer container, PartPlacement placement)
	{
		super(definition, container, placement);
	}
	
	protected static final VoxelShape Y_AXIS_AABB = Block.box(6.5D, 0.0D, 6.5D, 9.5D, 16.0D, 9.5D);
	protected static final VoxelShape Z_AXIS_AABB = Block.box(6.5D, 6.5D, 0.0D, 9.5D, 9.5D, 16.0D);
	protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 6.5D, 6.5D, 16.0D, 9.5D, 9.5D);
	
	@Override
	public List<ItemStack> getDrops(@Nullable ServerPlayer harvester, LootParams.Builder drops)
	{
		return Blocks.CHAIN.defaultBlockState().getDrops(drops);
	}
	
	@Override
	protected VoxelShape updateShape()
	{
		if(axis == null) return Y_AXIS_AABB;
		return switch(axis)
		{
			case X -> X_AXIS_AABB;
			case Y -> Y_AXIS_AABB;
			case Z -> Z_AXIS_AABB;
			default -> Y_AXIS_AABB;
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
		return Optional.of(Tuples.immutable(getRenderState().setValue(ChainBlock.WATERLOGGED, container.waterlogged), null));
	}
	
	@Override
	public void neighborChanged(@Nullable Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		super.neighborChanged(from, neigborPos, neigborState, waterlogged);
		if(axis == null) container.queuePartRemoval(placement, true, true, true);
	}
	
	@Nullable
	@Override
	public BlockState getRenderState()
	{
		if(axis == null) return null;
		return Blocks.CHAIN.defaultBlockState()
				.setValue(ChainBlock.AXIS, axis);
	}
	
	@Override
	public boolean isViewBlocking()
	{
		return false;
	}
}