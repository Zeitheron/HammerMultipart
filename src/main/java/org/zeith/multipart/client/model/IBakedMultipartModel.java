package org.zeith.multipart.client.model;

import org.zeith.multipart.api.PartEntity;

public interface IBakedMultipartModel
{
	BakedPartDefinitionModel getBakedPart(PartEntity entity);
}