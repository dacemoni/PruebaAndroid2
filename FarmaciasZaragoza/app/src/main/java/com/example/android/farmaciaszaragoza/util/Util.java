package com.example.android.farmaciaszaragoza.util;


import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

public class Util {


    public static LatLng DeUMTSaLatLng(double este, double oeste, char zonaLat, int zonaLong) {

        UTMRef utm = new UTMRef(este, oeste, 'N', 30);

        return utm.toLatLng();
    }
}