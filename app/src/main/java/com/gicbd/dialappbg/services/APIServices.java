package com.gicbd.dialappbg.services;

import com.gicbd.dialappbg.models.LoginResponse;
import com.gicbd.dialappbg.models.PostResponse;
import com.gicbd.dialappbg.models.SingleLog;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface APIServices {

    //@FormUrlEncoded only for @POST request and for @Field annotation.
    @FormUrlEncoded
    @POST("api/auth/login")
    Call<LoginResponse> attemptLogin(@Field("phone") String phone,
                                 @Field("password") String pass);

    //@FormUrlEncoded
    @POST("api/auth/employee-message-api")
    Call<PostResponse> postData(@Header ("Authorization") String api_token,
                                @Body JSONObject array);


    @POST("api/auth/store-single")
    Call<SingleLog> postDataObj(@Header ("Authorization") String api_token,
                                @Body SingleLog obj);



}
