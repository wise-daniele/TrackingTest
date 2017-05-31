package com.example.daniele.trackingtest;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by daniele on 31/05/17.
 */

public class Utils {

    public static String getDateFromTimestamp(long timestamp){
        Calendar mydate = Calendar.getInstance();
        mydate.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        return sdf.format(mydate.getTime());
    }
}
