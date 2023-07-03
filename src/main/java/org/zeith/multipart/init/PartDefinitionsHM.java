package org.zeith.multipart.init;

import org.zeith.hammerlib.annotations.*;
import org.zeith.multipart.ConfigsHM;
import org.zeith.multipart.impl.parts.*;

@SimplyRegister
public interface PartDefinitionsHM
{
	@RegistryName("ladder")
	@OnlyIf(owner = ConfigsHM.class, member = "registerLadder")
	PartDefLadder LADDER_PART = new PartDefLadder();
	
	@RegistryName("torch")
	@OnlyIf(owner = ConfigsHM.class, member = "registerTorch")
	PartDefTorch TORCH_PART = new PartDefTorch();
	
	@RegistryName("soul_torch")
	@OnlyIf(owner = ConfigsHM.class, member = "registerSoulTorch")
	PartDefSoulTorch SOUL_TORCH_PART = new PartDefSoulTorch();
	
	@RegistryName("chain")
	@OnlyIf(owner = ConfigsHM.class, member = "registerLadder")
	PartDefChain CHAIN_PART = new PartDefChain();
}