package org.zeith.multipart.api.placement;

import org.zeith.multipart.api.*;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface IConfiguredPartPlacer
{
	@Nullable
	PartEntity create(PartContainer container, PartPlacement placement);
	
	static IConfiguredPartPlacer configuring(PartDefinition definition, UnaryOperator<PartEntity> configurator)
	{
		return (container, placement) -> configurator.apply(definition.createEntity(container, placement));
	}
}