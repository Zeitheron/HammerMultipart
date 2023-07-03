package org.zeith.multipart.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.*;
import net.minecraft.util.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.zeith.hammerlib.util.colors.ColorHelper;
import org.zeith.multipart.api.*;
import org.zeith.multipart.client.model.IBakedMultipartModel;
import org.zeith.multipart.mixins.client.TextureSheetParticleAccessor;

import java.util.*;
import java.util.function.Supplier;

public class MultipartEffects
{
	static final Random random = new Random();
	
	public record TintedSprite(int rgb, TextureAtlasSprite sprite)
	{
		public TintedSprite(TextureAtlasSprite sprite)
		{
			this(0xFFFFFF, sprite);
		}
	}
	
	public static void spawnBreakFX(BlockPos pos, VoxelShape voxelshape, Supplier<TintedSprite> selector)
	{
		var state = WorldPartComponents.BLOCK.defaultBlockState();
		var mc = Minecraft.getInstance();
		
		double d0 = 0.25D;
		voxelshape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
		{
			double width = Math.min(1.0D, maxX - minX);
			double height = Math.min(1.0D, maxY - minY);
			double depth = Math.min(1.0D, maxZ - minZ);
			
			int stepX = Math.max(2, Mth.ceil(width / d0));
			int stepY = Math.max(2, Mth.ceil(height / d0));
			int stepZ = Math.max(2, Mth.ceil(depth / d0));
			
			for(int x = 0; x < stepX; ++x)
			{
				for(int y = 0; y < stepY; ++y)
				{
					for(int z = 0; z < stepZ; ++z)
					{
						double shiftX = (x + 0.5D) / (double) stepX;
						double shiftY = (y + 0.5D) / (double) stepY;
						double shiftZ = (z + 0.5D) / (double) stepZ;
						
						double offX = shiftX * width + minX;
						double offY = shiftY * height + minY;
						double offZ = shiftZ * depth + minZ;
						
						var fx = new TerrainParticle(mc.level,
								(double) pos.getX() + offX,
								(double) pos.getY() + offY,
								(double) pos.getZ() + offZ,
								shiftX - 0.5D, shiftY - 0.5D, shiftZ - 0.5D,
								state, pos
						);
						
						var sprite = selector.get();
						
						//noinspection ConstantValue
						if(sprite != null && fx instanceof TextureSheetParticleAccessor mixin)
						{
							int rgb = sprite.rgb;
							float red = ColorHelper.getRed(rgb) * 0.6F;
							float green = ColorHelper.getGreen(rgb) * 0.6F;
							float blue = ColorHelper.getBlue(rgb) * 0.6F;
							fx.setColor(red, green, blue);
							
							mixin.callSetSprite(sprite.sprite());
							mc.particleEngine.add(fx);
						}
					}
				}
			}
		});
	}
	
	public static void spawnBreakFX(PartEntity part)
	{
		var state = WorldPartComponents.BLOCK.defaultBlockState();
		var mc = Minecraft.getInstance();
		
		var model = mc.getBlockRenderer()
				.getBlockModelShaper()
				.getBlockModel(state);
		
		if(IClientPartDefinitionExtensions.of(part).addDestroyEffects(part, mc.particleEngine)) return;
		if(!(model instanceof IBakedMultipartModel bmm)) return;
		var bakedPart = bmm.getBakedPart(part);
		if(bakedPart == null) return;
		
		var pos = part.container().pos();
		VoxelShape voxelshape = part.getShape();
		double d0 = 0.25D;
		voxelshape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
		{
			double width = Math.min(1.0D, maxX - minX);
			double height = Math.min(1.0D, maxY - minY);
			double depth = Math.min(1.0D, maxZ - minZ);
			
			int stepX = Math.max(2, Mth.ceil(width / d0));
			int stepY = Math.max(2, Mth.ceil(height / d0));
			int stepZ = Math.max(2, Mth.ceil(depth / d0));
			
			for(int x = 0; x < stepX; ++x)
			{
				for(int y = 0; y < stepY; ++y)
				{
					for(int z = 0; z < stepZ; ++z)
					{
						double shiftX = (x + 0.5D) / (double) stepX;
						double shiftY = (y + 0.5D) / (double) stepY;
						double shiftZ = (z + 0.5D) / (double) stepZ;
						
						double offX = shiftX * width + minX;
						double offY = shiftY * height + minY;
						double offZ = shiftZ * depth + minZ;
						
						var fx = new TerrainParticle(mc.level,
								(double) pos.getX() + offX,
								(double) pos.getY() + offY,
								(double) pos.getZ() + offZ,
								shiftX - 0.5D, shiftY - 0.5D, shiftZ - 0.5D,
								state, pos
						);
						
						var sprites = bakedPart.getParticleIconsFor(part);
						
						//noinspection ConstantValue
						if(sprites != null && !sprites.isEmpty() &&
								fx instanceof TextureSheetParticleAccessor mixin)
						{
							int sprite = random.nextInt(sprites.size());
							
							int rgb = part.getTintLayerColor(-1 - sprite);
							float red = ColorHelper.getRed(rgb) * 0.6F;
							float green = ColorHelper.getGreen(rgb) * 0.6F;
							float blue = ColorHelper.getBlue(rgb) * 0.6F;
							fx.setColor(red, green, blue);
							
							mixin.callSetSprite(sprites.get(sprite));
							mc.particleEngine.add(fx);
						}
					}
				}
			}
		});
	}
	
	public static void spawnHitFX(PartEntity pe, BlockHitResult hit)
	{
		var state = WorldPartComponents.BLOCK.defaultBlockState();
		var mc = Minecraft.getInstance();
		
		var model = mc.getBlockRenderer()
				.getBlockModelShaper()
				.getBlockModel(state);
		
		if(IClientPartDefinitionExtensions.of(pe)
				.addHitEffects(hit, pe, mc.particleEngine))
			return;
		
		if(!(model instanceof IBakedMultipartModel bmm)) return;
		var bakedPart = bmm.getBakedPart(pe);
		if(bakedPart == null) return;
		
		var side = hit.getDirection();
		
		var pos = hit.getBlockPos();
		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();
		
		float f = 0.1F;
		
		AABB aabb = pe.getShape().bounds();
		
		double d0 = i + random.nextDouble() * (aabb.maxX - aabb.minX - 0.2F) + f + aabb.minX;
		double d1 = j + random.nextDouble() * (aabb.maxY - aabb.minY - 0.2F) + f + aabb.minY;
		double d2 = k + random.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2F) + f + aabb.minZ;
		
		if(side == Direction.DOWN)
			d1 = (double) j + aabb.minY - (double) 0.1F;
		
		if(side == Direction.UP)
			d1 = (double) j + aabb.maxY + (double) 0.1F;
		
		if(side == Direction.NORTH)
			d2 = (double) k + aabb.minZ - (double) 0.1F;
		
		if(side == Direction.SOUTH)
			d2 = (double) k + aabb.maxZ + (double) 0.1F;
		
		if(side == Direction.WEST)
			d0 = (double) i + aabb.minX - (double) 0.1F;
		
		if(side == Direction.EAST)
			d0 = (double) i + aabb.maxX + (double) 0.1F;
		
		var sprites = bakedPart.getParticleIconsFor(pe);
		
		var fx = new TerrainParticle(mc.level, d0, d1, d2, 0.0D, 0.0D, 0.0D, state, pos)
				.setPower(0.2F)
				.scale(0.6F);
		
		if(sprites != null && !sprites.isEmpty() &&
				fx instanceof TextureSheetParticleAccessor mixin)
		{
			int sprite = random.nextInt(sprites.size());
			
			int rgb = pe.getTintLayerColor(-1 - sprite);
			float red = ColorHelper.getRed(rgb) * 0.6F;
			float green = ColorHelper.getGreen(rgb) * 0.6F;
			float blue = ColorHelper.getBlue(rgb) * 0.6F;
			fx.setColor(red, green, blue);
			
			mixin.callSetSprite(sprites.get(sprite));
			mc.particleEngine.add(fx);
		}
	}
}