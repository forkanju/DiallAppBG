package com.gicbd.dialappbg.interfaces;

import com.gicbd.dialappbg.models.LoginResponse;

public interface LoginView {
    public void onSuccess(LoginResponse login, int code, String message);

    public void onError(String error);
}
