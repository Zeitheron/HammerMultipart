package org.zeith.multipart.client.model;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.multipart.api.PartEntity;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class BakedPartDefinitionModel
{
	public final Map<ResourceLocation, TextureAtlasSprite> allParticles;
	public final Map<ResourceLocation, BakedModel> allModels;
	
	public BakedPartDefinitionModel(
			Map<ResourceLocation, TextureAtlasSprite> allParticles,
			Map<ResourceLocation, BakedModel> allModels
	)
	{
		this.allParticles = allParticles;
		this.allModels = allModels;
	}
	
	public List<TextureAtlasSprite> getParticleIconsFor(PartEntity entity)
	{
		return entity.getParticleIcons(allParticles.keySet())
				.stream()
				.map(allParticles::get)
				.filter(Objects::nonNull)
				.toList();
	}
	
	public void getQuads(PartEntity part, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType, Consumer<BakedQuad> quadList)
	{
		var mm = Minecraft.getInstance().getModelManager();
		
		var renderState = part.getRenderState();
		if(renderState != null)
		{
			var model = mm.getBlockModelShaper().getBlockModel(renderState);
			if(renderType == null || model.getRenderTypes(renderState, rand, data).contains(renderType))
				model.getQuads(renderState, side, rand, data, renderType).forEach(quadList);
		}
		
		renderState = Blocks.AIR.defaultBlockState();
		for(var mloc : part.getRenderModels())
		{
			var model = getModel(mloc);
			if(renderType == null || model.getRenderTypes(renderState, rand, data).contains(renderType))
				model.getQuads(renderState, side, rand, data, renderType).forEach(quadList);
		}
	}
	
	public TextureAtlasSprite getTexture(ResourceLocation texture)
	{
		var v = allParticles.get(texture);
		if(v == null)
			return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
					.apply(MissingTextureAtlasSprite.getLocation());
		return v;
	}
	
	public BakedModel getModel(ModelResourceLocation model)
	{
		var mod = allModels.get(model);
		if(mod == null) return Minecraft.getInstance().getModelManager().getModel(model);
		return mod;
	}
}