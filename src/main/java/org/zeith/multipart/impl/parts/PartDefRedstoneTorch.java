package org.zeith.multipart.impl.parts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.*;
import org.zeith.multipart.impl.parts.entities.PartEntityRedstoneTorch;
import org.zeith.multipart.init.PartPlacementsHM;

import java.util.Optional;

public class PartDefRedstoneTorch
		extends PartDefinition
{
	public PartDefRedstoneTorch()
	{
		model.addParticleIcon(new ResourceLocation("block/redstone_torch"));
		soundType = SoundType.WOOD;
		destroySpeed = 0.0001F;
		survivesInWater = false;
		cloneItem = Items.REDSTONE_TORCH::getDefaultInstance;
	}
	
	public Optional<PlacedPartConfiguration> getPlacement(Level level, BlockPos pos, Player player, ItemStack stack, BlockHitResult hit)
	{
		Direction towards = hit.getDirection().getOpposite();
		
		var offPos = pos.relative(towards);
		
		if(level.getBlockState(offPos).isFaceSturdy(level, offPos, hit.getDirection()))
			return Optional.of(new PlacedPartConfiguration(this, PartPlacementsHM.SIDED_PLACEMENT.apply(towards)));
		
		return Optional.empty();
	}
	
	@Override
	public Optional<PlacedPartConfiguration> convertBlockToPart(Level level, BlockPos pos, BlockState state)
	{
		if(state.is(Blocks.REDSTONE_TORCH))
			return Optional.of(new PlacedPartConfiguration(this, PartPlacementsHM.DOWN));
		if(state.is(Blocks.REDSTONE_WALL_TORCH))
			return Optional.of(new PlacedPartConfiguration(this,
					PartPlacementsHM.SIDED_PLACEMENT.apply(state.getValue(RedstoneWallTorchBlock.FACING).getOpposite())
			));
		return Optional.empty();
	}
	
	@Override
	public boolean canPlaceAt(PartContainer container, @Nullable IConfiguredPartPlacer placer, PartPlacement placement)
	{
		Direction towards = placement.getDirection();
		if(towards == null || towards == Direction.UP) return false;
		if(PartPlacementsHM.SIDED_PLACEMENT.apply(towards) != placement) return false;
		if(container.waterlogged) return false;
		BlockPos pos = container.pos().relative(towards);
		BlockState blockstate = container.level().getBlockState(pos);
		return blockstate.isFaceSturdy(container.level(), pos, towards.getOpposite());
	}
	
	@Override
	public PartEntity createEntity(PartContainer container, PartPlacement placement)
	{
		return new PartEntityRedstoneTorch(this, container, placement);
	}
}