package com.beecub.bTax;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;


public class bTaxPlayerListener extends PlayerListener {
	@SuppressWarnings("unused")
	private final bTax plugin;

	public bTaxPlayerListener(bTax instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        bConfigManager.checkTaxPlayer(player);
	}
}