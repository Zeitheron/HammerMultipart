package org.zeith.multipart.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.zeith.api.registry.RegistryMapping;
import org.zeith.multipart.HammerMultipart;
import org.zeith.multipart.api.PartDefinition;
import org.zeith.multipart.api.WorldPartComponents;
import org.zeith.multipart.api.capability.GatherPartCapabilitiesEvent;
import org.zeith.multipart.api.item.IMultipartPlacerItem;
import org.zeith.multipart.api.placement.PartPlacement;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class PartRegistries
{
	private static final Map<Item, IMultipartPlacerItem> OVERRIDES = new ConcurrentHashMap<>();
	
	private static Registry<PartDefinition> DEFINITION_REGISTRY;
	private static Registry<PartPlacement> PLACEMENT_REGISTRY;
	
	@SubscribeEvent
	public static void registries(NewRegistryEvent e)
	{
		RegistryMapping.report(PartDefinition.class, DEFINITION_REGISTRY = e.create(new RegistryBuilder<>(Keys.PART_DEFINITION).sync(false)), false);
		RegistryMapping.report(PartPlacement.class, PLACEMENT_REGISTRY = e.create(new RegistryBuilder<>(Keys.PART_PLACEMENT).sync(false)), false);
	}
	
	@SubscribeEvent
	public static void partCapabilities(GatherPartCapabilitiesEvent e)
	{
		e.register(Capabilities.ItemHandler.BLOCK);
		e.register(Capabilities.EnergyStorage.BLOCK);
		e.register(Capabilities.FluidHandler.BLOCK);
	}
	
	@SubscribeEvent
	public static void capabilities(RegisterCapabilitiesEvent e)
	{
		Set<BlockCapability<?, ?>> allCaps = new HashSet<>();
		GatherPartCapabilitiesEvent rre = new GatherPartCapabilitiesEvent(allCaps::add);
		var ac = ModLoadingContext.get().getActiveContainer();
		ModList.get().forEachModInOrder(mc ->
		{
			var bus = mc.getEventBus();
			if(bus == null) return;
			ModLoadingContext.get().setActiveContainer(mc);
			bus.post(rre);
		});
		ModLoadingContext.get().setActiveContainer(ac);
		allCaps.forEach(cap -> addCapability(e, cap));
	}
	
	private static <T, C> void addCapability(RegisterCapabilitiesEvent e, BlockCapability<T, C> cap)
	{
		e.registerBlockEntity(cap, WorldPartComponents.TILE_TYPE, (ctr, ctx) -> ctr.findCapability(cap, ctx).orElse(null));
	}
	
	public static void registerFallbackPartPlacer(Item item, IMultipartPlacerItem placer)
	{
		OVERRIDES.put(item, placer);
	}
	
	public static IMultipartPlacerItem getFallbackPlacer(Item item)
	{
		return OVERRIDES.get(item);
	}
	
	public static Registry<PartDefinition> partDefinitions()
	{
		return DEFINITION_REGISTRY;
	}
	
	public static Registry<PartPlacement> partPlacements()
	{
		return PLACEMENT_REGISTRY;
	}
	
	public static final class Keys
	{
		public static final ResourceKey<? extends Registry<PartDefinition>> PART_DEFINITION = ResourceKey.createRegistryKey(HammerMultipart.id("multipart/definition"));
		public static final ResourceKey<? extends Registry<PartPlacement>> PART_PLACEMENT = ResourceKey.createRegistryKey(HammerMultipart.id("multipart/placement"));
	}
}