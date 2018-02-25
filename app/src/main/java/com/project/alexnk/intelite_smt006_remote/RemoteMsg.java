/*
 * Copyright (c) 2018. by Alexander Nazarenko
 */

package com.project.alexnk.intelite_smt006_remote;

import java.util.Calendar;
public class RemoteMsg
{
    public static class Time
    {
        public int hours = 0;
        public int minutes = 0;

        public Time(int h, int m)
        {
            hours = h;
            minutes = m;
        }
    }

    public final static int pref_pulse   = 3450;
    public final static int pref_space   = 1650;
    public final static int pulse        = 450;
    public final static int zero_space   = 450;
    public final static int one_space    = 1100;
    public final static int packet_size  = 14;
    public final static int pattern_size = 2 + 14*8*2 + 2;
    public final static int b_header     = 0b01110101;

    public final static int max_brightness = 10;
    public final static int max_color      = 10;

    private final static byte mode_off   = 0b00000000;
    private final static byte mode_on    = 0b00000001;
    private final static byte mode_night = 0b00000010;

    // b1
    protected boolean imitation    = false;
    protected boolean off_flag     = false;
    protected boolean on_flag      = false;
    protected byte sleep_mode      = 0;
    protected byte mode_flags      = mode_on;
    // b5-b6
    protected int imitation_hour   = 0;
    protected int imitation_minute = 0;
    // b7-b10
    protected int on_hour          = 0;
    protected int on_minute        = 0;
    protected int off_hour         = 0;
    protected int off_minute       = 0;
    // b11
    protected int color            = 5;
    protected int brightness       = 6;
    // b12
    protected boolean eco_mode     = false;
    protected boolean max_mode     = false;
    protected int night_mode       = 1;

    private int fillByte(int[] pattern, int pos, int b)
    {
        for (int i = 1; i <= 128; i *= 2)
        {
            pattern[pos++] = pulse;
            pattern[pos++] = ((b & i) != 0) ? one_space : zero_space;
        }
        return pos;
    }

    public void turnOn(boolean v)
    {
        if (v)
            mode_flags = mode_on;
        else
            mode_flags = mode_off;
        max_mode = false;
    }

    public boolean isOn()
    {
        return mode_flags == mode_on;
    }

    public void turnNightMode()
    {
        mode_flags = mode_night;
    }

    public boolean isNight()
    {
        return mode_flags == mode_night;
    }

    public int nightMode()
    {
        return night_mode;
    }

    public void setNightMode(int v) throws IllegalArgumentException
    {
        if ((v < 0) || (v > 4))
            throw new IllegalArgumentException("Invalid night mode value");
        night_mode = v;
    }

    public int sleepMode()
    {
        return sleep_mode;
    }

    public void setSleepMode(int v) throws IllegalArgumentException
    {
        if ((v < 0) || (v > 3))
            throw new IllegalArgumentException("Invalid sleep mode value");
        sleep_mode = (byte)v;
    }

    public boolean isAutoOn()
    {
        return on_flag;
    }

    public void turnAutoOn(boolean v)
    {
        on_flag = v;
    }

    public Time onTime()
    {
        return new Time(on_hour, on_minute);
    }

    public void setOnTime(Time t)
    {
        on_hour = t.hours;
        on_minute = t.minutes;
    }

    public boolean isAutoOff()
    {
        return off_flag;
    }

    public void turnAutoOff(boolean v)
    {
        off_flag = v;
    }

    public Time offTime()
    {
        return new Time(off_hour, off_minute);
    }

    public void setOffTime(Time t)
    {
        off_hour = t.hours;
        off_minute = t.minutes;
    }

    public boolean isImitation()
    {
        return imitation;
    }

    public void turnImitation(boolean v)
    {
        imitation = v;
    }

    public Time imitationTime()
    {
        return new Time(imitation_hour, imitation_minute);
    }

    public void setImitationTime(Time t)
    {
        imitation_hour = t.hours;
        imitation_minute = t.minutes;
    }

    public int getBrightness()
    {
        return brightness;
    }

    public void setBrightness(int v) throws IllegalArgumentException
    {
        if ((v < 1) || (v > max_brightness))
            throw new IllegalArgumentException("Invalid brightness value");
        brightness = v;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int v) throws IllegalArgumentException
    {
        if ((v < 1) || (v > max_color))
            throw new IllegalArgumentException("Invalid color value");
        color = v;
    }

    public void turnEcoMode(boolean v)
    {
        eco_mode = v;
    }

    public boolean isEcoMode()
    {
        return eco_mode;
    }

    public void turnMaxMode(boolean v)
    {
        max_mode = v;
    }

    public boolean isMaxMode()
    {
        return max_mode;
    }

    public int[] genPattern()
    {
        int[] packet = new int[packet_size];

        // Header byte
        packet[0] = b_header;

        // Flags
        packet[1] = (imitation ? 0b10000000 : 0)
                  | (off_flag ? 0b01000000 : 0)
                  | (on_flag ? 0b00100000 : 0)
                  | ((sleep_mode & 0b00000011) << 3)
                  | mode_flags;

        // Current time
        Calendar c = Calendar.getInstance();
        packet[2] = c.get(Calendar.HOUR_OF_DAY);
        packet[3] = c.get(Calendar.MINUTE);
        packet[4] = c.get(Calendar.SECOND);

        // Home imitation time
        packet[5] = imitation_hour;
        packet[6] = imitation_minute;

        // Auto on
        packet[7] = on_hour;
        packet[8] = on_minute;

        // Auto off
        packet[9] = off_hour;
        packet[10] = off_minute;

        // Color/brightness
        if (max_mode)
            packet[11] = 0b01011010;
        else
            packet[11] = ((color & 0b00001111) << 4) | (brightness & 0b00001111);

        // Eco/max/night mode
        packet[12] = (eco_mode ? 0b10000000 : 0)
                   | (max_mode ? 0b01000000 : 0)
                   | (night_mode & 0b00001111);

        // Calc checksum
        int checksum = 0;
        for (int i = 0; i < packet_size - 1; ++i)
            checksum = (checksum + packet[i]) & 0xff;
        packet[13] = (checksum + 0x55) & 0xff;

        // Generate IR pattern
        int[] pattern = new int[pattern_size];
        int p = 0;
        pattern[p++] = pref_pulse;
        pattern[p++] = pref_space;
        for (int i = 0; i < packet_size; ++i)
            p = fillByte(pattern, p, packet[i]);
        pattern[p++] = pulse;
        pattern[p++] = zero_space;
        return pattern;
    }
}
