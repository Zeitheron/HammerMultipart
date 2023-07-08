package org.zeith.multipart.client.model;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.*;
import org.zeith.hammerlib.client.model.*;
import org.zeith.multipart.api.*;
import org.zeith.multipart.client.*;
import org.zeith.multipart.init.PartRegistries;

import java.util.*;
import java.util.function.*;

@LoadUnbakedGeometry(path = "multipart")
public class DynamicMultipartModel
		implements IUnbakedGeometry<DynamicMultipartModel>
{
	public DynamicMultipartModel(JsonObject json, JsonDeserializationContext context)
	{
	}
	
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
	{
		Map<PartDefinition, BakedPartDefinitionModel> bakedParts = new HashMap<>();
		for(PartDefinition def : PartRegistries.partDefinitions())
		{
			var partBaker = IPartModelBaker.of(def.getModel());
			if(partBaker != null)
			{
				var baked = partBaker.bake(context, baker, spriteGetter, modelState);
				if(baked == null) continue;
				bakedParts.put(def, baked);
			}
		}
		
		var defaultParticle = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation("block/stone")));
		
		return new Baked(bakedParts, defaultParticle);
	}
	
	@Override
	public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		return List.of();
	}
	
	public static UnaryOperator<BakedQuad> replaceTintIndex(int src, int dst)
	{
		return quad ->
		{
			if(quad.getTintIndex() == src)
				return new BakedQuad(quad.getVertices(), dst, quad.getDirection(), quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());
			return quad;
		};
	}
	
	private record Baked(Map<PartDefinition, BakedPartDefinitionModel> bakedParts, TextureAtlasSprite defaultParticle)
			implements IBakedModel, IBakedMultipartModel
	{
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType)
		{
			var ctr = data.get(PartContainer.CONTAINER_PROP);
			var hash = data.get(PartContainer.PART_HASH);
			if(ctr == null || hash == null || hash.longValue() == 0L) return List.of();
			
			// Perform tint layer refresh prior to transposing quads.
			ctr.recalcTintLayers(hash);
			
			List<BakedQuad> quads = new ArrayList<>();
			
			while(true)
			{
				try
				{
					quads.clear();
					for(var part : ctr.parts())
					{
						int[] indices = part.getTintIndices();
						Consumer<BakedQuad> quadConsumer = quad ->
						{
							int idx = quad.getTintIndex();
							if(idx >= 0 && idx < indices.length)
								quad = new BakedQuad(quad.getVertices(), indices[idx], quad.getDirection(), quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());
							quads.add(quad);
						};
						
						if(IClientPartDefinitionExtensions.of(part)
								.getQuads(part, side, rand, data, renderType, quadConsumer)) continue;
						var baked = getBakedPart(part);
						if(baked != null)
							baked.getQuads(part, side, rand, data, renderType, quadConsumer);
					}
					
					break;
				} catch(ConcurrentModificationException cme)
				{
					// This is a rare case, but if it happens, just try again.
					continue;
				}
			}
			
			return quads;
		}
		
		@Override
		public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data)
		{
			return ChunkRenderTypeSet.all();
		}
		
		@Override
		public boolean useAmbientOcclusion()
		{
			return true;
		}
		
		@Override
		public boolean isGui3d()
		{
			return false;
		}
		
		@Override
		public boolean usesBlockLight()
		{
			return true;
		}
		
		@Override
		public boolean isCustomRenderer()
		{
			return true;
		}
		
		@Override
		public TextureAtlasSprite getParticleIcon()
		{
			return defaultParticle;
		}
		
		@Override
		public BakedPartDefinitionModel getBakedPart(PartEntity entity)
		{
			return bakedParts.get(entity.definition());
		}
	}
}