package org.zeith.multipart.api.placement;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Set;

public abstract class PartPlacement
{
	public abstract VoxelShape getExampleShape();
	
	public boolean canBePlacedAlongside(Set<PartPlacement> others)
	{
		return true;
	}
	
	public boolean isCompatibleWith(PartPlacement other)
	{
		return true;
	}
	
	@Nullable
	public Direction getDirection()
	{
		return null;
	}
}