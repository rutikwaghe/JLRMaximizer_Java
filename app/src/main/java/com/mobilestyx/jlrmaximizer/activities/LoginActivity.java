package com.mobilestyx.jlrmaximizer;

import static com.mobilestyx.jlrmaximizer.utils.AppUtils.showAlertDialog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mobilestyx.jlrmaximizer.model.LoginRequest;
import com.mobilestyx.jlrmaximizer.model.LoginResponse;
import com.mobilestyx.jlrmaximizer.remote.ApiClient;
import com.mobilestyx.jlrmaximizer.remote.UserService;
import com.mobilestyx.jlrmaximizer.utils.AppUtils;
import com.mobilestyx.jlrmaximizer.utils.MCrypt;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    String username1, password1;
    Button login;
    CheckBox checkbox;
    private String encryptedpass, encryptedid;
    private String msgResponse, linkResponse, tokenResponse, mergedLinkWv;

    private EncryptedSharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        checkbox = (CheckBox) findViewById(R.id.checkbox);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username1 = username.getText().toString().trim();
                password1 = password.getText().toString().trim();

                if (username1.length() == 0) {
                    Toast.makeText(getBaseContext(), "Please Enter User ID", Toast.LENGTH_SHORT).show();
                    username.requestFocus();
                    username.setText("");
                } else if (password1.length() == 0) {
                    Toast.makeText(getBaseContext(), "Please Enter Password", Toast.LENGTH_SHORT).show();
                    password.requestFocus();
                    password.setText("");
                } else {
                    doLogin();
                }

            }


        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "jlr_preferences",
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doLogin() {
        MCrypt mcrypt = new MCrypt();
        try {
            encryptedid = MCrypt.bytesToHex(mcrypt.encrypt(username1));
            encryptedpass = MCrypt.bytesToHex(mcrypt.encrypt(password1));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        UserService retrofit = ApiClient.getUserService();
        Call<JsonObject> loginResponseCall = retrofit.userLogin(encryptedid, encryptedpass);
        loginResponseCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("TAG", "onSuccessLogin !" + response.body().toString());
                msgResponse = response.body().getAsJsonObject().get("msg").toString();

                if (msgResponse.equals("success")) {
                    linkResponse = response.body().getAsJsonObject().get("link").toString();
                    tokenResponse = response.body().getAsJsonObject().get("token").toString();

                    if (checkbox.isChecked() == true) {
                        sharedPreferences.edit().putString("user", encryptedid).apply();
                        sharedPreferences.edit().putString("pass", encryptedpass).apply();
                    } else {
                        sharedPreferences.edit().putString("user", "").apply();
                        sharedPreferences.edit().putString("pass", "").apply();
                    }
//                    GlobalVariable.setUrl(mergedLinkWv);
                    Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent1);
                } else if (msgResponse.contains("edit")) {
                    if (checkbox.isChecked() == true) {
                        sharedPreferences.edit().putString("user", encryptedid).apply();
                        sharedPreferences.edit().putString("pass", encryptedpass).apply();
                    } else {
                        sharedPreferences.edit().putString("user", "").apply();
                        sharedPreferences.edit().putString("pass", "").apply();
                    }
                    // GlobalVariable.setUrl(linkResponse);
                    Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent1);

                } else if (msgResponse.contains("Wrong User ID or Password!")) {
                    Log.d("TAG", "onSuccessLoginWrong !" + response.body().toString());
                    showAlertDialog(LoginActivity.this, "Alert!",
                            "Please enter a valid User ID & Password !!", false);

                } else if (msgResponse.contains("User exceeded max login attempt.")) {
                    Log.d("TAG", "onSuccessLoginexceeded !" + response.body().toString());
                    showAlertDialog(LoginActivity.this, "Alert!",
                            "Your account has been disabled for security reasons ! Please try again later in sometime !", false);

                } else {
//                  timeRemaining = null;
                    linkResponse = null;
                    msgResponse = null;
                    Toast.makeText(LoginActivity.this, "Login Unsuccessful", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login Unsuccessful, Please try after sometime", Toast.LENGTH_LONG).show();
                Log.d("TAG", "onFailure: " + t);
            }
        });


    }

    @Override
    public void onBackPressed() {
        AppUtils.showAlertDialog(this, "Confirm !", "Are you sure you want to exit ?", false).show();
    }
}