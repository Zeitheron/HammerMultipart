package org.zeith.multipart.api;

import net.minecraft.world.entity.player.Player;

public interface IPartContainerTile
{
	PartContainer getContainer();
	
	void syncContainer(boolean force);
	
	void openContainer(Player player);
}