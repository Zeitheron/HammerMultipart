package org.zeith.multipart.api;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeith.hammerlib.util.mcf.Resources;
import org.zeith.multipart.client.model.BakedPartDefinitionModel;
import org.zeith.multipart.client.model.IPartModelBaker;
import org.zeith.multipart.init.PartRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PartDefinitionModel
{
	public static final Logger LOG = LoggerFactory.getLogger(PartDefinitionModel.class);
	protected final List<ResourceLocation> extraIcons = new ArrayList<>();
	protected final List<ResourceLocation> partModels = new ArrayList<>();
	
	protected final PartDefinition definition;
	
	public PartDefinitionModel(PartDefinition definition)
	{
		this.definition = definition;
		initClient();
	}
	
	public PartDefinitionModel addParticleIcon(String path)
	{
		return addParticleIcon(Resources.location(path));
	}
	
	public PartDefinitionModel addParticleIcon(String namespace, String path)
	{
		return addParticleIcon(Resources.location(namespace, path));
	}
	
	public PartDefinitionModel addParticleIcon(ResourceLocation icon)
	{
		if(!extraIcons.contains(icon))
			extraIcons.add(icon);
		return this;
	}
	
	public PartDefinitionModel addSubmodel(ResourceLocation model)
	{
		if(!partModels.contains(model))
			partModels.add(model);
		return this;
	}
	
	public List<ResourceLocation> getExtraIcons()
	{
		return extraIcons;
	}
	
	public List<ResourceLocation> getExtraModels()
	{
		return partModels;
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
		   !DatagenModLoader.isRunningDataGen())
		{
			initializeClient(properties ->
			{
				if(properties == this)
					throw new IllegalStateException("Don't extend IPartModelBaker in your part, use an anonymous class instead.");
				this.renderProperties = properties;
			});
		}
	}
	
	protected void initializeClient(Consumer<IPartModelBaker> consumer)
	{
		consumer.accept(new DefaultPartModelBaker());
	}
	
	protected class DefaultPartModelBaker
			implements IPartModelBaker
	{
		BakedPartDefinitionModel mod;
		
		@Override
		public BakedPartDefinitionModel get()
		{
			return mod;
		}
		
		@Override
		public BakedPartDefinitionModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState)
		{
			var particleIcons = getExtraIcons()
					.stream()
					.map(path -> Map.entry(path, spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, path))))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			var partModels = getExtraModels()
					.stream()
					.map(path ->
					{
						var mod = baker.bake(path, BlockModelRotation.X0_Y0, spriteGetter);
						if(mod == null)
						{
							LOG.warn("Unable to load model {} for part definition {}", path, PartRegistries.partDefinitions()
									.getKey(definition));
							return null;
						}
						return Map.entry(path, mod);
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			return mod = new BakedPartDefinitionModel(particleIcons, partModels);
		}
	}
}