package org.zeith.multipart.api.src;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.zeith.hammerlib.abstractions.sources.*;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPos;
import org.zeith.multipart.init.PartRegistries;

import java.util.Objects;

/**
 * The object source type, allowing addressing of placed {@link PartEntity} instances.
 */
public class PartSourceType
		implements IObjectSourceType
{
	@Override
	public IObjectSource<?> readSource(CompoundTag tag)
	{
		return new PartSource(tag);
	}
	
	public static PartSource of(PartEntity part)
	{
		return of(part.pos());
	}
	
	public static PartSource of(PartPos pos)
	{
		return new PartSource(pos);
	}
	
	public static class PartSource
			implements IObjectSource<PartEntity>
	{
		protected final PartPos pos;
		
		public PartSource(PartPos pos)
		{
			this.pos = pos;
		}
		
		public PartSource(CompoundTag tag)
		{
			this.pos = new PartPos(
					new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
					PartRegistries.partPlacements().getValue(ResourceLocation.tryParse(tag.getString("Placement")))
			);
		}
		
		@Override
		public CompoundTag writeSource()
		{
			CompoundTag tag = new CompoundTag();
			var pos = this.pos.pos();
			tag.putInt("x", pos.getX());
			tag.putInt("y", pos.getY());
			tag.putInt("z", pos.getZ());
			tag.putString("Placement", Objects.toString(PartRegistries.partPlacements().getKey(this.pos.placement())));
			return tag;
		}
		
		@Override
		public IObjectSourceType getType()
		{
			return WorldPartComponents.PART_SOURCE_TYPE;
		}
		
		@Override
		public Class<PartEntity> getBaseType()
		{
			return PartEntity.class;
		}
		
		@Override
		public PartEntity get(Level level)
		{
			return pos.getOfType(level, PartEntity.class);
		}
	}
}