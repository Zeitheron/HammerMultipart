package org.zeith.multipart;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.*;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;

import static org.zeith.multipart.init.PartRegistries.registerFallbackPartPlacer;
import static org.zeith.multipart.init.PartDefinitionsHM.*;

@Mod(HammerMultipart.MOD_ID)
public class HammerMultipart
{
	public static final Logger LOG = LoggerFactory.getLogger(HammerMultipart.class);
	public static final String MOD_ID = "hammermultipart";
	
	public HammerMultipart()
	{
		var modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		LanguageAdapter.registerMod(MOD_ID);
		modBus.addListener(this::commonSetup);
	}
	
	private void commonSetup(FMLCommonSetupEvent e)
	{
		if(TORCH_PART.isRegistered())
		{
			registerFallbackPartPlacer(Items.TORCH, TORCH_PART::getPlacement);
			LOG.info("Registered torch multipart target.");
		}
		
		if(SOUL_TORCH_PART.isRegistered())
		{
			registerFallbackPartPlacer(Items.SOUL_TORCH, SOUL_TORCH_PART::getPlacement);
			LOG.info("Registered soul torch multipart target.");
		}
		
		if(LADDER_PART.isRegistered())
		{
			registerFallbackPartPlacer(Items.LADDER, LADDER_PART::getPlacement);
			LOG.info("Registered ladder multipart target.");
		}
		
		if(CHAIN_PART.isRegistered())
		{
			registerFallbackPartPlacer(Items.CHAIN, CHAIN_PART::getPlacement);
			LOG.info("Registered chain multipart target.");
		}
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
}