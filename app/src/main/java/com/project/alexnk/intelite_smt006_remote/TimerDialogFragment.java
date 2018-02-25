/*
 * Copyright (c) 2018. by Alexander Nazarenko
 */

package com.project.alexnk.intelite_smt006_remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class TimerDialogFragment extends DialogFragment
{
    private TimeOptionGroup groupAutoOn;
    private TimeOptionGroup groupAutoOff;

    private RemoteMsg remoteMsg;
    private DialogHost dialogHost;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dlg_timer_title);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_timer, null);
        builder.setView(view);

        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        groupAutoOn = new TimeOptionGroup(view, R.id.toggleAutoOn, R.id.pickerOnH, R.id.pickerOnM,
            new TimeOptionGroup.Host()
            {
                @Override
                public void onTOGChanged(boolean enabled, int hours, int minutes)
                {
                    remoteMsg.turnAutoOn(enabled);
                    remoteMsg.setOnTime(new RemoteMsg.Time(hours, minutes));
                }

                @Override
                public void syncState()
                {
                    dialogHost.syncData();
                }

                @Override
                public boolean getTOGState(RemoteMsg.Time time)
                {
                    RemoteMsg.Time t = remoteMsg.onTime();
                    time.hours = t.hours;
                    time.minutes = t.minutes;
                    return remoteMsg.isAutoOn();
                }
            });

        groupAutoOff = new TimeOptionGroup(view, R.id.toggleAutoOff, R.id.pickerOffH, R.id.pickerOffM,
            new TimeOptionGroup.Host()
            {
                @Override
                public void onTOGChanged(boolean enabled, int hours, int minutes)
                {
                    remoteMsg.turnAutoOff(enabled);
                    remoteMsg.setOffTime(new RemoteMsg.Time(hours, minutes));
                }

                @Override
                public void syncState()
                {
                    dialogHost.syncData();
                }

                @Override
                public boolean getTOGState(RemoteMsg.Time time)
                {
                    RemoteMsg.Time t = remoteMsg.offTime();
                    time.hours = t.hours;
                    time.minutes = t.minutes;
                    return remoteMsg.isAutoOff();
                }
            });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            dialogHost = (DialogHost)activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " is not proper DialogHost");
        }
    }

    public void setRemoteMsg(RemoteMsg msg)
    {
        remoteMsg = msg;
    }
}
