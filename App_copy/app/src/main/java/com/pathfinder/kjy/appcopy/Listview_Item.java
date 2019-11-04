package com.pathfinder.kjy.appcopy;

import android.graphics.drawable.Drawable;

public class Listview_Item {

    private Drawable iconDrawable ;
    private String departure ;
    private String transfer;
    private String arrival ;

    public void setDeparture(String depart) { departure = depart ; }

    public void setTransfer(String transf) {
        transfer = transf;
    }
    public void setArrival(String arrive) {
        arrival = arrive ;
    }

    public String getDeparture() {
        return this.departure ;
    }
    public String getTransfer() {
        return this.transfer;
    }
    public String getArrival() {
        return this.arrival ;
    }
}