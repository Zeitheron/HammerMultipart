package org.zeith.multipart.api.placement;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.*;
import org.zeith.hammerlib.annotations.*;

import javax.annotation.Nullable;
import java.util.Set;

@SimplyRegister
public abstract class PartPlacement
{
	public abstract VoxelShape getExampleShape();
	
	public boolean canBePlacedAlongside(Set<PartPlacement> others)
	{
		return true;
	}
	
	@Nullable
	public Direction getDirection()
	{
		return null;
	}
}