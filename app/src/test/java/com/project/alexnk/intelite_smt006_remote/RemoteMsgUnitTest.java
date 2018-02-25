
/*
 * Copyright (c) 2018. by Alexander Nazarenko
 */

package com.project.alexnk.intelite_smt006_remote;

import org.junit.Test;
import java.util.Calendar;

import static org.junit.Assert.*;

public class RemoteMsgUnitTest
{
    private int getRevByte(int pattern[], int n)
    {
        int ret = 0;
        for (int i = 0, mask = 0b10000000; i < 8; ++i, mask >>= 1)
        {
            int pulse = pattern[2 + 8*2*n + i*2];
            int space = pattern[2 + 8*2*n + i*2 + 1];

            assertEquals(pulse, RemoteMsg.pulse);
            assertTrue((space == RemoteMsg.one_space) || (space == RemoteMsg.zero_space));

            ret |= (((space == RemoteMsg.one_space) ? 1 : 0) << i);
        }
        return ret;
    }

    private int getCurrentSeconds()
    {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 * 60 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
    }

    @Test
    public void calcChecksum() throws Exception
    {
        RemoteMsg msg = new RemoteMsg();

        msg.turnOn(true);
        msg.setSleepMode(0);

        msg.turnEcoMode(false);
        msg.turnMaxMode(false);
        msg.setNightMode(3);

        msg.setBrightness(5);
        msg.setColor(4);

        msg.turnAutoOn(true);
        msg.setOnTime(new RemoteMsg.Time(1, 2));

        msg.turnAutoOff(true);
        msg.setOffTime(new RemoteMsg.Time(3, 4));

        msg.turnImitation(false);
        msg.setImitationTime(new RemoteMsg.Time(5,6));

        int startSeconds = getCurrentSeconds();
        int pattern[]  = msg.genPattern();
        int endSeconds = getCurrentSeconds();

        assertEquals(pattern.length, RemoteMsg.pattern_size);

        // Prefix
        assertEquals(pattern[0], RemoteMsg.pref_pulse);
        assertEquals(pattern[1], RemoteMsg.pref_space);

        // Suffix
        assertEquals(pattern[pattern.length - 2], RemoteMsg.pulse);
        assertEquals(pattern[pattern.length - 1], RemoteMsg.zero_space);

        // Header
        assertEquals(RemoteMsg.b_header, getRevByte(pattern, 0));

        // Flags
        assertEquals(0b01100001, getRevByte(pattern, 1));

        // Current time
        int currentTimeSeconds = getRevByte(pattern, 2) * 60 * 60
                               + getRevByte(pattern, 3) * 60
                               + getRevByte(pattern, 4);
        assertTrue((currentTimeSeconds >= startSeconds) && (currentTimeSeconds <= endSeconds));

        // Imitation time
        assertEquals(5, getRevByte(pattern, 5));
        assertEquals(6, getRevByte(pattern, 6));

        // On time
        assertEquals(1, getRevByte(pattern, 7));
        assertEquals(2, getRevByte(pattern, 8));

        // Off time
        assertEquals(3, getRevByte(pattern, 9));
        assertEquals(4, getRevByte(pattern, 10));

        // Color/brightness
        assertEquals(0b01000101, getRevByte(pattern, 11));

        // Eco/max/night mode
        assertEquals((byte)0b00000011, getRevByte(pattern, 12));

        int checksum = 0x55;
        for (int i = 0; i < RemoteMsg.packet_size - 1; ++i)
            checksum += getRevByte(pattern, i);

        assertEquals(checksum & 0xFF, getRevByte(pattern, 13));
    }
}