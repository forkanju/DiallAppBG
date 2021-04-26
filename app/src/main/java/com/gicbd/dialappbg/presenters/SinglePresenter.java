package com.gicbd.dialappbg.presenters;

import android.util.Log;

import com.gicbd.dialappbg.interfaces.StoreDataView;
import com.gicbd.dialappbg.models.PostResponse;
import com.gicbd.dialappbg.models.SingleLog;
import com.gicbd.dialappbg.services.APIClient;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class SinglePresenter {
    StoreDataView mView;
    private APIClient mAPIClient;

    public SinglePresenter(StoreDataView mView) {
        this.mView = mView;
        if (this.mAPIClient == null) {
            this.mAPIClient = new APIClient();
        }
    }


    public void sendJsonObj(String token, SingleLog obj) {
        mAPIClient.getAPI()
                .postDataObj(token, obj)
                .enqueue(new Callback<SingleLog>() {
                    @Override
                    public void onResponse(Call<SingleLog> call, Response<SingleLog> response) {
                        mView.singleSuccess(response.message());
                        Log.d("RES_CODE", response.code()+"");
                        Log.d("RES_CODE", response.message()+"");

                        if (response.code() == 200 || response.code() == 201) {
                            mView.singleSuccess(response.message());
                        } else if (response.code() == 400) {
                         //   mView.onLogout(response.code());
                            return;
                        } else {
                            mView.singleError("Error Occurred !");
                        }
                    }

                    @Override
                    public void onFailure(Call<SingleLog> call, Throwable e) {

                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
                            Log.e("CODE", code + "");
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            mView.singleError(getErrorMessage(responseBody));

                        } else if (e instanceof SocketTimeoutException) {

                            mView.singleError("Server connection error");
                            Log.d("Server", "onFailure: "+e);

                        } else if (e instanceof IOException) {

                            mView.singleError("IOException");
                            Log.d("IOE", "onFailure: "+ e);

                        } else {
                            mView.singleError("Unknown error");
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
