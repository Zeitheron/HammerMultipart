package org.zeith.multipart.init;

import org.zeith.hammerlib.annotations.*;
import org.zeith.multipart.impl.parts.*;

@SimplyRegister
public interface PartDefinitionsHM
{
	@RegistryName("ladder")
	PartDefLadder LADDER_PART = new PartDefLadder();
	
	@RegistryName("torch")
	PartDefTorch TORCH_PART = new PartDefTorch();
	
	@RegistryName("soul_torch")
	PartDefSoulTorch SOUL_TORCH_PART = new PartDefSoulTorch();
	
	@RegistryName("chain")
	PartDefChain CHAIN_PART = new PartDefChain();
}