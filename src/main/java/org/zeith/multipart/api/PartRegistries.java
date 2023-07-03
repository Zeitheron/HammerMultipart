package org.zeith.multipart.api;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.*;
import org.zeith.api.registry.RegistryMapping;
import org.zeith.hammerlib.annotations.*;
import org.zeith.multipart.HammerMultipart;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.impl.parts.*;
import org.zeith.multipart.impl.placements.*;
import org.zeith.multipart.item.IMultipartPlacerItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

@SimplyRegister
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PartRegistries
{
	private static final Map<Item, IMultipartPlacerItem> OVERRIDES = new ConcurrentHashMap<>();
	
	private static Supplier<IForgeRegistry<PartDefinition>> DEFINITION_REGISTRY;
	private static Supplier<IForgeRegistry<PartPlacement>> PLACEMENT_REGISTRY;
	
	public static final @RegistryName("center") PartPlacement CENTER = new PartPlacementOfCenter();
	public static final @RegistryName("frame") PartPlacement FRAME = new PartPlacementOfFrame();
	public static final @RegistryName("down") PartPlacement DOWN = new PartPlacementOfDirection(Direction.DOWN);
	public static final @RegistryName("up") PartPlacement UP = new PartPlacementOfDirection(Direction.UP);
	public static final @RegistryName("north") PartPlacement NORTH = new PartPlacementOfDirection(Direction.NORTH);
	public static final @RegistryName("south") PartPlacement SOUTH = new PartPlacementOfDirection(Direction.SOUTH);
	public static final @RegistryName("west") PartPlacement WEST = new PartPlacementOfDirection(Direction.WEST);
	public static final @RegistryName("east") PartPlacement EAST = new PartPlacementOfDirection(Direction.EAST);
	
	public static final Function<Direction, PartPlacement> SIDED_PLACEMENT = Util.make(new HashMap<Direction, PartPlacement>(), v ->
	{
		v.put(null, CENTER);
		v.put(Direction.DOWN, DOWN);
		v.put(Direction.UP, UP);
		v.put(Direction.NORTH, NORTH);
		v.put(Direction.SOUTH, SOUTH);
		v.put(Direction.WEST, WEST);
		v.put(Direction.EAST, EAST);
	})::get;
	
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
	
	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent e)
	{
		registerFallbackPartPlacer(Items.TORCH, PartDefTorch.TORCH_PART::getPlacement);
		registerFallbackPartPlacer(Items.SOUL_TORCH, PartDefSoulTorch.SOUL_TORCH_PART::getPlacement);
		registerFallbackPartPlacer(Items.LADDER, PartDefLadder.LADDER_PART::getPlacement);
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