package org.zeith.multipart.api.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.*;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.multipart.blocks.BlockMultipartContainer;
import org.zeith.multipart.init.PartRegistries;

/**
 * A record class that represents part's position in the world.
 * <p>
 * {@link #pos} defines the coordinates of the block in the world.
 * {@link #placement} defines how a part is placed inside the container. If the position targets entire block, the {@link #placement} is null.
 */
public record PartPos(@NotNull BlockPos pos, @Nullable PartPlacement placement)
{
	public static final Codec<PartPos> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					BlockPos.CODEC.fieldOf("pos").forGetter(PartPos::pos),
					ExtraCodecs.lazyInitializedCodec(() -> PartRegistries.partPlacements().getCodec()).fieldOf("placement").forGetter(PartPos::placement)
			).apply(instance, PartPos::new)
	);
	
	/**
	 * Returns the part at this position as an instance of the given type, or null if not found or of a different type.
	 *
	 * @param level
	 * 		the level where the part is located
	 * @param type
	 * 		the class of the part to return
	 * @param <T>
	 * 		the type parameter of the part to return
	 *
	 * @return the part as an instance of T, or null
	 */
	@Nullable
	public <T> T getOfType(Level level, Class<T> type)
	{
		if(placement != null)
		{
			var pc = BlockMultipartContainer.pc(level, pos);
			if(pc != null)
				return Cast.cast(pc.getPartAt(placement), type);
		}
		return Cast.cast(level.getBlockEntity(pos), type);
	}
	
	public static PartPos ofBlock(BlockPos pos)
	{
		return new PartPos(pos, null);
	}
}