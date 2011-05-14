package com.beecub.bTax;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import util.bChat;

import com.iConomy.system.Holdings;

public class bConfigManager {
	
	protected static bTax bTax;
    protected static Configuration conf;
    protected static Configuration uconf;
    protected File confFile;
    static long interval;
    static double amount;
    static String message;
    static String payee;
    static String currencyname;
    static boolean amountispercentage;
    static boolean exceptionop;
    static boolean onlineonly;
    static List<String> exceptionusers = new LinkedList<String>();
	
	@SuppressWarnings("static-access")
    public bConfigManager(bTax bTax) {
    	this.bTax = bTax;

    	File f = new File(bTax.getDataFolder(), "config.yml");
    	conf = null;

        if (f.exists())
        {
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	this.confFile = new File(bTax.getDataFolder(), "config.yml");
            this.conf = new Configuration(confFile);
            
            // tax
            conf.setProperty("tax.interval", 60);
            conf.setProperty("tax.amount", 1.00);
            conf.setProperty("tax.amountispercentage", true);
            conf.setProperty("tax.message", "&6Its time to pay your tax: &amount &currencyname");
            conf.setProperty("tax.payee", "none");
            conf.setProperty("tax.currencyname", "MineCoins");
            conf.setProperty("tax.onlineonly", true);
            // options
            List<String> bsp2 = new LinkedList<String>();
            bsp2.add("beecub");
            bsp2.add("anotherplayer");
            conf.setProperty("option.exception.users", bsp2);
            conf.setProperty("option.exception.op", true);
            conf.save();
        }
        f = new File(bTax.getDataFolder(), "users.yml");
        
        if (f.exists())
        {
            uconf = new Configuration(f);
            uconf.load();
        }
        else {
            this.confFile = new File(bTax.getDataFolder(), "users.yml");
            this.uconf = new Configuration(confFile);
            uconf.save();
        }
    }
    
	static void load() {
    	conf.load();
    	interval = conf.getInt("tax.interval", 60);
    	amount = conf.getDouble("tax.amount", 1.00);
    	message = conf.getString("tax.message", "&6Its time to pay your tax: &amount &currencyname");
    	payee = conf.getString("tax.payee", "none");
    	currencyname = conf.getString("tax.currencyname", "MineCoins");
    	amountispercentage = conf.getBoolean("tax.amountispercentage", true);
    	onlineonly = conf.getBoolean("tax.onlineonly", true);
    	exceptionop = conf.getBoolean("option.exception.op", true);
    	exceptionusers = conf.getStringList("option.exception.users", null);
    	
    	uconf.load();
    }
	
	static void reload() {
		load();
	}
	
	static void checkTaxAll() {
	    List<String> players = new LinkedList<String>();
	    players = uconf.getKeys("users");
	    String playername;
	    String currTime = getcurrTime();
	    for(int i = 0; i < players.size(); i++) {
	        playername = players.get(i);
	        if(uconf.getBoolean(("users." + playername + ".hastopay"), true)) {
	            checkTaxPlayer(playername, currTime);
	        }
	    }
	}
	
	static void checkTaxOnline() {
	    Player[] players = bTax.getServer().getOnlinePlayers();
        String currTime = getcurrTime();
        for(Player player : players) {
            checkTaxPlayer(player, currTime);
        }
	}
	
   static boolean checkTaxPlayer(Player player, String currTime) {
       
       boolean check = true;
       String playername = player.getName();
       
       // check check for tax is needed
       if(exceptionop = true) {
           if(player.isOp()) {
               check = false;
           }
       }
       if(exceptionusers.contains(playername)) {
           check = false;
       }
       if(bTax.permissions == true) {
           if(bTax.Permissions.permission(player, "bTax.exception")) {
               check = false;
           }
       }
       
       // check for tax
       if(check) {
           if(checkTaxPlayer(playername, currTime)) {
               bChat.sendMessageToPlayer(player, message);
               return true;
           }
           uconf.setProperty("users." + playername + ".hastopay", true);
           uconf.save();
           return false;
       }
       else {
           uconf.setProperty("users." + playername + ".hastopay", false);
           uconf.save();
           return false;
       }
    }
	
	static boolean checkTaxPlayer(String playername, String currTime) {
        if(interval > 0) {
            Date lastTime = getTime(playername);
            if(lastTime == null) {
                setTime(playername, currTime);
            }
            lastTime = getTime(playername);
            if(lastTime != null) {
                Calendar calcurrTime = Calendar.getInstance();
                calcurrTime.setTime(getCurrTime());
                Calendar callastTime = Calendar.getInstance();
                callastTime.setTime(lastTime);
                long secondsBetween = secondsBetween(callastTime, calcurrTime);
                if(secondsBetween > interval) {
                    if(withdrawTax(playername, currTime)) {
                        return true;
                    }
                }
            }
        }
        return false;
	}
	
	@SuppressWarnings("static-access")
    static boolean withdrawTax(String accountname, String currTime) {
	    
	    Holdings balance = bTax.iConomy.getAccount(accountname).getHoldings();
	    if(amountispercentage) {
	        double damount = balance.balance();
	        damount = damount / 100 * amount;
	        amount = damount;
	    }
	    if(amount > 0.0) {
            balance.subtract(amount);            
            
    	    message = message.replaceAll("&amount", "&e" + Double.toString(amount));
    	    message = message.replaceAll("&currencyname", "&e" + currencyname);
    	    
    	    if(payee != null && payee != "") {
    	        Holdings payeebalance = bTax.iConomy.getAccount(payee).getHoldings();
    	        payeebalance.add(amount);
    	    }
    	    
    	    setTime(accountname, currTime);
    	    return true;
	    }
	    return false;
	}
    
    static String getcurrTime() {
        String currTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        return currTime;
    }
    
    static Date getCurrTime() {
        String currTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        Date time = null;
        
        try {
            time = sdf.parse(currTime);
            return time;
         } catch(ParseException e) {
            return null;
         } 
    }
    
    static void setTime(String playername, String currTime) {
        uconf.setProperty("users." + playername + ".lastpay", currTime);
        uconf.save();
    }
    
    static Date getTime(String playername) {
        String confTime = "";
        confTime = uconf.getString("users." + playername + ".lastpay", null);
        
        if(confTime != null && confTime != "") {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date lastDate = null;
       
            try {
               lastDate = sdf.parse(confTime);
               return lastDate;
            } catch(ParseException e) {
               return null;
            }
        }
        return null;              
    }
    
    public static long secondsBetween(Calendar startDate, Calendar endDate) {
        long secondsBetween = 0;
        
        while (startDate.before(endDate)) {
            startDate.add(Calendar.SECOND, 1);
            secondsBetween++;
        }   
        return secondsBetween;
    }
	
}