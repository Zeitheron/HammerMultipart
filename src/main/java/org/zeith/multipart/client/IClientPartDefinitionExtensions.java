package org.zeith.multipart.client;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.*;
import org.zeith.multipart.api.*;
import org.zeith.multipart.client.rendering.IPartRenderer;

import java.util.function.Consumer;

public interface IClientPartDefinitionExtensions
{
	IClientPartDefinitionExtensions DEFAULT = new IClientPartDefinitionExtensions() {};
	
	static IClientPartDefinitionExtensions of(PartEntity entity)
	{
		return of(entity.definition());
	}
	
	static IClientPartDefinitionExtensions of(PartDefinition definition)
	{
		return definition.getRenderPropertiesInternal() instanceof IClientPartDefinitionExtensions e ? e : DEFAULT;
	}
	
	default IPartRenderer createRenderer(PartEntity part)
	{
		return null;
	}
	
	default boolean addHitEffects(BlockHitResult target, PartEntity part, ParticleEngine manager)
	{
		return false;
	}
	
	default boolean addDestroyEffects(PartEntity part, ParticleEngine manager)
	{
		return false;
	}
	
	default boolean getQuads(PartEntity part, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType, Consumer<BakedQuad> addQuad)
	{
		return false;
	}
}
