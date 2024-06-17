package org.zeith.multipart.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.zeith.multipart.api.PartDefinitionModel;

import java.util.function.Function;

public interface IPartModelBaker
{
	static IPartModelBaker of(PartDefinitionModel model)
	{
		return model.getRenderPropertiesInternal() instanceof IPartModelBaker baker ? baker : null;
	}
	
	BakedPartDefinitionModel get();
	
	BakedPartDefinitionModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState);
}