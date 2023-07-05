package org.zeith.multipart.impl.parts;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.*;
import org.zeith.multipart.impl.parts.entities.PartEntityChain;
import org.zeith.multipart.init.*;

import java.util.*;

public class PartDefChain
		extends PartDefinition
{
	public PartDefChain()
	{
		model.addParticleIcon(new ResourceLocation("block/chain"));
		soundType = SoundType.CHAIN;
		destroySpeed = 5.0F;
		cloneItem = Items.CHAIN::getDefaultInstance;
	}
	
	public Optional<PlacedPartConfiguration> getPlacement(Level level, BlockPos pos, Player player, ItemStack stack, BlockHitResult hit)
	{
		Direction towards = hit.getDirection().getOpposite();
		return Optional.of(new PlacedPartConfiguration(this, new ChainPartPlacer(towards.getAxis()), PartPlacementsHM.CENTER));
	}
	
	@Override
	public Optional<PlacedPartConfiguration> convertBlockToPart(Level level, BlockPos pos, BlockState state)
	{
		if(state.is(Blocks.CHAIN))
			return Optional.of(new PlacedPartConfiguration(this, new ChainPartPlacer(state.getValue(ChainBlock.AXIS)), PartPlacementsHM.CENTER));
		return Optional.empty();
	}
	
	@Override
	public boolean canPlaceAt(PartContainer container, @Nullable IConfiguredPartPlacer placer, PartPlacement placement)
	{
		if(placement != PartPlacementsHM.CENTER) return false;
		
		if(placer instanceof ChainPartPlacer c)
			return container.parts().stream()
					.map(PartEntity::placement)
					.map(PartPlacement::getDirection)
					.filter(Objects::nonNull)
					.map(Direction::getAxis)
					.noneMatch(a -> a == c.placeAxis);
		
		return false;
	}
	
	@Override
	public PartEntity createEntity(PartContainer container, PartPlacement placement)
	{
		return new PartEntityChain(this, container, placement);
	}
	
	public record ChainPartPlacer(Direction.Axis placeAxis)
			implements IConfiguredPartPlacer
	{
		
		@Nullable
		@Override
		public PartEntity create(PartContainer container, PartPlacement placement)
		{
			var def = new PartEntityChain(PartDefinitionsHM.CHAIN_PART, container, placement);
			def.axis = placeAxis;
			return def;
		}
	}
}