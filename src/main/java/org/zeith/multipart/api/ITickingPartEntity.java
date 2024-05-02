package org.zeith.multipart.api;

public interface ITickingPartEntity
		extends IPartEntity
{
	default void tickServer()
	{
		tickShared();
	}
	
	default void tickClient()
	{
		tickShared();
	}
	
	default void tickShared()
	{
	}
}