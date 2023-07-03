package org.zeith.multipart.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.zeith.multipart.api.*;
import org.zeith.multipart.blocks.BlockMultipartContainer;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderMultipartHightlights
{
	@SubscribeEvent
	public static void renderBlockOutline(RenderHighlightEvent.Block e)
	{
		var hit = e.getTarget();
		var pos = hit.getBlockPos();
		var level = Minecraft.getInstance().level;
		var pc = BlockMultipartContainer.pc(level, pos);
		var player = Minecraft.getInstance().player;
		if(pc == null || !(level.getBlockState(pos).getBlock() instanceof BlockMultipartContainer mpctr)) return;
		IndexedVoxelShape shape = pc.selectPart(hit.getLocation())
				.map(p -> p.getValue().getSelectionShape(player, hit))
				.orElse(null);
		if(shape == null) return;
		mpctr.renderShape = shape.shape();
	}
	
	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class OnModBus
	{
		@SubscribeEvent
		public static void renderBlockOutline(RegisterColorHandlersEvent.Block e)
		{
			e.register((state, get, pos, tintLayer) ->
			{
				var pc = BlockMultipartContainer.pc(get, pos);
				if(pc != null)
					return pc.getColorForTintLayer(tintLayer);
				return 0xFFFFFF;
			}, WorldPartComponents.BLOCK);
		}
	}
}