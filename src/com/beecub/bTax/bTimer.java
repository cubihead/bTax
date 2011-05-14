package com.beecub.bTax;

import java.util.Timer;
import java.util.TimerTask;

public class bTimer extends TimerTask {
    
    bTax bTax;
    
    public bTimer(bTax bTax, Timer timer){
        this.bTax = bTax;
    }
    public bTimer() {
    }
    
    public void run() {
        bConfigManager.checkTaxOnline();
        if(!bConfigManager.onlineonly){
            bConfigManager.checkTaxAll();
        }
        Timer scheduler = new Timer();
        bTimer scheduleMe = new bTimer(bTax, scheduler);
        scheduler.schedule(scheduleMe, bConfigManager.interval * 1000);
    }
}