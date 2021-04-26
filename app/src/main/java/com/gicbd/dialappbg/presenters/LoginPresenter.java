package com.gicbd.dialappbg.presenters;

import android.util.Log;

import com.gicbd.dialappbg.interfaces.LoginView;
import com.gicbd.dialappbg.models.LoginResponse;
import com.gicbd.dialappbg.services.APIClient;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class LoginPresenter {
    LoginView mViewInterface;
    private APIClient mAPIClient;

    public LoginPresenter(LoginView view) {
        this.mViewInterface = view;

        if (this.mAPIClient == null) {
            this.mAPIClient = new APIClient();
        }
    }


    public void attemptLogin(String phone, String pass) {


        mAPIClient.getAPI()
                .attemptLogin(phone, pass)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        Log.e("RES: ", response.message());
                        Log.e("RES: ", String.valueOf(response.code()));

                        if(response.code() == 400){
                            mViewInterface.onError("Invalid User!");
                        }

                        if (response.isSuccessful()) {
                            LoginResponse loginResponse = response.body();
                            if (loginResponse != null) {
                                mViewInterface.onSuccess(loginResponse, response.code(), response.message());
                            } else {
                                mViewInterface.onError(getErrorMessage(response.errorBody()));
                            }
                        } else mViewInterface.onError(getErrorMessage(response.errorBody()));
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.e("RES_F: ", t.getMessage().toString() + "");
                        mViewInterface.onError(t.getMessage());
                        t.printStackTrace();
                        if (t instanceof HttpException) {
                            int code = ((HttpException) t).response().code();
                            if(code == 400){
                                mViewInterface.onError("Invalid User!");
                            }
                            ResponseBody responseBody = ((HttpException) t).response().errorBody();
                            mViewInterface.onError(getErrorMessage(responseBody));

                            Log.d("TAG", "onFailure: "+getErrorMessage(responseBody));

                            Log.e("RES_F: ", t.getMessage().toString());

                        } else if (t instanceof SocketTimeoutException) {
                            mViewInterface.onError("Server connection error");
                            // Log.e("RES_F: ", t.getMessage().toString());
                        } else if (t instanceof IOException) {
                            Log.e("RES_F: ", t.getMessage().toString() + "");
                            mViewInterface.onError("IOException");

                        } else {
                            mViewInterface.onError("Unknown error");
                        }
                    }
                });

    }

    private String getErrorMessage(ResponseBody responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            return jsonObject.getString("message");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
