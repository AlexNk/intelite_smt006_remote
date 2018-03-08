/*
 * Copyright (c) 2018. by Alexander Nazarenko
 */

package com.project.alexnk.intelite_smt006_remote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import java.util.Vector;

public class MainActivity extends AppCompatActivity
                          implements DialogHost, View.OnClickListener
{
    private final boolean emulating = false;

    private int ir_frequency = 0;
    private ConsumerIrManager consumerIrManager;
    private SavingRemoteMsg remoteMsg;

    private ToggleButton togglePower;
    private ToggleButton toggleMax;
    private ToggleButton toggleNight;
    private SeekBar seekBarBrightness;
    private SeekBar seekBarColor;
    private ToggleButton toggleSleep;
    private ToggleButton toggleTimer;
    private ToggleButton toggleImitation;
    private ToggleButton toggleEcoMode;

    private final Vector<Drawable> nightDrawables = new Vector<>();
    private final Vector<Drawable> sleepDrawables = new Vector<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        consumerIrManager = (ConsumerIrManager) this.getSystemService(Context.CONSUMER_IR_SERVICE);
        if (!emulating)
            checkIR();

        nightDrawables.add(getResources().getDrawable(R.drawable.night_mode));
        nightDrawables.add(getResources().getDrawable(R.drawable.night_mode_1));
        nightDrawables.add(getResources().getDrawable(R.drawable.night_mode_2));
        nightDrawables.add(getResources().getDrawable(R.drawable.night_mode_3));
        nightDrawables.add(getResources().getDrawable(R.drawable.night_mode_4));

        sleepDrawables.add(getResources().getDrawable(R.drawable.sleep_mode));
        sleepDrawables.add(getResources().getDrawable(R.drawable.sleep_mode_1));
        sleepDrawables.add(getResources().getDrawable(R.drawable.sleep_mode_2));
        sleepDrawables.add(getResources().getDrawable(R.drawable.sleep_mode_3));

        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        remoteMsg = new SavingRemoteMsg(pref);

        togglePower = findViewById(R.id.togglePower);
        togglePower.setOnClickListener(this);

        toggleMax = findViewById(R.id.toggleMax);
        toggleMax.setOnClickListener(this);

        toggleNight = findViewById(R.id.toggleNight);
        toggleNight.setOnClickListener(this);

        toggleSleep = findViewById(R.id.toggleSleep);
        toggleSleep.setOnClickListener(this);

        toggleTimer = findViewById(R.id.toggleTimer);
        toggleTimer.setOnClickListener(this);

        toggleImitation = findViewById(R.id.toggleImitation);
        toggleImitation.setOnClickListener(this);

        toggleEcoMode = findViewById(R.id.toggleEco);
        toggleEcoMode.setOnClickListener(this);

        seekBarBrightness = findViewById(R.id.seekBarBrightness);
        initSeekBar(seekBarBrightness, 1, 10, 0xFF808080, 0xFFFFFFFF, false);

        seekBarColor = findViewById(R.id.seekBarColor);
        initSeekBar(seekBarColor, 1, 10, 0xFFFFA000, 0xFFD4FBFF, true);

        updateUI();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.toggleNight:
                onNightModeClick();
                break;
            case R.id.togglePower:
                onPowerClick();
                break;
            case R.id.toggleMax:
                onMaxClick();
                break;
            case R.id.toggleSleep:
                onSleepClick();
                break;
            case R.id.toggleTimer:
                onTimerClick();
                break;
            case R.id.toggleImitation:
                onImitationClick();
                break;
            case R.id.toggleEco:
                onEcoClick();
                break;
        }
    }

    private void onNightModeClick()
    {
        if (toggleNight.isChecked() && !remoteMsg.isNight())
            remoteMsg.turnNightMode();
        else
        if (remoteMsg.isNight())
        {
            int mode = remoteMsg.nightMode() + 1;
            if (mode > 4)
                mode = 1;

            try
            {
                remoteMsg.setNightMode(mode);
            }
            catch (Exception e)
            {   }
        } else
            remoteMsg.turnNightMode();

        syncState();
    }

    private void onPowerClick()
    {
        remoteMsg.turnOn(togglePower.isChecked());
        syncState();
    }

    private void onMaxClick()
    {
        remoteMsg.turnMaxMode(toggleMax.isChecked());
        syncState();
    }

    private void onSleepClick()
    {
        int mode = (remoteMsg.sleepMode() + 1) % 4;
        try
        {
            remoteMsg.setSleepMode(mode);
        }
        catch (Exception e)
        {   }

        syncState();
    }

    private void onTimerClick()
    {
        toggleTimer.setChecked(remoteMsg.isAutoOn() || remoteMsg.isAutoOff());
        TimerDialogFragment timerDlg = new TimerDialogFragment();
        timerDlg.setRemoteMsg(remoteMsg);
        timerDlg.show(getFragmentManager(), "on_off_timer");
    }

    private void onImitationClick()
    {
        toggleImitation.setChecked(remoteMsg.isImitation());
        ImitationDialogFragment presenceDialog = new ImitationDialogFragment();
        presenceDialog.setRemoteMsg(remoteMsg);
        presenceDialog.show(getFragmentManager(), "presence_time");
    }

    private void onEcoClick()
    {
        remoteMsg.turnEcoMode(toggleEcoMode.isChecked());
        syncState();
    }

    private void initSeekBar(final SeekBar seekBar,
                             final int minVal, final int maxVal,
                             final int gradStart, final int gradEnd,
                             final boolean reverse)
    {
        int gradient[] = new int[] {gradStart, gradEnd};
        seekBar.setProgressDrawable(
                new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradient));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            private final int seekBarId = seekBar.getId();
            private int progress = minVal;

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar)
            {
                try
                {
                    int value = reverse ? (maxVal + 1 - progress) : progress;
                    switch (seekBarId)
                    {
                        case R.id.seekBarBrightness:
                            remoteMsg.setBrightness(value);
                            break;
                        case R.id.seekBarColor:
                            remoteMsg.setColor(value);
                            break;
                        default:
                            return;
                    }
                    updateUI();
                }
                catch (Exception e)
                {   }
                syncState();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {   }

            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean fromUser)
            {
                progress = pos + minVal;
            }
        });
    }

    private void showMessage(String text, String button_text, final boolean exit)
    {
        AlertDialog.Builder dlg  = new AlertDialog.Builder(this);
        dlg.setMessage(text);
        dlg.setTitle(getResources().getString(R.string.app_name));
        dlg.setPositiveButton(button_text, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        dlg.setCancelable(true);
        if (exit)
        {
            dlg.setOnDismissListener(new Dialog.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialogInterface)
                {
                    System.exit(0);
                }
            });
        }
        dlg.create().show();
    }

    private void checkIR()
    {
        if (!consumerIrManager.hasIrEmitter())
        {
            String msg = getResources().getString(R.string.trans_not_found);
            showMessage(msg, getResources().getString(R.string.OK), true);
            return;
        }

        ConsumerIrManager.CarrierFrequencyRange[] freqRange = consumerIrManager.getCarrierFrequencies();
        final int best_freq = 38400;
        for (ConsumerIrManager.CarrierFrequencyRange freq : freqRange)
        {
            final int min = freq.getMinFrequency();
            final int max = freq.getMaxFrequency();
            if ((min <= best_freq) && (max >= best_freq))
            {
                ir_frequency = best_freq;
                break;
            }

            if (Math.abs(best_freq - min) < Math.abs(best_freq - ir_frequency))
                ir_frequency = min;
            if (Math.abs(best_freq - max) < Math.abs(best_freq - ir_frequency))
                ir_frequency = max;
        }

        if (ir_frequency == 0)
        {
            String msg = getResources().getString(R.string.freq_not_sup);
            showMessage(msg, getResources().getString(R.string.OK), true);
        }
    }

    private void syncState()
    {
        int pattern[] = remoteMsg.genPattern();
        if (!emulating)
            consumerIrManager.transmit(ir_frequency, pattern);

        remoteMsg.save();
        updateUI();

        /*
        if (emulating)
        {
            String res = "";
            for (int i = 0; i < RemoteMsg.packet_size; ++i)
            {
                int b = 0;
                for (int j = 0, mask = 0b10000000; j < 8; ++j, mask >>= 1)
                {
                    int space = pattern[2 + 8*2*i + j*2 + 1];
                    b |= (((space == RemoteMsg.one_space) ? 1 : 0) << j);
                }
                res += String.format("%02x ", b);
            }
            Log.v("IR sent", res);
        }
        */
    }

    private void updateUI()
    {
        toggleNight.setCompoundDrawablesWithIntrinsicBounds(
                null, nightDrawables.get(remoteMsg.nightMode()), null, null);
        if (remoteMsg.isNight() != toggleNight.isChecked() )
            toggleNight.setChecked(remoteMsg.isNight());

        final boolean is_on = remoteMsg.isOn() || remoteMsg.isNight();
        if (is_on != togglePower.isChecked())
            togglePower.setChecked(is_on);

        toggleMax.setEnabled(remoteMsg.isOn() || remoteMsg.isNight());
        final boolean is_max = (remoteMsg.isMaxMode() && remoteMsg.isOn());
        if (is_max != toggleMax.isChecked())
            toggleMax.setChecked(is_max);

        seekBarBrightness.setEnabled(remoteMsg.isOn() && !remoteMsg.isNight() && !remoteMsg.isMaxMode());
        seekBarBrightness.setProgress(remoteMsg.getBrightness() - 1);

        seekBarColor.setEnabled(remoteMsg.isOn() && !remoteMsg.isNight() && !remoteMsg.isMaxMode());
        seekBarColor.setProgress(10 - remoteMsg.getColor());

        toggleSleep.setEnabled(remoteMsg.isOn() || remoteMsg.isNight());
        toggleSleep.setCompoundDrawablesWithIntrinsicBounds(
                null, sleepDrawables.get(remoteMsg.sleepMode()), null, null);
        final boolean is_sleep = (remoteMsg.sleepMode() > 0);
        if (is_sleep != toggleSleep.isChecked())
            toggleSleep.setChecked(is_sleep);

        toggleTimer.setEnabled(remoteMsg.isOn() || remoteMsg.isNight());
        final boolean is_timer = remoteMsg.isAutoOn() || remoteMsg.isAutoOff();
        if (is_timer != toggleTimer.isChecked())
            toggleTimer.setChecked(is_timer);

        toggleImitation.setEnabled(remoteMsg.isOn() || remoteMsg.isNight());
        if (remoteMsg.isImitation() != toggleImitation.isChecked())
            toggleImitation.setChecked(remoteMsg.isImitation());

        toggleEcoMode.setEnabled(remoteMsg.isOn() && !remoteMsg.isNight() && !remoteMsg.isNight());
        if (remoteMsg.isEcoMode() != toggleEcoMode.isChecked())
            toggleEcoMode.setChecked(remoteMsg.isEcoMode());
    }

    @Override
    public void syncData()
    {
        syncState();
        updateUI();
    }
}
