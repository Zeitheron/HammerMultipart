package org.zeith.multipart.api;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;
import org.zeith.api.registry.RegistryMapping;
import org.zeith.hammerlib.annotations.*;
import org.zeith.multipart.HammerMultipart;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.impl.placements.*;
import org.zeith.multipart.item.IMultipartPlacerItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PartRegistries
{
	private static final Map<Item, IMultipartPlacerItem> OVERRIDES = new ConcurrentHashMap<>();
	
	private static Supplier<IForgeRegistry<PartDefinition>> DEFINITION_REGISTRY;
	private static Supplier<IForgeRegistry<PartPlacement>> PLACEMENT_REGISTRY;
	
	
	
	@SubscribeEvent
	public static void registries(NewRegistryEvent e)
	{
		DEFINITION_REGISTRY = e.create(
				new RegistryBuilder<PartDefinition>()
						.setName(HammerMultipart.id("multipart/definitions"))
						.disableSync()
						.disableSaving(),
				reg -> RegistryMapping.report(PartDefinition.class, reg, false)
		);
		
		PLACEMENT_REGISTRY = e.create(
				new RegistryBuilder<PartPlacement>()
						.setName(HammerMultipart.id("multipart/placements"))
						.disableSync()
						.disableSaving(),
				reg -> RegistryMapping.report(PartPlacement.class, reg, false)
		);
	}
	
	public static void registerFallbackPartPlacer(Item item, IMultipartPlacerItem placer)
	{
		OVERRIDES.put(item, placer);
	}
	
	public static IMultipartPlacerItem getFallbackPlacer(Item item)
	{
		return OVERRIDES.get(item);
	}
	
	public static IForgeRegistry<PartDefinition> partDefinitions()
	{
		return DEFINITION_REGISTRY.get();
	}
	
	public static IForgeRegistry<PartPlacement> partPlacements()
	{
		return PLACEMENT_REGISTRY.get();
	}
}