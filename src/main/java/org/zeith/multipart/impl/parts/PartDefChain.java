package org.zeith.multipart.impl.parts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.zeith.multipart.impl.parts.entities.PartEntityChain;
import org.zeith.multipart.init.PartDefinitionsHM;
import org.zeith.multipart.init.PartPlacementsHM;

import java.util.Optional;

public class PartDefChain
		extends PartDefinition
{
	public PartDefChain()
	{
		model.addParticleIcon("block/chain");
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
		return placement == PartPlacementsHM.CENTER;
	}
	
	@Override
	public PartEntity createEntity(PartContainer container, PartPlacement placement)
	{
		return new PartEntityChain(this, container, placement);
	}
	
	public record ChainPartPlacer(Direction.Axis placeAxis)
			implements IConfiguredPartPlacer
	{
		@Override
		public PartEntity create(PartContainer container, PartPlacement placement)
		{
			var def = new PartEntityChain(PartDefinitionsHM.CHAIN_PART, container, placement);
			def.axis = placeAxis;
			return def;
		}
	}
}