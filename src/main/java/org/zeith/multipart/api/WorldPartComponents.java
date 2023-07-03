package org.zeith.multipart.api;

import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.zeith.hammerlib.annotations.*;
import org.zeith.hammerlib.annotations.client.TileRenderer;
import org.zeith.hammerlib.api.forge.BlockAPI;
import org.zeith.multipart.client.rendering.TESRMultipartContainer;
import org.zeith.multipart.blocks.*;

@SimplyRegister
public class WorldPartComponents
{
	@RegistryName("multipart")
	public static final BlockMultipartContainer BLOCK = new BlockMultipartContainer();
	
	@RegistryName("multipart")
	@TileRenderer(TESRMultipartContainer.class)
	public static final BlockEntityType<TileMultipartContainer> TILE_TYPE = BlockAPI.createBlockEntityType(TileMultipartContainer::new, BLOCK);
	
	public static boolean isSideSolid(Level level, BlockPos pos, Direction side, SupportType support)
	{
		return level.getBlockState(pos).isFaceSturdy(level, pos, side, support);
	}
	
	public static boolean isSideSolid(Level level, BlockPos pos, Direction side)
	{
		return isSideSolid(level, pos, side, SupportType.FULL);
	}
}