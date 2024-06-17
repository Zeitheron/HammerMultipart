package org.zeith.multipart.client;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.multipart.api.PartDefinition;
import org.zeith.multipart.api.PartEntity;
import org.zeith.multipart.client.model.BakedPartDefinitionModel;
import org.zeith.multipart.client.rendering.IPartRenderer;

import java.util.List;
import java.util.function.BiFunction;
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
	
	default void addLandingEffects(PartEntity part, VoxelShape shape, LivingEntity living, int numberOfParticles, AABB entityBb, Vec3 particlePos)
	{
		MultipartEffects.spawnLandingFX(
				part.container().pos(),
				shape,
				particlePos,
				numberOfParticles,
				MultipartEffects.defaultPartSpriteSelector(part)
		);
	}
	
	default void addRunningEffects(PartEntity part, VoxelShape shape, Entity living, AABB entityBb, Vec3 particlePos, Vec3 particleMotion)
	{
		MultipartEffects.spawnRunningFX(
				part.container().pos(),
				shape,
				particlePos,
				particleMotion,
				MultipartEffects.defaultPartSpriteSelector(part)
		);
	}
	
	default <T> List<T> gatherParticles(BakedPartDefinitionModel data, PartEntity part, BiFunction<ResourceLocation, Integer, T> spriteFactory)
	{
		return part.getParticleIcons(data.allParticles.keySet())
				.stream()
				.map(tex -> spriteFactory.apply(tex, part.getTintForParticle(tex)))
				.toList();
	}
	
	default boolean getQuads(PartEntity part, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType, Consumer<BakedQuad> addQuad)
	{
		return false;
	}
}
