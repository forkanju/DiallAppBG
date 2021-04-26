package com.gicbd.dialappbg.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gicbd.dialappbg.R;
import com.gicbd.dialappbg.dbhelper.AppDatabase;
import com.gicbd.dialappbg.dbhelper.SharedDataSaveLoad;
import com.gicbd.dialappbg.globals.Constants;
import com.gicbd.dialappbg.interfaces.StoreDataView;
import com.gicbd.dialappbg.models.SingleLog;
import com.gicbd.dialappbg.models.StoreData;
import com.gicbd.dialappbg.presenters.SinglePresenter;
import com.gicbd.dialappbg.presenters.StoreDataPresenter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements StoreDataView {

    private static final String TAG = MainActivity.class.getSimpleName();
    Realm realm;
    BroadcastReceiver broadcastReceiver;
    private FloatingActionButton logoutButton;
    private StoreDataPresenter mPresenter;
    private SinglePresenter sPresenter;


    String callNumber_ = "";
    String callType_ = "";
    String contactName_ = "Concern";
    String callDuration_ = "";
    String callDate_ = "";
    String callTime_ = "";

    String token;

    String[] appPermissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE
    };
    private int eventCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        checkRequestPermissions();

        // UUID.randomUUID().toString();

        //Initialize realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        initializeSSLContext(this);

        logoutButton = findViewById(R.id.logout_button);
        mPresenter = new StoreDataPresenter((StoreDataView) this);
        sPresenter = new SinglePresenter((StoreDataView) this);


        //Custom Broadcast implementation and register broadcast here
        IntentFilter filter = new IntentFilter();
        filter.addAction("service.to.activity.transfer");

        token = SharedDataSaveLoad.load(this, getString(R.string.preference_auth_token));

        //Created object


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    eventCount = eventCount + 1;
                    if (eventCount == 2) {
                        eventCount = 0;
                        Toast.makeText(context, intent.getStringExtra("number").toString(), Toast.LENGTH_SHORT).show();
                        //Sleep 1second after terminated call and get last call log.
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getCallDetail();
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        SingleLog logObj = new SingleLog(callNumber_, contactName_, callType_, callDate_, callTime_, callDuration_);

//                                        if (isNetworkConnected()) {
                                            sPresenter.sendJsonObj(token, logObj);
//                                        } else {
//                                            Toast.makeText(getApplicationContext(), "Data Saved Locally!", Toast.LENGTH_SHORT).show();
//                                            saveLastCallLogToRealm();
//                                        }

                                    }
                                }, Constants.SLEEP_TIME);
                            }
                        }, Constants.SLEEP_TIME);


                    }
                }
            }
        };

        registerReceiver(broadcastReceiver, filter);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  onLogoutClicked();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Send local data when internet is available
        if (isNetworkConnected()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("calls", AppDatabase.getAllLocalData().asJSON());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(jsonObject != null){
                Log.d(TAG, "onStart: LocalData: "+jsonObject);
                mPresenter.postLastLogData(token, jsonObject);
            }

        }
    }

    //Check all required permissions is accepted or not..
    public boolean checkRequestPermissions() {
        List<String> allPermissions = new ArrayList<>();
        for (String item : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, item) != PackageManager.PERMISSION_GRANTED)
                allPermissions.add(item);
        }


        if (!allPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, allPermissions.toArray(new String[allPermissions.size()]), 1);
            return false;
        }
        //App has all permissions, Proceed ahead
        return true;
    }

    //Get Last call log details
    private String getCallDetail() {

        StringBuffer sb = new StringBuffer();
        @SuppressLint("MissingPermission") Cursor manageCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int name = manageCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);         //android.provider.CallLog.Calls.DATE + " DESC limit 1;"
        int number = manageCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = manageCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = manageCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = manageCursor.getColumnIndex(CallLog.Calls.DURATION);


        sb.append("Call Details:\n\n");
        int counter = 0;

        while (manageCursor.moveToLast()) {
            if (counter == 0) {
                String contactName = manageCursor.getString(name);
                String phoneNumber = manageCursor.getString(number);
                String callType = manageCursor.getString(type);
                String callDate = manageCursor.getString(date);
                Date callDayTime = new Date(Long.valueOf(callDate));
                SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yy HH:mm");
                String dateString = formater.format(callDayTime);
                String callDuration = manageCursor.getString(duration);
                String dir = null;
                int dirCode = Integer.parseInt(callType);
                switch (dirCode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "Outgoing";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "Incoming";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        dir = "Missed";
                        break;
                }
                sb.append("\n Phone Number:" + phoneNumber + " \n Call Type :" + dir + "\n Call Date:" + dateString +
                        " \n Call Duration :" + callDuration + "Contact_Name: " + contactName);
                counter++;
                sb.append("\n--------------------------------------------------");

                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                callTime_ = timeFormatter.format(new Date(Long.parseLong(callDate)));

                callNumber_ = phoneNumber;
                callType_ = dir + "";
                contactName_ = "Concern";
                callDuration_ = DurationFormat(callDuration);
                callDate_ = dateString;
                callTime_ = getFormatedDateTime(callTime_, "HH:mm:ss", "hh:mm a");


                //Log.d("USER_NUMBER: ", userNumber_);
                Log.d("CONTACT_NAME: ", contactName_ + "");
                Log.d("CONTACT_NUMBER: ", callNumber_);
                Log.d("CALL_TYPE: ", callType_ + "");
                Log.d("CALL_DATE: ", callDate_);
                Log.d("CALL_TIME: ", callTime_);
                Log.d("CALL_DURATION: ", callDuration_);

            } else {
                break;
            }
        }
        manageCursor.close();
        return sb.toString();
    }


    ///////////////////time duration format/////////////

    private String DurationFormat(String duration) {
        String durationFormatted = null;
        if (Integer.parseInt(duration) < 60) {
            durationFormatted = duration + " sec";
        } else {
            int min = Integer.parseInt(duration) / 60;
            int sec = Integer.parseInt(duration) % 60;

            if (sec == 0)
                durationFormatted = min + " min";
            else
                durationFormatted = min + " min " + sec + " :sec";

        }
        return durationFormatted;
    }


    //////////////////////// date and time format /////////////////////////////

    private String getFormatedDateTime(String dateStr, String strInputFormat, String strOutputFormat) {
        String formattedDate = dateStr;
        java.text.DateFormat inputFormat = new SimpleDateFormat(strInputFormat, Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat(strOutputFormat, Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.e("ERR", e + "");
        }
        if (date != null) {
            formattedDate = outputFormat.format(date);
        }
        return formattedDate;
    }


    public void saveLastCallLogToRealm() {
        final StoreData dataModel = new StoreData();

        Number current_id = realm.where(StoreData.class).max("id");
        long nextId;
        if (current_id == null) {
            nextId = 1;
        } else {
            nextId = current_id.intValue() + 1;
        }

        dataModel.setId(nextId);
        dataModel.setClient_name(contactName_ + "");
        dataModel.setClient_phone(callNumber_);
        dataModel.setStatuslevel_id(callType_);
        dataModel.setCall_date(callDate_);
        dataModel.setCall_time(callTime_);
        dataModel.setDuration(callDuration_);


        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(dataModel);
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "DialApp Destroyed from Background!", Toast.LENGTH_SHORT).show();
    }

    private void onLogoutClicked() {
        SharedDataSaveLoad.remove(this, getString(R.string.preference_auth_token));
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.caution)
                .setTitle("Caution!")
                .setCancelable(false)
                .setMessage("Are you sure,You want to close this app? if press yes this App will no longer workable.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onStoreSuccess(String storeData) {
        AppDatabase.clearAllLocalData();
        Log.d("SUCCESS", "onStoreSuccess: " + storeData);
        Toast.makeText(getApplicationContext(), "Status Updated Successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStoreError(String error) {
        Log.d("ERROR", "onStoreError: " + error);

    }

    @Override
    public void onLogout(int code) {
        if (code == 400) {
            SharedDataSaveLoad.remove(this, getString(R.string.preference_auth_token));
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void singleSuccess(String res) {
        Toast.makeText(getApplicationContext(), "Single Status Updated!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void singleError(String error) {

        Toast.makeText(getApplicationContext(), "Data Saved Locally!", Toast.LENGTH_SHORT).show();
        saveLastCallLogToRealm();

    }

    @Override
    public void singleSuccessCode(int code) {

    }

    /**
     * Initialize SSL
     *
     * @param mContext
     */
    public static void initializeSSLContext(Context mContext) {
        try {
            SSLContext.getInstance("TLSv1.2"); //SSL
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ProviderInstaller.installIfNeeded(mContext.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
