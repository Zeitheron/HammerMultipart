package org.zeith.multipart.impl.parts;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.zeith.hammerlib.annotations.*;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.*;
import org.zeith.multipart.impl.parts.entities.PartEntityLadder;

import java.util.*;

@SimplyRegister
public class PartDefLadder
		extends PartDefinition
{
	@RegistryName("ladder")
	public static final PartDefLadder LADDER_PART = new PartDefLadder();
	
	public PartDefLadder()
	{
		model.addParticleIcon(new ResourceLocation("block/ladder"));
		soundType = SoundType.LADDER;
		destroySpeed = 0.4F;
		cloneItem = Items.LADDER::getDefaultInstance;
	}
	
	public Optional<PlacedPartConfiguration> getPlacement(Level level, BlockPos pos, Player player, ItemStack stack, BlockHitResult hit)
	{
		Direction towards = hit.getDirection().getOpposite();
		if(level.getBlockState(pos).isFaceSturdy(level, pos, hit.getDirection()))
			return Optional.of(new PlacedPartConfiguration(this, PartRegistries.SIDED_PLACEMENT.apply(towards)));
		return Optional.empty();
	}
	
	@Override
	public Optional<PlacedPartConfiguration> convertBlockToPart(Level level, BlockPos pos, BlockState state)
	{
		if(state.is(Blocks.LADDER))
			return Optional.of(new PlacedPartConfiguration(this,
					PartRegistries.SIDED_PLACEMENT.apply(state.getValue(LadderBlock.FACING).getOpposite())
			));
		return Optional.empty();
	}
	
	@Override
	public boolean canPlaceAt(PartContainer container, PartPlacement placement)
	{
		Direction towards = placement.getDirection();
		if(towards == null || towards.getAxis() == Direction.Axis.Y) return false;
		if(PartRegistries.SIDED_PLACEMENT.apply(towards) != placement) return false;
		BlockPos pos = container.pos().relative(towards);
		BlockState blockstate = container.level().getBlockState(pos);
		return blockstate.isFaceSturdy(container.level(), pos, towards.getOpposite());
	}
	
	@Override
	public PartEntity createEntity(PartContainer container, PartPlacement placement)
	{
		return new PartEntityLadder(this, container, placement);
	}
}
