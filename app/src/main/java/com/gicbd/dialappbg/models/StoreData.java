package com.gicbd.dialappbg.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class StoreData extends RealmObject {

    @PrimaryKey
    long id;
    String client_phone;
    String client_name;
    String statuslevel_id;
    String call_date;
    String call_time;
    String duration;


    public void setId(long id) {
        this.id = id;
    }

    public void setClient_phone(String client_phone) {
        this.client_phone = client_phone;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public void setStatuslevel_id(String statuslevel_id) {
        this.statuslevel_id = statuslevel_id;
    }

    public void setCall_date(String call_date) {
        this.call_date = call_date;
    }

    public void setCall_time(String call_time) {
        this.call_time = call_time;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
