package com.gicbd.dialappbg.globals;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.gicbd.dialappbg.globals.Constants.DB_NAME;

public class MyDialApplication extends Application {
    private static MyDialApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        initRealm();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration defaultRealmConfiguration = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .name(DB_NAME)
                .build();
        Realm.setDefaultConfiguration(defaultRealmConfiguration);
    }

    public static MyDialApplication getInstance() {
        return mInstance;
    }
}
