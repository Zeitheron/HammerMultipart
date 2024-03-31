package org.zeith.multipart.api;

import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.zeith.hammerlib.annotations.*;
import org.zeith.hammerlib.annotations.client.TileRenderer;
import org.zeith.hammerlib.api.forge.BlockAPI;
import org.zeith.multipart.api.src.PartSourceType;
import org.zeith.multipart.blocks.*;
import org.zeith.multipart.client.rendering.TESRMultipartContainer;

@SimplyRegister
public class WorldPartComponents
{
	@RegistryName("multipart")
	public static final BlockMultipartContainer BLOCK = new BlockMultipartContainer();
	
	@RegistryName("multipart")
	@TileRenderer(TESRMultipartContainer.class)
	public static final BlockEntityType<TileMultipartContainer> TILE_TYPE = BlockAPI.createBlockEntityType(TileMultipartContainer::new, BLOCK);
	
	@RegistryName("multipart")
	public static final PartSourceType PART_SOURCE_TYPE = new PartSourceType();
	
	public static PartContainer getContainer(BlockGetter level, BlockPos pos)
	{
		return BlockMultipartContainer.pc(level, pos);
	}
	
	public static boolean isSideSolid(Level level, BlockPos pos, Direction side, SupportType support)
	{
		return level.getBlockState(pos).isFaceSturdy(level, pos, side, support);
	}
	
	public static boolean isSideSolid(Level level, BlockPos pos, Direction side)
	{
		return isSideSolid(level, pos, side, SupportType.FULL);
	}
}