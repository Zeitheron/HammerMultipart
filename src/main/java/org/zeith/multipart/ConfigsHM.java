package org.zeith.multipart;

import org.zeith.hammerlib.annotations.SetupConfigs;
import org.zeith.hammerlib.util.configured.*;

public class ConfigsHM
{
	private static ConfigFile config;
	
	public static boolean registerTorch = true;
	public static boolean registerRedstoneTorch = true;
	public static boolean registerSoulTorch = true;
	public static boolean registerChain = true;
	public static boolean registerLadder = true;
	
	@SetupConfigs
	public static void reloadCustom(ConfigFile cfgs)
	{
		config = cfgs;
		
		var gameplay = cfgs.setupCategory("Vanilla Parts")
				.withComment("Which of the built-in multiparts should be enabled?");
		{
			registerTorch = gameplay.getElement(ConfiguredLib.BOOLEAN, "Torch")
					.withDefault(true)
					.withComment("Should torches be a valid multipart target?")
					.getValue();
			registerRedstoneTorch = gameplay.getElement(ConfiguredLib.BOOLEAN, "Redstone Torch")
					.withDefault(true)
					.withComment("Should redstone torches be a valid multipart target?")
					.getValue();
			registerSoulTorch = gameplay.getElement(ConfiguredLib.BOOLEAN, "Soul Torch")
					.withDefault(true)
					.withComment("Should soul torches be a valid multipart target?")
					.getValue();
			registerChain = gameplay.getElement(ConfiguredLib.BOOLEAN, "Chain")
					.withDefault(true)
					.withComment("Should chains be a valid multipart target?")
					.getValue();
			registerLadder = gameplay.getElement(ConfiguredLib.BOOLEAN, "Ladder")
					.withDefault(true)
					.withComment("Should ladders be a valid multipart target?")
					.getValue();
		}
	}
	
	public static ConfigFile config()
	{
		return config;
	}
}