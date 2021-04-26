package com.gicbd.dialappbg.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.gicbd.dialappbg.R;
import com.gicbd.dialappbg.dbhelper.SharedDataSaveLoad;
import com.gicbd.dialappbg.interfaces.LoginView;
import com.gicbd.dialappbg.models.LoginResponse;
import com.gicbd.dialappbg.presenters.LoginPresenter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class LoginActivity extends AppCompatActivity implements LoginView {

    RelativeLayout login_layout;
    LottieAnimationView animation_view;
    ImageView loginButton;
    private LoginPresenter mPresenter;
    private TextInputEditText userPhone;
    private TextInputEditText userPass;
    private TextInputLayout input_layout_phone;
    private TextInputLayout input_layout_pin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login page");

        initializeSSLContext(this);

        login_layout = findViewById(R.id.login_layout);

        loginButton = findViewById(R.id.btn_login);
        animation_view = findViewById(R.id.animation_view);
        userPhone = findViewById(R.id.edt_phone);
        userPass = findViewById(R.id.edt_pin);
        input_layout_phone = findViewById(R.id.input_layout_phone);
        input_layout_pin = findViewById(R.id.input_layout_pin);

        mPresenter = new LoginPresenter(this);

        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_auth_token));
        if (token != null && !token.isEmpty()) {
            goDashboard();
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLogin();
            }
        });

    }

    public void showAnimation() {
        login_layout.setVisibility(View.GONE);
        animation_view.setVisibility(View.VISIBLE);
        animation_view.setAnimation("loading.json");
        animation_view.playAnimation();
        animation_view.loop(true);
    }

    public void hideAnimation() {
        login_layout.setVisibility(View.VISIBLE);
        if (animation_view.isAnimating()) animation_view.cancelAnimation();
        animation_view.setVisibility(View.GONE);
    }

    //Phone number validation
    public void goDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    private static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Validating form
     */
    private void submitLogin() {

        if (!validatePhone()) {
            return;
        }

        if (!validatePin()) {
            return;
        }

        hideKeyboard(this);

        String phone = userPhone.getText().toString().trim();
        String pin = userPass.getText().toString().trim();
        SharedDataSaveLoad.save(this, getString(R.string.preference_auth_token), phone);
        attemptLogin(phone, pin);


    }

    public void clearTextField() {
        userPhone.setText("");
        userPass.setText("");
    }

    //Phone number validation with matcher class.

    private boolean validatePhone() {
        String phone = userPhone.getText().toString().trim();

        if (phone.isEmpty() || !isValidPhone(phone)) {
            input_layout_phone.setError(getString(R.string.err_msg_phone));
            requestFocus(userPhone);
            return false;
        } else {
            input_layout_phone.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePin() {
        String pin = userPass.getText().toString().trim();
        if (pin.isEmpty()) {
            input_layout_pin.setError(getString(R.string.err_msg_pin));
            requestFocus(userPass);
            return false;
        } else if (pin.length() < 4) {
            input_layout_pin.setError(getString(R.string.err_msg_pin_length));
            requestFocus(userPass);
            return false;
        } else {
            input_layout_pin.setErrorEnabled(false);
        }

        return true;
    }

    private void attemptLogin(String phone, String pin) {
        String fcmToken = SharedDataSaveLoad.load(this, getString(R.string.preference_auth_token));
        if (checkConnection()) {
            showAnimation();
            mPresenter.attemptLogin(phone, pin);
        } else
            // CustomAlertDialog.showError(LoginActivity.this, getString(R.string.err_no_internet_connection));
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private String getVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "0.0.0";
        }
    }

    //make sure that is internet available or not
    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    public void onSuccess(LoginResponse login, int code, String message) {
        clearTextField();
        hideAnimation();
        if (login.getAccessToken() != null) {
            Toast.makeText(getApplicationContext(), "Successfully Logged in", Toast.LENGTH_SHORT).show();
            SharedDataSaveLoad.save(this, getString(R.string.preference_auth_token), "Bearer " + login.getAccessToken());
            goDashboard();
        } else {
            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onError(String error) {
        hideAnimation();
        Log.d("TAG", "onError: " + error);
       Toast.makeText(getApplicationContext(), error + "", Toast.LENGTH_SHORT).show();
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
}
