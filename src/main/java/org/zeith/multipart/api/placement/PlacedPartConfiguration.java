package org.zeith.multipart.api.placement;

import org.zeith.multipart.api.PartDefinition;

public record PlacedPartConfiguration(
		PartDefinition base,
		IConfiguredPartPlacer placer,
		PartPlacement placement
)
{
	public PlacedPartConfiguration(PartDefinition base, PartPlacement placement)
	{
		this(base, base::createEntity, placement);
	}
}