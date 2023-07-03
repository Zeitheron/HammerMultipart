package org.zeith.multipart.api;

import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

public class IndexedVoxelShape
{
	protected final int index;
	protected final VoxelShape shape;
	
	public IndexedVoxelShape(int index, VoxelShape shape)
	{
		this.index = index;
		this.shape = shape;
	}
	
	public int index()
	{
		return index;
	}
	
	public VoxelShape shape()
	{
		return shape;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (IndexedVoxelShape) obj;
		return this.index == that.index &&
				Objects.equals(this.shape, that.shape);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(index, shape);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" +
				"index=" + index + ", " +
				"shape=" + shape + ']';
	}
	
}