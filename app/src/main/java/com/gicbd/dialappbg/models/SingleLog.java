package com.gicbd.dialappbg.models;

public class SingleLog {

    String client_phone;
    String client_name;
    String statuslevel_id;
    String call_date;
    String call_time;
    String duration;

    public SingleLog(String client_phone, String client_name, String statuslevel_id, String call_date, String call_time, String duration) {
        this.client_phone = client_phone;
        this.client_name = client_name;
        this.statuslevel_id = statuslevel_id;
        this.call_date = call_date;
        this.call_time = call_time;
        this.duration = duration;
    }
}
