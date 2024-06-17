package org.zeith.multipart.api.capability;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.function.Consumer;

/**
 * Subscribe to this even on your mod bus to let HammerMultipart provide any capability through {@link org.zeith.multipart.api.PartEntity}.
 * <p>
 * This event is reserved for any non-forge capabilities that you might want to access from parts. The vanilla capabilities (Items, Fluids and Energy) are registered by HammerMultipart for you.
 */
public class GatherPartCapabilitiesEvent
		extends Event
		implements IModBusEvent
{
	private final Consumer<BlockCapability<?, ?>> capabilities;
	
	public GatherPartCapabilitiesEvent(Consumer<BlockCapability<?, ?>> capabilities)
	{
		this.capabilities = capabilities;
	}
	
	public void register(BlockCapability<?, ?> capability)
	{
		capabilities.accept(capability);
	}
}