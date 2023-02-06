package com.mobilestyx.JLRMaximizer.activities;

import static com.mobilestyx.JLRMaximizer.utils.AppUtils.createInfoDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.JsonObject;
import com.mobilestyx.JLRMaximizer.R;
import com.mobilestyx.JLRMaximizer.remote.ApiClient;
import com.mobilestyx.JLRMaximizer.remote.UserService;
import com.mobilestyx.JLRMaximizer.utils.AppUtils;
import com.mobilestyx.JLRMaximizer.utils.GlobalVariable;
import com.mobilestyx.JLRMaximizer.utils.MCrypt;

import java.io.File;

import io.michaelrocks.paranoid.Obfuscate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Obfuscate
public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    String username1, password1;
    Button login;
    CheckBox checkbox;
    public ProgressDialog pDialog;
    public String APP_NAME;
    private String encryptedUsername, encryptedPass;
    private String msgResponse, linkResponse, tokenResponse, mergedLinkWv;
    private EncryptedSharedPreferences sharedPreferences;

    MCrypt mcrypt = new MCrypt();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String userAgent = new WebView(this).getSettings().getUserAgentString();
        GlobalVariable.setUserAgent(userAgent);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        APP_NAME = getString(R.string.app_name);

        try {
            Intent intent = getIntent();
            Bundle bd = intent.getExtras();
            if (bd != null) {
                String getName = intent.getStringExtra("code");
                if ((getName != null) && (getName == "1" || getName.equalsIgnoreCase("1"))) {
                    createInfoDialog(LoginActivity.this, "Alert!", "Please login with your new password !");
                } else if ((getName != null) && (getName == "2" || getName.equalsIgnoreCase("2"))) {
                    delayLogout();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username1 = username.getText().toString().trim();
                password1 = password.getText().toString().trim();
                if (username1.length() == 0) {
                    Toast.makeText(LoginActivity.this, "Please Enter User ID", Toast.LENGTH_SHORT).show();
                    username.requestFocus();
                    username.setText("");
                } else if (password1.length() == 0) {
                    Toast.makeText(LoginActivity.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                    password.requestFocus();
                    password.setText("");
                } else {
                    try {
                        doLogin();
                    } catch (Exception e) {
                        createInfoDialog(LoginActivity.this, APP_NAME, "Something went wrong, Please try again later");
                        e.printStackTrace();
                    }
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
            createInfoDialog(LoginActivity.this, "Network Connection", "Something went wrong, Please try after sometime!");
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
            createInfoDialog(LoginActivity.this, "Network Connection", "Something went wrong, Please try after sometime!");
        }

        try {
            String name = sharedPreferences.getString("user", "");
            String sharedId = new String(mcrypt.decrypt(name));
            username.setText(sharedId.trim());

            String pass = sharedPreferences.getString("pass", "");
            String sharedPass = new String(mcrypt.decrypt(pass));
            password.setText(sharedPass.trim());

            if (!sharedId.equals("")) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        findViewById(R.id.l2_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

    }

    public void doLogin() {
        //start loader
        pDialog = new ProgressDialog(LoginActivity.this, R.style.full_screen_dialog) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.fill_dialog);
                getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        };
        pDialog.setCancelable(false);
        pDialog.show();

        MCrypt mcrypt = new MCrypt();
        try {
            encryptedUsername = MCrypt.bytesToHex(mcrypt.encrypt(username1));
            encryptedPass = MCrypt.bytesToHex(mcrypt.encrypt(password1));
        } catch (Exception e) {
            e.printStackTrace();
            createInfoDialog(LoginActivity.this, "Network Connection", "Something went wrong, Please try after sometime!");
        }

        if (!new AppUtils().isInternetOn(LoginActivity.this)) {
            createInfoDialog(LoginActivity.this, "No Internet Connection", "Please... Check your internet connection and Try again!");
        } else {

            UserService retrofit = ApiClient.getUserService();
            Call<JsonObject> loginResponseCall = retrofit.userLogin(encryptedUsername, encryptedPass);

            loginResponseCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    pDialog.dismiss();

                    if (response != null && response.body() != null) {
                        if (response.isSuccessful()) {
                            try {
                                msgResponse = response.body().getAsJsonObject().get("msg").toString();
                                if (msgResponse.contains("success")) {

                                    linkResponse = response.body().getAsJsonObject().get("link").toString();
                                    tokenResponse = response.body().getAsJsonObject().get("token").toString();
                                    tokenResponse = tokenResponse.substring(1, tokenResponse.length() - 1);
                                    mergedLinkWv = getString(R.string.ulogin) + tokenResponse;

                                    if (checkbox.isChecked() == true) {
                                        sharedPreferences.edit().putString("user", encryptedUsername).apply();
                                        sharedPreferences.edit().putString("pass", encryptedPass).apply();
                                    } else {
                                        sharedPreferences.edit().putString("user", "").apply();
                                        sharedPreferences.edit().putString("pass", "").apply();
                                    }
                                    GlobalVariable.setUrl(mergedLinkWv);
                                    Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent1);
                                } else if (msgResponse.contains("edit")) {
                                    if (checkbox.isChecked() == true) {
                                        sharedPreferences.edit().putString("user", encryptedUsername).apply();
                                        sharedPreferences.edit().putString("pass", encryptedPass).apply();
                                    } else {
                                        sharedPreferences.edit().putString("user", "").apply();
                                        sharedPreferences.edit().putString("pass", "").apply();
                                    }
                                    GlobalVariable.setUrl(linkResponse);
                                    Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent1);

                                } else if (msgResponse.contains("Wrong User ID or Password!")) {
                                    Log.d("TAG", "onSuccessLoginWrong !" + response.body().toString());
                                    createInfoDialog(LoginActivity.this, APP_NAME, "Please enter a valid User ID & Password !!");

                                } else if (msgResponse.contains("User exceeded max login attempt.")) {
                                    Log.d("TAG", "onSuccessLoginexceeded !" + response.body().toString());
                                    createInfoDialog(LoginActivity.this, APP_NAME, "Your account has been disabled for security reasons ! Please try again later in sometime !");

                                } else {
                                    linkResponse = null;
                                    msgResponse = null;
                                    Toast.makeText(LoginActivity.this, "Login Unsuccessful!", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Something Went Wrong, Please try after sometime!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Unsuccessful!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Unsuccessful!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    pDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Login Unsuccessful!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void delayLogout() {
        pDialog = new ProgressDialog(LoginActivity.this, R.style.full_screen_dialog) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.fill_dialog);
                getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        };
//        pDialog.getWindow().setBackgroundDrawableResource(R.drawable.splash);
        pDialog.setCancelable(false);
        pDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pDialog.dismiss();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            trimCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void trimCache(Context context) {
        try {
            String pathadmob = this.getFilesDir().getParent() + "/app_webview";
            File dir = new File(pathadmob);
            if (dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir != null && dir.delete();

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(APP_NAME)
                .setMessage("Confirm !\nAre you sure you want to exit ?")
                .setCancelable(false)
                .setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                    dialog.cancel();
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

