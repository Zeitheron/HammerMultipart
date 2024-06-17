package org.zeith.multipart.net.props;

import net.minecraft.network.RegistryFriendlyByteBuf;
import org.zeith.hammerlib.net.properties.PropertyBase;
import org.zeith.hammerlib.util.java.DirectStorage;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.api.placement.PartPos;
import org.zeith.multipart.init.PartRegistries;

public class PropertyPartPos
		extends PropertyBase<PartPos>
{
	public PropertyPartPos(Class<PartPos> type)
	{
		super(type);
	}
	
	public PropertyPartPos(Class<PartPos> type, DirectStorage<PartPos> value)
	{
		super(type, value);
	}
	
	@Override
	public void write(RegistryFriendlyByteBuf buf)
	{
		var val = get();
		buf.writeBoolean(val != null);
		if(val != null)
		{
			buf.writeBlockPos(val.pos());
			
			var pl = val.placement();
			if(pl != null)
			{
				var key = PartRegistries.partPlacements().getKey(pl);
				buf.writeBoolean(key != null);
				
				if(key != null) buf.writeResourceLocation(key);
			} else
				buf.writeBoolean(false);
		}
	}
	
	@Override
	public void read(RegistryFriendlyByteBuf buf)
	{
		if(!buf.readBoolean())
		{
			set(null);
			return;
		}
		
		var pos = buf.readBlockPos();
		PartPlacement pl = null;
		
		if(buf.readBoolean())
			pl = PartRegistries.partPlacements().get(buf.readResourceLocation());
		
		set(new PartPos(pos, pl));
	}
}
