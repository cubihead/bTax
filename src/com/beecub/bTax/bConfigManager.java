package com.beecub.bTax;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.iConomy.system.Holdings;

public class bConfigManager {
	
	protected static bTax bTax;
    protected static Configuration conf;
    protected static Configuration uconf;
    protected File confFile;
    static int interval;
    static int amount;
    static String message;
    static String payee;
    static String currencyname;
	
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
            conf.setProperty("tax.interval", " 60");
            conf.setProperty("tax.amount", "1");
            conf.setProperty("tax.message", "&6Its time to pay your tax: &amount &currencyname");
            conf.setProperty("tax.payee", "none");
            conf.setProperty("tax.currencyname", "MineCoins");
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
    	amount = conf.getInt("tax.amount", 1);
    	message = conf.getString("tax.message", "&6Its time to pay your tax: &amount &currencyname");
    	payee = conf.getString("tax.payee", "none");
    	currencyname = conf.getString("tax.currencyname", "MineCoins");
    	
    	uconf.load();
    }
	
	static void reload() {
		load();
	}
	
	static void checkTax() {
	    Player[] players = bTax.getServer().getOnlinePlayers();
	    Timer scheduler;
        for(Player player : players) {
            checkTaxPlayer(player);
        }
        scheduler = new Timer();
        bTimer scheduleMe = new bTimer(bTax, scheduler);
        scheduler.schedule(scheduleMe, interval * 1000);
	}
	
	static void checkTaxPlayer(Player player) {
        if(interval > 0) {
            Date lastTime = getTime(player);
            if(lastTime == null) {
                setTime(player);
            }
            lastTime = getTime(player);
            if(lastTime != null) {
                Calendar calcurrTime = Calendar.getInstance();
                calcurrTime.setTime(getCurrTime());
                Calendar callastTime = Calendar.getInstance();
                callastTime.setTime(lastTime);
                long secondsBetween = secondsBetween(callastTime, calcurrTime);
                if(secondsBetween > interval) {
                    withdrawTax(player);
                }
            }
        }
	}
	
	@SuppressWarnings("static-access")
    static void withdrawTax(Player player) {
	    message = message.replaceAll("&amount", "&e" + Integer.toString(amount));
	    message = message.replaceAll("&currencyname", "&e" + currencyname);
	    bChat.sendMessageToPlayer(player, message);
	    
        Holdings balance = bTax.iConomy.getAccount(player.getName()).getHoldings();
	    balance.subtract(amount);
	    
	    if(payee != null && payee != "") {
	        Holdings payeebalance = bTax.iConomy.getAccount(payee).getHoldings();
	        payeebalance.add(amount);
	    }
	    
	    setTime(player);
	}
	
    static void setTime(Player player) {
        String currTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        uconf.setProperty("users." + player.getName(), currTime);
        uconf.save();
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
    
    static Date getTime(Player player) {
        String confTime = "";
        confTime = uconf.getString("users." + player.getName(), null);
        
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