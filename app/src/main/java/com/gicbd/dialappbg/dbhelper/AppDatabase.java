package com.gicbd.dialappbg.dbhelper;

import com.gicbd.dialappbg.models.StoreData;

import io.realm.Realm;
import io.realm.RealmResults;

public class AppDatabase {

    public AppDatabase() {
    }

    public static RealmResults<StoreData> getAllLocalData() {
        return Realm.getDefaultInstance().where(StoreData.class).findAll();
    }


    public static void clearAllLocalData() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(StoreData.class);
        realm.commitTransaction();
    }

}
