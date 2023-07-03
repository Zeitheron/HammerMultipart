package org.zeith.multipart;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;

import static org.zeith.multipart.api.PartRegistries.registerFallbackPartPlacer;
import static org.zeith.multipart.init.PartDefinitionsHM.*;

@Mod(HammerMultipart.MOD_ID)
public class HammerMultipart
{
	public static final String MOD_ID = "hammermultipart";
	
	public HammerMultipart()
	{
		var modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		LanguageAdapter.registerMod(MOD_ID);
		modBus.addListener(this::commonSetup);
	}
	
	private void commonSetup(FMLCommonSetupEvent e)
	{
		registerFallbackPartPlacer(Items.TORCH, TORCH_PART::getPlacement);
		registerFallbackPartPlacer(Items.SOUL_TORCH, SOUL_TORCH_PART::getPlacement);
		registerFallbackPartPlacer(Items.LADDER, LADDER_PART::getPlacement);
		registerFallbackPartPlacer(Items.CHAIN, CHAIN_PART::getPlacement);
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
}