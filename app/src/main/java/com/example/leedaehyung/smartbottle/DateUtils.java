package com.example.leedaehyung.smartbottle;

import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lee DaeHyung on 2019-03-22.
 */

public class DateUtils {


    public static String getFarDay(int far){
        Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH,far);
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
        String currentDateandTime = sdf.format(cal.getTime());
        return currentDateandTime;
    }
    public static int getDateDay(String date, String dateType){
        try{
            SimpleDateFormat dateFormat=new SimpleDateFormat(dateType);
            Date nDate = dateFormat.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(nDate);
            return cal.get(Calendar.DAY_OF_WEEK)-1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
    int getDayofWeek(String date, String dateType){
        try{
            SimpleDateFormat dateFormat=new SimpleDateFormat(dateType);
            Date nDate = dateFormat.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(nDate);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            return dayOfWeek;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
    String getDayNameList(String days){
        StringBuilder builder= new StringBuilder();
        if(days.contains("0"))
            builder.append("일");
        if(days.contains("1"))
            builder.append("월");
        if(days.contains("2"))
            builder.append("화");
        if(days.contains("3"))
            builder.append("수");
        if(days.contains("4"))
            builder.append("목");
        if(days.contains("5"))
            builder.append("금");
        if(days.contains("6"))
            builder.append("토");
        return builder.toString();
    }
    String getIndexOfDayName(int index){
        String dname ;
        switch (index){
            case 1: dname="월요일";
            case 2: dname="화요일";
            case 3: dname="수요일";
            case 4: dname="목요일";
            case 5: dname="금요일";
            case 6: dname="토요일";
            default: dname="일요일";

        }
        return dname;
    }
    String getIndexofDayNameHead(int index){
        String dayName ;
        switch (index){
            case 1: dayName="(월)";
            case 2: dayName="(화)";
            case 3: dayName="(수)";
            case 4: dayName="(목)";
            case 5: dayName="(금)";
            case 6: dayName="(토)";
            default: dayName="(일)";

        }
        return dayName;
    }
}
