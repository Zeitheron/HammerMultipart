package org.zeith.multipart.impl.placements;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.zeith.multipart.api.placement.PartPlacement;

public class PartPlacementOfCenter
		extends PartPlacement
{
	protected final VoxelShape sampleShape = Shapes.box(
			5 / 16F, 5 / 16F, 5 / 16F,
			11 / 16F, 11 / 16F, 11 / 16F
	);
	
	@Override
	public VoxelShape getExampleShape()
	{
		return sampleShape;
	}
	
	@Override
	public String toString()
	{
		return "PartPlacementOfCenter{" +
			   '}';
	}
}