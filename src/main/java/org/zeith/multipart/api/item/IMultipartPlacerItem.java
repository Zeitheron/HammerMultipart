package org.zeith.multipart.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.*;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PlacedPartConfiguration;

import java.util.Optional;

public interface IMultipartPlacerItem
{
	default void onPartPlacedBy(PartEntity part, Player player, ItemStack stack, @NotNull InteractionHand hand)
	{
		var sound = part.definition().getSoundType(part);
		
		player.level().playSound(player, part.container().pos(), sound.getPlaceSound(), SoundSource.BLOCKS,
				(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F
		);
		
		if(!player.isCreative())
			stack.shrink(1);
		
		player.swing(hand);
	}
	
	default Optional<InteractionResult> tryPlacePartFirst(@Nullable PartContainer sameBlockContainer, @Nullable PartContainer neigborContainer, Level level, BlockPos pos, Player player, ItemStack stack, BlockHitResult hit)
	{
		return Optional.empty();
	}
	
	Optional<PlacedPartConfiguration> getPlacement(Level level, BlockPos pos, Player player, ItemStack stack, BlockHitResult hit);
}