package org.zeith.multipart.api;

public interface IPartEntity
{
	@Deprecated(forRemoval = true)
	boolean syncDirty();
	
	@Deprecated(forRemoval = true)
	void markSynced();
	
	int getLightEmission();
	
	boolean isShapeDirty();
}