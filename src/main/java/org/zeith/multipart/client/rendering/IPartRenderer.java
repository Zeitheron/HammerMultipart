package org.zeith.multipart.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IPartRenderer
{
	void renderPart(float partial, PoseStack matrix, MultiBufferSource buf, int lighting, int overlay);
}