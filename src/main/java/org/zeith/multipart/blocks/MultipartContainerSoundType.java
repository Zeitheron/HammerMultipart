package org.zeith.multipart.blocks;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import org.zeith.multipart.api.PartEntity;
import org.zeith.multipart.api.WorldPartComponents;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MultipartContainerSoundType
		extends DeferredSoundType
{
	protected final Supplier<Set<PartEntity>> collidingParts;
	protected final Supplier<PartEntity> targetedPart;
	
	// Args from SoundType.EMPTY
	public MultipartContainerSoundType(Supplier<Set<PartEntity>> collidingParts, Supplier<PartEntity> targetedPart)
	{
		super(1.0F, 1.0F,
				() -> Optional.ofNullable(targetedPart.get()).map(PartEntity::getSoundType).map(SoundType::getBreakSound).orElse(SoundEvents.EMPTY), // break
				() -> findMatching(collidingParts, SoundType::getStepSound), // step
				() -> SoundEvents.EMPTY, // place
				() -> Optional.ofNullable(targetedPart.get()).map(PartEntity::getSoundType).map(SoundType::getHitSound).orElse(SoundEvents.EMPTY), // hit
				() -> findMatching(collidingParts, SoundType::getFallSound) // fall
		);
		this.collidingParts = collidingParts;
		this.targetedPart = targetedPart;
	}
	
	public static SoundEvent findMatching(Supplier<Set<PartEntity>> parts, Function<SoundType, SoundEvent> applier)
	{
		return parts.get()
				.stream()
				.map(PartEntity::getSoundType)
				.map(applier)
				.filter(e -> !Objects.equals(e.getLocation(), SoundEvents.EMPTY.getLocation()))
				.findFirst()
				.orElse(SoundEvents.EMPTY);
	}
	
	public static SoundType create(BlockGetter level, BlockPos pos, Entity ent)
	{
		var pc = WorldPartComponents.getContainer(level, pos);
		if(!(ent instanceof LivingEntity le)) return SoundType.EMPTY;
		return new MultipartContainerSoundType(
				Suppliers.memoizeWithExpiration(() ->
				{
					var eb = le.getBoundingBox().inflate(0.001);
					return pc.parts().stream().filter(pe ->
					{
						var cs = pe.getCollisionShape();
						return cs != null && !cs.isEmpty() && eb.intersects(cs.bounds().move(pos));
					}).collect(Collectors.toSet());
				}, 50L, TimeUnit.MILLISECONDS),
				Suppliers.memoizeWithExpiration(() ->
				{
					var hit = BlockMultipartContainer.getEntityPOVHitResult(level, le, ClipContext.Fluid.NONE);
					return pc.selectPart(hit.getLocation()).map(Map.Entry::getValue).orElse(null);
				}, 50L, TimeUnit.MILLISECONDS)
		);
	}
}