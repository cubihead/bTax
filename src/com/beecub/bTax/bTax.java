package com.beecub.bTax;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.iConomy.*;
import java.util.logging.Logger;


public class bTax extends JavaPlugin {
	private final bTaxPlayerListener playerListener = new bTaxPlayerListener(this);
	public Logger log = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile pdfFile;
	public static Configuration conf;
	public iConomy iConomy;

	@SuppressWarnings("static-access")
	public void onEnable() {

		pdfFile = this.getDescription();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Highest, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" +  pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!");
		
		bConfigManager bConfigManager = new bConfigManager(this);
		bConfigManager.load();
		conf = bConfigManager.conf;
		
		//iConomy();
		bConfigManager.checkTax();
	}
	
	public void onDisable() {
		log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " disabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command c, String commandLabel, String[] args) {
        String command = c.getName().toLowerCase();
        if (command.equalsIgnoreCase("bTax")) {
            bConfigManager.reload();
            bChat.sendMessageToCommandSender(sender, "&6[" + pdfFile.getName() + "]" + " config reloaded");
            return true;
        }
        return false;
	}
	
	public void iConomy() {
	    Plugin iConomy = this.getServer().getPluginManager().getPlugin("iConomy");
	    
	    if ((iConomy != null) && (iConomy.isEnabled())) {
	        if (!iConomy.getDescription().getVersion().startsWith("5")) {
	            log.warning("[" + pdfFile.getName() + "]" + " You need iConomy 5! If you get errors, upgrade iConomy!");
	        }
	        iConomy = (iConomy)iConomy;
	        log.info("[" + pdfFile.getName() + "]" + "hooked into iConomy.");
	    }
	}
}
