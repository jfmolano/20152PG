package com.example.jfm.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String DEBUG_TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        Toast.makeText(context, "Alarm worked.", Toast.LENGTH_LONG).show();
        System.out.println("Funcionaaaa - - - - - - - - - - - - - - - - -");
    }

}
