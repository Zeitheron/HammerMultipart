package org.zeith.multipart;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;

@Mod(HammerMultipart.MOD_ID)
public class HammerMultipart
{
	public static final String MOD_ID = "hammermultipart";
	
	public HammerMultipart()
	{
		LanguageAdapter.registerMod(MOD_ID);
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
}