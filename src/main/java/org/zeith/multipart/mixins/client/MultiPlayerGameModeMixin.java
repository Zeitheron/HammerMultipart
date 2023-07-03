package org.zeith.multipart.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.tile.BlockMultipartContainer;

import java.util.Objects;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin
{
	@Shadow
	private BlockPos destroyBlockPos;
	
	@Shadow
	@Final
	private Minecraft minecraft;
	
	@Unique
	private PartPlacement hammerMultipart$destroyPartPlacement;
	
	@Inject(
			method = "sameDestroyTarget",
			at = @At("HEAD"),
			cancellable = true
	)
	private void sameDestroyTarget_Multipart(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
	{
		if(pos.equals(destroyBlockPos))
		{
			var pc = BlockMultipartContainer.pc(minecraft.level, pos);
			if(pc == null || !(minecraft.hitResult instanceof BlockHitResult bhr)) return;
			var pe = pc.selectPart(bhr.getLocation()).orElse(null);
			if(pe == null) return;
			if(!Objects.equals(pe.getKey(), hammerMultipart$destroyPartPlacement))
			{
				hammerMultipart$destroyPartPlacement = pe.getKey();
				cir.setReturnValue(false);
				cir.cancel();
			}
		}
	}
}