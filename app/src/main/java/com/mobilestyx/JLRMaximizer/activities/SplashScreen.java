package com.mobilestyx.JLRMaximizer.activities;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;
import static com.mobilestyx.JLRMaximizer.utils.AppUtils.createInfoDialog;
//import static com.mobilestyx.jlrmaximizer.utils.AppUtils.showAlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.gson.JsonObject;
import com.mobilestyx.JLRMaximizer.BuildConfig;
import com.mobilestyx.JLRMaximizer.R;
import com.mobilestyx.JLRMaximizer.remote.ApiClient;
import com.mobilestyx.JLRMaximizer.remote.UserService;
import com.mobilestyx.JLRMaximizer.utils.AppUtils;
import com.scottyab.rootbeer.RootBeer;

import io.michaelrocks.paranoid.Obfuscate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Obfuscate
public class SplashScreen extends AppCompatActivity {

    private static final String TAG = "JLRMaximizerSplash";
    private String latestVersion = null;
    private PackageInfo pinfo = null;
    private String versionCode;

    private AppUpdateManager appUpdateManager;
    Task<AppUpdateInfo> appUpdateInfoTask;
    private int REQUEST_UPDATE_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        appUpdateManager = AppUpdateManagerFactory.create(SplashScreen.this);
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        RootBeer rootBeer = new RootBeer(SplashScreen.this);
        if (rootBeer.isRooted()) {
            Log.e(TAG, "ROOT DETECTED");
            createInfoDialog(SplashScreen.this, "Root Detected!", "Your device is Rooted! Please unroot the device to run the application.");
        } else {
            if (!AppUtils.isInternetOn(SplashScreen.this)) {
                createInfoDialog(SplashScreen.this, "No Internet Connection", "Please check your internet connection & try again !");
            } else {
                checkInAppUpdate();
            }
        }

    }

    public void checkInAppUpdate() {
        Intent i = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(i);
        finish();
//        checkupdate();
        Log.d(TAG, "onSuccess: Update not Available1");
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                Log.d(TAG, "onSuccess: Update not Available2");
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && result.isUpdateTypeAllowed(IMMEDIATE)) {
                    Log.d(TAG, "onSuccess: Update Available3");
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, IMMEDIATE, SplashScreen.this, REQUEST_UPDATE_CODE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onSuccess: Update not Availablerror");
                    Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }


    public void checkupdate() {

        UserService retrofit = ApiClient.getUserService();
        Call<JsonObject> loginResponseCall = retrofit.splashVersion("JLRMax");

        loginResponseCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "SplashVersionCodebody: " + response.body());
                Log.d(TAG, "SplashVersionCodeSuccess: " + response.isSuccessful());
                latestVersion = String.valueOf(response);
                if (!latestVersion.isEmpty() || latestVersion != null) {
                    Log.d(TAG, "!latestVersion.trim().equals(versionCode.trim()): " + latestVersion.trim() + versionCode.trim());
                    if (!latestVersion.trim().equals(versionCode.trim())) {
                        createInfoDialog(SplashScreen.this, "Update Notice !","You are using an outdated version, please uninstall your application and get the latest version from Google Play Store!");
                    } else {
                        Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                } else {
                    createInfoDialog(SplashScreen.this, "Connection Error !","Application is facing difficulties in connecting to server. Please check your data connection or try after sometime");
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!new AppUtils().isInternetOn(SplashScreen.this)) {
            Log.e(" Connection Error", "Internet connection not available");
            createInfoDialog(SplashScreen.this, "No Internet Connection", "Please... Check your internet connection and Try again!");
        } else {
            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo result) {
                    if (result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(result, IMMEDIATE, SplashScreen.this, REQUEST_UPDATE_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Log.e(" Connection Error", "Internet connection not available");
                        createInfoDialog(SplashScreen.this, "No Internet Connection", "Please... Check your internet connection and Try again!");
                    }
                }
            });
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "Update flow failed! Result code: " + resultCode);
        if (requestCode == REQUEST_UPDATE_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                Log.d(TAG, "Update flow failed! Result code: " + resultCode);
                //Toast.makeText(Splash.this,"Update flow failed",Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "Start Download", Toast.LENGTH_SHORT).show();
            if (resultCode != RESULT_OK) {

                Log.d("Result", "Update complete" + resultCode);
                InstallStateUpdatedListener listener = new InstallStateUpdatedListener() {
                    @Override
                    public void onStateUpdate(@NonNull InstallState state) {
                        if (state.installStatus() == InstallStatus.DOWNLOADED) {
                            Log.d(TAG, "An update has been downloaded");
                            appUpdateManager.completeUpdate();
                        }
                    }
                };
                appUpdateManager.registerListener(listener);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
