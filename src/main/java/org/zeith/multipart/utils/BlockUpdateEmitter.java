package org.zeith.multipart.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class BlockUpdateEmitter
{
	public static void blockUpdated(Level level, BlockPos pos)
	{
		if(level == null || pos == null) return;
		Optional.ofNullable(level.getBlockEntity(pos)).ifPresent(BlockEntity::requestModelDataUpdate);
		var state = level.getBlockState(pos);
		level.sendBlockUpdated(pos, state, state, 3);
		level.blockUpdated(pos, state.getBlock());
		level.setBlockAndUpdate(pos, Block.updateFromNeighbourShapes(state, level, pos));
	}
}