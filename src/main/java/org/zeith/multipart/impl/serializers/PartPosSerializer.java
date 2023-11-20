package org.zeith.multipart.impl.serializers;

import org.zeith.hammerlib.api.io.NBTSerializer;
import org.zeith.hammerlib.api.io.serializers.BaseCodecSerializer;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.multipart.api.placement.PartPos;

@NBTSerializer(PartPos.class)
public class PartPosSerializer
		extends BaseCodecSerializer<PartPos>
{
	public PartPosSerializer()
	{
		super(PartPos.CODEC, Cast.constant(null));
	}
}