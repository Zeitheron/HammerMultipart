package org.zeith.multipart.impl.placements;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.*;
import org.zeith.multipart.api.placement.PartPlacement;

public class PartPlacementOfDirection
		extends PartPlacement
{
	protected final Direction direction;
	protected final VoxelShape sampleShape;
	
	public PartPlacementOfDirection(Direction direction)
	{
		this.direction = direction;
		
		float minAxial = 5 / 16F, maxAxial = 11 / 16F;
		float elevation = 3 / 16F, invElevation = 1 - elevation;
		
		sampleShape = switch(direction)
		{
			case DOWN -> Shapes.create(minAxial, 0, minAxial, maxAxial, elevation, maxAxial);
			case UP -> Shapes.create(minAxial, invElevation, minAxial, maxAxial, 1, maxAxial);
			case NORTH -> Shapes.create(minAxial, minAxial, 0, maxAxial, maxAxial, elevation);
			case SOUTH -> Shapes.create(minAxial, minAxial, invElevation, maxAxial, maxAxial, 1);
			case WEST -> Shapes.create(0, minAxial, minAxial, elevation, maxAxial, maxAxial);
			case EAST -> Shapes.create(invElevation, minAxial, minAxial, 1, maxAxial, maxAxial);
		};
	}
	
	@Override
	public Direction getDirection()
	{
		return direction;
	}
	
	@Override
	public VoxelShape getExampleShape()
	{
		return sampleShape;
	}
	
	@Override
	public String toString()
	{
		return "PartPlacementOfDirection{" +
			   "direction=" + direction +
			   '}';
	}
}