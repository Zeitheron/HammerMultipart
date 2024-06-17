package org.zeith.multipart.api;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.annotations.client.TileRenderer;
import org.zeith.hammerlib.api.forge.BlockAPI;
import org.zeith.hammerlib.api.registrars.Registrar;
import org.zeith.multipart.api.src.PartSourceType;
import org.zeith.multipart.blocks.BlockMultipartContainer;
import org.zeith.multipart.blocks.TileMultipartContainer;
import org.zeith.multipart.client.rendering.TESRMultipartContainer;

@SimplyRegister
public class WorldPartComponents
{
	@RegistryName("multipart")
	public static final BlockMultipartContainer BLOCK = new BlockMultipartContainer(Block.Properties.of()
			.forceSolidOff()
			.dynamicShape()
			.noOcclusion()
			.lightLevel(s -> s.getValue(BlockMultipartContainer.LIGHT_LEVEL)));
	
	
	@RegistryName("multipart_container")
	public static final Registrar<MapCodec<BlockMultipartContainer>> BLOCK_CODEC = Registrar.blockType(BlockMultipartContainer.CODEC);
	
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
	
	public static PartContainer createFragile(LevelAccessor level, BlockPos pos)
	{
		return BLOCK.safePlace(level, pos, (l, p) ->
		{
			l.setBlock(p, BLOCK.defaultBlockState(l, p), 4 | 16);
			return BlockMultipartContainer.pc(l, p);
		});
	}
}