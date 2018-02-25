/*
 * Copyright (c) 2018. by Alexander Nazarenko
 */

package com.project.alexnk.intelite_smt006_remote;

import android.content.SharedPreferences;

class SavingRemoteMsg extends RemoteMsg
{
    private final SharedPreferences pref;

    SavingRemoteMsg(SharedPreferences p)
    {
        pref = p;
        load();
    }

    public void save()
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("imitation", imitation);
        editor.putBoolean("off_flag", off_flag);
        editor.putBoolean("on_flag", on_flag);
        editor.putInt("sleep_mode", sleep_mode);
        editor.putInt("mode_flags", mode_flags);
        editor.putInt("imitation_hour", imitation_hour);
        editor.putInt("imitation_minute", imitation_minute);
        editor.putInt("on_hour", on_hour);
        editor.putInt("on_minute", on_minute);
        editor.putInt("off_hour", off_hour);
        editor.putInt("off_minute", off_minute);
        editor.putInt("color", color);
        editor.putInt("brightness", brightness);
        editor.putBoolean("eco_mode", eco_mode);
        editor.putBoolean("max_mode", max_mode);
        editor.putInt("night_mode", night_mode);
        editor.apply();
    }

    public void load()
    {
        imitation        = pref.getBoolean("imitation", imitation);
        off_flag         = pref.getBoolean("off_flag", off_flag);
        on_flag          = pref.getBoolean("on_flag", on_flag);
        sleep_mode       = (byte) pref.getInt("sleep_mode", sleep_mode);
        mode_flags       = (byte) pref.getInt("mode_flags", mode_flags);
        imitation_hour   = pref.getInt("imitation_hour", imitation_hour);
        imitation_minute = pref.getInt("imitation_minute", imitation_minute);
        on_hour          = pref.getInt("on_hour", on_hour);
        on_minute        = pref.getInt("on_minute", on_minute);
        off_hour         = pref.getInt("off_hour", off_hour);
        off_minute       = pref.getInt("off_minute", off_minute);
        color            = pref.getInt("color", color);
        brightness       = pref.getInt("brightness", brightness);
        eco_mode         = pref.getBoolean("eco_mode", eco_mode);
        max_mode         = pref.getBoolean("max_mode", max_mode);
        night_mode       = pref.getInt("night_mode", night_mode);
    }
}
