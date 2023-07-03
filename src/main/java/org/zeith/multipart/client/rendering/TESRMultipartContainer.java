package org.zeith.multipart.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.zeith.hammerlib.client.render.tile.IBESR;
import org.zeith.multipart.api.PartEntity;
import org.zeith.multipart.tile.TileMultipartContainer;

import java.util.ConcurrentModificationException;

public class TESRMultipartContainer
		implements IBESR<TileMultipartContainer>
{
	@Override
	public void render(TileMultipartContainer entity, float partial, PoseStack matrix, MultiBufferSource buf, int lighting, int overlay)
	{
		try
		{
			for(var part : entity.container.renderers.values())
			{
				matrix.pushPose();
				((IPartRenderer) part).renderPart(partial, matrix, buf, lighting, overlay);
				matrix.popPose();
			}
		} catch(ConcurrentModificationException cme)
		{
			// This may happen, but shouldn't. We're being on the safe side.
		}
	}
}