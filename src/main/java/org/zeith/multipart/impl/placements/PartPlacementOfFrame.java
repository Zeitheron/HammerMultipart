package org.zeith.multipart.impl.placements;

import net.minecraft.world.phys.shapes.*;
import org.zeith.multipart.api.placement.PartPlacement;

public class PartPlacementOfFrame
		extends PartPlacement
{
	protected final VoxelShape sampleShape = Shapes.box(
			0, 0, 0,
			1, 1, 1
	);
	
	@Override
	public VoxelShape getExampleShape()
	{
		return sampleShape;
	}
}