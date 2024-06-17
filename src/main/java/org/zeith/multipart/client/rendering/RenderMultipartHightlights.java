package org.zeith.multipart.client.rendering;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.zeith.multipart.api.IndexedVoxelShape;
import org.zeith.multipart.api.WorldPartComponents;
import org.zeith.multipart.blocks.BlockMultipartContainer;

@EventBusSubscriber(Dist.CLIENT)
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
	
	@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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