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

public class ImitationDialogFragment extends DialogFragment
{
    private RemoteMsg remoteMsg;
    private DialogHost dialogHost;

    private TimeOptionGroup groupImitation;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dlg_imitation_title);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_imitation, null);
        builder.setView(view);

        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        groupImitation = new TimeOptionGroup(view, R.id.toggleImitationOn, R.id.pickerOnH, R.id.pickerOnM,
            new TimeOptionGroup.Host()
            {
                @Override
                public void onTOGChanged(boolean enabled, int hours, int minutes)
                {
                    remoteMsg.turnImitation(enabled);
                    remoteMsg.setImitationTime(new RemoteMsg.Time(hours, minutes));
                }

                @Override
                public void syncState()
                {
                    dialogHost.syncData();
                }

                @Override
                public boolean getTOGState(RemoteMsg.Time time)
                {
                    RemoteMsg.Time t = remoteMsg.imitationTime();
                    time.hours = t.hours;
                    time.minutes = t.minutes;
                    return remoteMsg.isImitation();
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
