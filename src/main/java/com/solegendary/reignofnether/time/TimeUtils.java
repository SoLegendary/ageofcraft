package com.solegendary.reignofnether.time;

public class TimeUtils {

    public static final long DAWN = 500;
    public static final long DUSK = 12500;

    // ensures a time value is between 0 and 24000
    public static long normaliseTime(long time) {
        long timeNorm = time;
        while (timeNorm < 0)
            timeNorm += 24000;
        while (timeNorm >= 24000)
            timeNorm -= 24000;
        return timeNorm;
    }

    public static String get12HourTimeStr(long time) {
        long hours = time / 1000 + 6;
        long minutes = (time % 1000) * 60 / 1000;
        String ampm = "am";
        while (hours >= 12) {
            hours -= 12;
            ampm = "pm";
        }
        if (hours == 0) hours = 12;
        String mm = "0" + minutes;
        mm = mm.substring(mm.length() - 2);
        return hours + ":" + mm + ampm;
    }

    // get a string representing real time in min/sec until the given time
    public static String getTimeUntilStr(long currentTime, long targetTime) {
        if (currentTime > targetTime)
            currentTime -= 24000;
        long timeDiff = targetTime - currentTime;

        // there's 1200 real time seconds per MC day (24000 units)
        int sec = (int) Math.round(timeDiff / 20d);
        int min = sec / 60;
        sec -= (min * 60);

        if (min == 0)
            return sec + "s";
        return min + "m" + sec + "s";
    }

    // get a string representing real time in min/sec until the given time
    public static String getTimeStrFromTicks(long ticks) {
        int sec = (int) Math.round(ticks / 20d);
        int min = sec / 60;
        sec -= (min * 60);

        if (min == 0)
            return sec + "s";
        return min + "m" + sec + "s";
    }

    // use instead of level.isDay() as its much stricter for undead burning checks
    public static boolean isDay(long time) {
        long normTime = normaliseTime(time);
        return normTime > DAWN && normTime <= DUSK;
    }
}
