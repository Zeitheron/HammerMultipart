package org.zeith.multipart.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.*;
import org.zeith.multipart.api.placement.*;
import org.zeith.multipart.client.IClientPartDefinitionExtensions;

import java.util.Optional;
import java.util.function.*;

public abstract class PartDefinition
{
	public float destroySpeed = 1F;
	protected SoundType soundType = SoundType.STONE;
	protected final PartDefinitionModel model = createModel();
	protected int tintIndexCount = 0;
	
	protected Supplier<ItemStack> cloneItem = () -> ItemStack.EMPTY;
	
	public PartDefinition()
	{
		initClient();
	}
	
	public SoundType getSoundType(PartEntity entity)
	{
		return soundType;
	}
	
	public boolean canSurviveInWater(PartEntity part)
	{
		return true;
	}
	
	protected PartDefinitionModel createModel()
	{
		return new PartDefinitionModel(this);
	}
	
	public PartDefinitionModel getModel()
	{
		return model;
	}
	
	public int getTintIndexCount()
	{
		return tintIndexCount;
	}
	
	public Optional<PlacedPartConfiguration> convertBlockToPart(Level level, BlockPos pos, BlockState state)
	{
		return Optional.empty();
	}
	
	public Optional<PartEntity> tryMergeWith(PartContainer container, PartPlacement placement, PartEntity otherEntity)
	{
		return Optional.empty();
	}
	
	public boolean canPlaceAt(PartContainer container, PartPlacement placement)
	{
		return true;
	}
	
	public abstract PartEntity createEntity(PartContainer container, PartPlacement placement);
	
	
	public ItemStack getCloneItem()
	{
		return cloneItem.get();
	}
	
	private Object renderProperties;
	
	/*
	   DO NOT CALL
	   Call IClientPartDefinitionExtensions.get instead
	 */
	public Object getRenderPropertiesInternal()
	{
		return renderProperties;
	}
	
	private void initClient()
	{
		// Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
		if(FMLEnvironment.dist == Dist.CLIENT &&
				!FMLLoader.getLaunchHandler().isData())
		{
			initializeClient(properties ->
			{
				if(properties == this)
					throw new IllegalStateException("Don't extend IClientPartDefinitionExtensions in your part, use an anonymous class instead.");
				this.renderProperties = properties;
			});
		}
	}
	
	public void initializeClient(Consumer<IClientPartDefinitionExtensions> consumer)
	{
	}
}