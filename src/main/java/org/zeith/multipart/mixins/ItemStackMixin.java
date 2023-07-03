package org.zeith.multipart.mixins;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.multipart.api.WorldPartComponents;

@Mixin(ItemStack.class)
public class ItemStackMixin
{
	@Inject(
			method = "useOn",
			at = @At("HEAD"),
			cancellable = true
	)
	public void onPlaceItemIntoWorld_PP(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir)
	{
		WorldPartComponents.BLOCK.useItem(context).ifPresent(cir::setReturnValue);
	}
}