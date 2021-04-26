package com.gicbd.dialappbg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MyBroadCast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Log.d("TAG", "onReceive: Ringing");
            }

            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // Log.d("TAG", "onReceive: Received");
            }

            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {

                Intent local = new Intent();
                local.setAction("service.to.activity.transfer");
                local.putExtra("number", "Call Terminated!");
                context.sendBroadcast(local);
                // Log.d("TAG", "onReceive: Terminated");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}



