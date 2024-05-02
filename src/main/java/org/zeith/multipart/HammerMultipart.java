package org.zeith.multipart;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.slf4j.*;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import org.zeith.hammerlib.util.CommonMessages;

import static org.zeith.multipart.init.PartDefinitionsHM.*;
import static org.zeith.multipart.init.PartRegistries.registerFallbackPartPlacer;

@Mod(HammerMultipart.MOD_ID)
public class HammerMultipart
{
	public static final Logger LOG = LoggerFactory.getLogger(HammerMultipart.class);
	public static final String MOD_ID = "hammermultipart";
	
	public HammerMultipart()
	{
		CommonMessages.printMessageOnIllegalRedistribution(HammerMultipart.class,
				LogManager.getLogger(HammerMultipart.class), "HammerMultipart", "https://www.curseforge.com/minecraft/mc-mods/hammer-multipart");
		
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
		
		if(REDSTONE_TORCH_PART.isRegistered())
		{
			registerFallbackPartPlacer(Items.REDSTONE_TORCH, REDSTONE_TORCH_PART::getPlacement);
			LOG.info("Registered redstone torch multipart target.");
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