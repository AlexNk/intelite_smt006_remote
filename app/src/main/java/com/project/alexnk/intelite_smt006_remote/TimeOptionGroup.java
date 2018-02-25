/*
 * Copyright (c) 2018. by Alexander Nazarenko
 */

package com.project.alexnk.intelite_smt006_remote;

import android.os.Handler;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

class TimeOptionGroup implements NumberPicker.OnValueChangeListener, View.OnClickListener
{
    public interface Host
    {
        void onTOGChanged(boolean enabled, int hours, int minutes);
        void syncState();
        boolean getTOGState(RemoteMsg.Time time);
    }

    private final NumberPicker pickerH;
    private final NumberPicker pickerM;
    private final ToggleButton toggleEnable;
    private final Handler handler = new Handler();
    private final Host groupHost;
    private int hours, minutes;

    TimeOptionGroup(View viewParent, int toggleId, int hoursId, int minutesId, Host host)
    {
        pickerH = viewParent.findViewById(hoursId);
        initNumPicker(pickerH, 0, 23);

        pickerM = viewParent.findViewById(minutesId);
        initNumPicker(pickerM, 0, 59);

        toggleEnable = viewParent.findViewById(toggleId);
        toggleEnable.setOnClickListener(this);

        groupHost = host;

        updateUI();
    }

    @Override
    public void onClick(View view)
    {
        groupHost.onTOGChanged(toggleEnable.isChecked(), hours, minutes);
        groupHost.syncState();
        updateUI();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1)
    {
        if (numberPicker.getId() == pickerH.getId())
            hours = i1;
        else
            minutes = i1;

        groupHost.onTOGChanged(toggleEnable.isChecked(), hours, minutes);
        updateUI();

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                groupHost.syncState();
            }
        }, 750);
    }

    private void initNumPicker(NumberPicker np, int min, int max)
    {
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setWrapSelectorWheel(true);
        String labels[] = new String[max - min + 1];
        for (int i = 0; i < labels.length; ++i)
            labels[i] = String.format("%02d", i + min);
        np.setDisplayedValues(labels);
        np.setSaveFromParentEnabled(false);
        np.setSaveEnabled(false);

        np.setOnValueChangedListener(this);
    }

    private void updateUI()
    {
        RemoteMsg.Time time = new RemoteMsg.Time(0, 0);
        boolean enabled = groupHost.getTOGState(time);

        toggleEnable.setChecked(enabled);

        hours = time.hours;
        minutes = time.minutes;
        pickerH.setEnabled(enabled);
        pickerH.setValue(hours);
        pickerM.setEnabled(enabled);
        pickerM.setValue(minutes);
    }
}
