package com.zh.weixinmonitor;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zhanghong on 2017/7/12.
 */

public class TimeUtils {

    public static String convertTimeToUploadTimeByLongString(String weixinTime) {
        if (TextUtils.isEmpty(weixinTime)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        return simpleDateFormat.format(Long.valueOf(weixinTime));
    }

    public static String convertTimeToUploadTime(String weixinTime) {
        if (TextUtils.isEmpty(weixinTime)) {
            return null;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String subPrefix = "";
        String subSuffix = "";

        if (weixinTime.contains("昨天")) {
            subSuffix = weixinTime.substring(3, weixinTime.length());
            String[] detailTime = subSuffix.split(":");
            return simpleDateFormat.format(getDayStartTime(-1, Integer.valueOf(detailTime[0]), Integer.valueOf(detailTime[1])));
        } else if (weixinTime.contains("星期")) {
            subSuffix = weixinTime.substring(4, weixinTime.length());
            String[] detailTime = subSuffix.split(":");
            String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
            int i = 0;
            for (; i < weekDays.length; i++) {
                if (weixinTime.equals(weekDays[i])) {
                    break;
                }
            }
            return simpleDateFormat.format(getDayStartTime(i - w, Integer.valueOf(detailTime[0]), Integer.valueOf(detailTime[1])));
        } else if (weixinTime.contains("年")) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
            String[] weixinTimeString = weixinTime.split(" ");
            String yearString = weixinTimeString[0];
            String timeString = weixinTimeString[1];
            try {
                String finalDetailTime = timeString.substring(2, timeString.length());
                String[] detailTime = finalDetailTime.split(":");
                int hour = Integer.parseInt(detailTime[0]);
                int minute = Integer.parseInt(detailTime[1]);
                Date date = yearDateFormat.parse(yearString);

                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            } catch (Exception ex) {

            }
            return simpleDateFormat.format(calendar.getTime().getTime());
        } else if (weixinTime.contains("月")) {
            String[] weixinTimeString = weixinTime.split(" ");
            String monthString = weixinTimeString[0];
            String timeString = weixinTimeString[1];
            SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM月dd日", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            try {
                String finalDetailTime = timeString.substring(2, timeString.length());
                String[] detailTime = finalDetailTime.split(":");
                int hour = Integer.parseInt(detailTime[0]);
                int minute = Integer.parseInt(detailTime[1]);

                Calendar yearCalendar = Calendar.getInstance();
                yearCalendar.setTime(new Date());

                Date date = monthDateFormat.parse(monthString);
                calendar.setTime(date);
                calendar.set(Calendar.YEAR, yearCalendar.get(Calendar.YEAR));
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            } catch (Exception ex) {

            }
            return simpleDateFormat.format(calendar.getTime().getTime());
        } else {
            String[] timeString = weixinTime.split(":");
            int hour = Integer.valueOf(timeString[0]);
            int minute = Integer.valueOf(timeString[1]);
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return simpleDateFormat.format(cal.getTime().getTime());
        }
    }

    public static long getDayStartTime(int differ, int hour, int minute) {
        Calendar dayStart = Calendar.getInstance();
        dayStart.add(Calendar.DATE, differ);
        dayStart.set(Calendar.HOUR_OF_DAY, hour);
        dayStart.set(Calendar.MINUTE, minute);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        return dayStart.getTime().getTime();
    }

    public static long getDayTimeOfWeiXin(String weixinTime) {
        if (TextUtils.isEmpty(weixinTime)) {
            return 0;
        }
        String subPrefix = "";
        String subSuffix = "";

        if (weixinTime.contains("昨天")) {
            subSuffix = weixinTime.substring(3, weixinTime.length());
            String[] detailTime = subSuffix.split(":");
            return getDayStartTime(-1, Integer.valueOf(detailTime[0]), Integer.valueOf(detailTime[1]));
        } else if (weixinTime.contains("星期")) {
            subSuffix = weixinTime.substring(4, weixinTime.length());
            String[] detailTime = subSuffix.split(":");
            String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
            int i = 0;
            for (; i < weekDays.length; i++) {
                if (weixinTime.equals(weekDays[i])) {
                    break;
                }
            }
            return getDayStartTime(i - w, Integer.valueOf(detailTime[0]), Integer.valueOf(detailTime[1]));
        } else if (weixinTime.contains("年")) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
            String[] weixinTimeString = weixinTime.split(" ");
            String yearString = weixinTimeString[0];
            String timeString = weixinTimeString[1];
            try {
                String finalDetailTime = timeString.substring(2, timeString.length());
                String[] detailTime = finalDetailTime.split(":");
                int hour = Integer.parseInt(detailTime[0]);
                int minute = Integer.parseInt(detailTime[1]);
                Date date = yearDateFormat.parse(yearString);

                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            } catch (Exception ex) {

            }
            return calendar.getTime().getTime();
        } else if (weixinTime.contains("月")) {
            String[] weixinTimeString = weixinTime.split(" ");
            String monthString = weixinTimeString[0];
            String timeString = weixinTimeString[1];
            SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM月dd日", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            try {
                String finalDetailTime = timeString.substring(2, timeString.length());
                String[] detailTime = finalDetailTime.split(":");
                int hour = Integer.parseInt(detailTime[0]);
                int minute = Integer.parseInt(detailTime[1]);

                Calendar yearCalendar = Calendar.getInstance();
                yearCalendar.setTime(new Date());

                Date date = monthDateFormat.parse(monthString);
                calendar.setTime(date);
                calendar.set(Calendar.YEAR, yearCalendar.get(Calendar.YEAR));
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            } catch (Exception ex) {

            }
            return calendar.getTime().getTime();
        } else {
            String[] timeString = weixinTime.split(":");
            int hour = Integer.valueOf(timeString[0]);
            int minute = Integer.valueOf(timeString[1]);
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime().getTime();
        }
    }

    public static void main(String args[]) throws ParseException {
        SimpleDateFormat yearDataFormat = new SimpleDateFormat("yyyy年MM月dd日 晚上HH:mm", Locale.getDefault());
        String yearString = "2016年7月4日 晚上12:30";
//        System.out.println(convertTimeToUploadTime(yearString));
//        System.out.println(yearDataFormat.parse(yearDataFormat.format(yearString)));


//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String weixinTime = "12:20";
//        String subPrefix = "";
//        String subSuffix = "";
//        subSuffix = weixinTime.substring(3, weixinTime.length());
//        String[] detailTime = subSuffix.split(":");
//        System.out.println(simpleDateFormat.format(getDayStartTime(-1, Integer.valueOf(detailTime[0]), Integer.valueOf(detailTime[1]))));

        String[] timeString = weixinTime.split(":");
        int hour = Integer.valueOf(timeString[0]);
        int minute = Integer.valueOf(timeString[1]);
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        System.out.println(simpleDateFormat.format(cal.getTime().getTime()));
//        if (weixinTime.contains("昨天")) {
//            subPrefix = weixinTime.substring(0, 3);
//            subSuffix = weixinTime.substring(3, weixinTime.length());
//        }
//        System.out.println("前缀：" + subPrefix + " 后缀： " + subSuffix);
//        String[] detailTime = subSuffix.split(":");
//        System.out.println(simpleDateFormat.format(getDayStartTime(-1, Integer.valueOf(detailTime[0]), Integer.valueOf(detailTime[1]))));
//        Date date = new Date();
//        SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
//        System.out.println(dateFm.format(date));
//
//        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
//        if (w < 0)
//            w = 0;
//        System.out.println(weekDays[w]);
//
//        int i = 0;
//        for (; i < weekDays.length; i++) {
//            if ("星期一".equals(weekDays[i])) {
//                break;
//            }
//        }
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        System.out.println(simpleDateFormat.format(getDayStartTime(i - w)) + " 12:35");
    }
}
