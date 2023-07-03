package org.zeith.multipart.init;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import org.zeith.hammerlib.annotations.*;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.impl.placements.*;

import java.util.HashMap;
import java.util.function.Function;

@SimplyRegister
public interface PartPlacementsHM
{
	@RegistryName("center")
	PartPlacement CENTER = new PartPlacementOfCenter();
	
	@RegistryName("frame")
	PartPlacement FRAME = new PartPlacementOfFrame();
	
	@RegistryName("down")
	PartPlacement DOWN = new PartPlacementOfDirection(Direction.DOWN);
	
	@RegistryName("up")
	PartPlacement UP = new PartPlacementOfDirection(Direction.UP);
	
	@RegistryName("north")
	PartPlacement NORTH = new PartPlacementOfDirection(Direction.NORTH);
	
	@RegistryName("south")
	PartPlacement SOUTH = new PartPlacementOfDirection(Direction.SOUTH);
	
	@RegistryName("west")
	PartPlacement WEST = new PartPlacementOfDirection(Direction.WEST);
	
	@RegistryName("east")
	PartPlacement EAST = new PartPlacementOfDirection(Direction.EAST);
	
	
	Function<Direction, PartPlacement> SIDED_PLACEMENT = Util.make(new HashMap<Direction, PartPlacement>(), v ->
	{
		v.put(null, CENTER);
		v.put(Direction.DOWN, DOWN);
		v.put(Direction.UP, UP);
		v.put(Direction.NORTH, NORTH);
		v.put(Direction.SOUTH, SOUTH);
		v.put(Direction.WEST, WEST);
		v.put(Direction.EAST, EAST);
	})::get;
}