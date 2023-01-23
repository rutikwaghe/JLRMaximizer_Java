package com.mobilestyx.JLRMaximizer.activities;

import static com.mobilestyx.JLRMaximizer.utils.AppUtils.createInfoDialog;
import static com.mobilestyx.JLRMaximizer.utils.PermissionActivity.PERMISSION_REQUEST_CODE;
import static com.mobilestyx.JLRMaximizer.utils.PermissionsChecker.REQUIRED_PERMISSION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.print.PrintHelper;

import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import com.mobilestyx.JLRMaximizer.R;
import com.mobilestyx.JLRMaximizer.utils.AppUtils;
import com.mobilestyx.JLRMaximizer.utils.CapturePhotoUtils;
import com.mobilestyx.JLRMaximizer.utils.GlobalVariable;
import com.mobilestyx.JLRMaximizer.utils.PermissionActivity;
import com.mobilestyx.JLRMaximizer.utils.PermissionsChecker;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class MainActivity extends Activity {

    private WebView webView;
    boolean loadingFinished = true;
    boolean redirect = false;
    View activity;
    Context context;
    Button printBtn, homeBtn, btn_screenshot;
    boolean statback = true;
    PackageInfo pinfo = null;
    ProgressDialog pDialog, progressDialog;
    private String fileName = "", fileNameCSV = "";
    private static final String TAG = "JLRMaximizer";
    ScrollView scrollview2;
    PermissionsChecker checker;
    boolean isUrlOpenInMobileBrowser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        checker = new PermissionsChecker(this);
        proceedFurther();
    }

    //    @SuppressWarnings("deprecation")
    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    public void proceedFurther() {

        webView = (WebView) findViewById(R.id.webView1);
        homeBtn = (Button) findViewById(R.id.button1);
        printBtn = (Button) findViewById(R.id.button2);
        scrollview2 = (ScrollView) findViewById(R.id.scrollview2);

        homeBtn.setVisibility(View.GONE);
        printBtn.setVisibility(View.GONE);

        activity = getWindow().getDecorView();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(PluginState.OFF);
        webView.getSettings().setUserAgentString(GlobalVariable.getUserAgent());
        webView.getSettings().setLoadWithOverviewMode(true);
        // double tap zoom below
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAllowFileAccess(false);
        context = getApplicationContext();

        startWebView(GlobalVariable.getUrl());
        Log.e(TAG, "proceedFurther:=========================================== " + GlobalVariable.getUrl());
    }


    private void logoutWebview() {
        /** New Loader */
        pDialog = new ProgressDialog(MainActivity.this, R.style.full_screen_dialog) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.fill_dialog);
                getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
        };
        pDialog.getWindow().setBackgroundDrawableResource(R.drawable.splash);
        pDialog.setCancelable(false);
        pDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.clearCache(true);
                webView.clearHistory();
                webView.clearFormData();
                webView.clearCache(true);
                pDialog.dismiss();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("code", "2");
                startActivity(i);
                finish();
//                WebStorage.getInstance().deleteAllData();
//                String keyIdentifer = null;
            }
        }, 1000);
    }


    private void startWebView(String url) {

        webView.setWebViewClient(new WebViewClient() {

            @SuppressLint("MissingPermission")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.e(TAG, "shouldOverrideUrlLoading ================ " + url);

                if (!loadingFinished) {
                    redirect = true;
                }

                if (!AppUtils.isInternetOn(MainActivity.this)) {
                    createInfoDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !");
                } else {
                    if (url.endsWith("?type=relogin")) {
                        webView.loadUrl(getString(R.string.u5));
                        Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
                        i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        String keyIdentifer = null;

                        i2.putExtra("code", "1");// adding additional data using
                        startActivity(i2);
                        finish();

                    }

                    if (url.contains("call")) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 11);

                        } else {
                            String callNumber = url.substring(url.indexOf("call") + 5, url.length());
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + callNumber));
                            startActivity(intent);
                        }
                    }

                    if (url.contains(getString(R.string.u6)) || url.contains(getString(R.string.u69)) || url.contains(getString(R.string.u90)) || url.contains(getString(R.string.u113))) {
                        GlobalVariable.setStatusBackurl(url);

                        printBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                if (!AppUtils.isInternetOn(MainActivity.this)) {
                                    createInfoDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !");
                                } else {
                                    doPhotoPrint(GlobalVariable.getGlobalprint());
                                }
                            }
                        });

                        homeBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (!AppUtils.isInternetOn(MainActivity.this)) {
                                    createInfoDialog(
                                            MainActivity.this,
                                            "No Internet Connection",
                                            "Please check your internet connection & try again !");
                                } else {

                                    homeBtn.setVisibility(View.GONE);
                                    printBtn.setVisibility(View.GONE);
                                    webView.loadUrl(getString(R.string.u7));
                                }
                            }
                        });

                        view.loadUrl(url);
                        loadingFinished = false;

                    }

                    if (url.substring(url.length() - 4).equalsIgnoreCase(".csv")) {

                        if (!AppUtils.isInternetOn(MainActivity.this)) {
                            createInfoDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !");
                        } else {

                            if (checker.lacksPermissions(REQUIRED_PERMISSION)) {


                                PermissionActivity.startActivityForResult(MainActivity.this, PERMISSION_REQUEST_CODE, REQUIRED_PERMISSION);

                            } else {
                                fileNameCSV = url.substring(url.lastIndexOf('/') + 1, url.length());
                                Log.e(TAG, "CSV Name: " + fileNameCSV);

                                try {
                                    fileNameCSV = URLDecoder.decode(fileNameCSV, "UTF-8");
                                } catch (UnsupportedEncodingException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }


                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator +
                                        fileNameCSV);

                                if (file.exists()) {

                                    Log.e("File", "file name " + fileNameCSV + " already exists!");
                                    if (file.delete()) {

                                        Log.e("Hrishi", "File deleted sucessfully!");
                                    } else {

                                        Log.e("Hrishi", "File not deleted!");

                                    }
                                }

                                Log.e("Hrishikesh", "FileName Fetched = " + fileNameCSV);


                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileNameCSV);
                                //You can change the name of the downloads, by changing "download" to everything you want, such as the mWebview title...
                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                dm.enqueue(request);
                            }
                        }
                    }

                    if (url.substring(url.length() - 4).equalsIgnoreCase(".pdf")) {

                        if (!AppUtils.isInternetOn(MainActivity.this)) {
                            createInfoDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !");

                        } else {

                            if (checker.lacksPermissions(REQUIRED_PERMISSION)) {


                                PermissionActivity.startActivityForResult(MainActivity.this, PERMISSION_REQUEST_CODE, REQUIRED_PERMISSION);

                            } else {

                                fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
                                Log.e(TAG, "PDF Name: " + fileName);

                                try {
                                    fileName = URLDecoder.decode(fileName, "UTF-8");
                                } catch (UnsupportedEncodingException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }


                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator +
                                        fileName);

                                if (file.exists()) {

                                    Log.e("File", "file name " + fileName + " already exists!");
                                    if (file.delete()) {

                                        Log.e("Hrishi", "File deleted sucessfully!");
                                    } else {

                                        Log.e("Hrishi", "File not deleted!");

                                    }
                                }

                                Log.e("Hrishikesh", "FileName Fetched = " + fileName);


                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                                //You can change the name of the downloads, by changing "download" to everything you want, such as the mWebview title...
                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                dm.enqueue(request);
                            }
                        }
                    }

                    if (!(url.startsWith("https://www.jlrmaximizer.in")) && !(url.startsWith("https://data.findmeacar.in/max")) && (!(url.startsWith("https://sa.jlrconnect.com/jlrfeed")))
                            && (!(url.startsWith("http://drive.google.com"))) && (!(url.startsWith("https://drive.google.com")))) {

                        if (url.startsWith("https://www.youtube.com")) {

                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                            isUrlOpenInMobileBrowser = true;

                        } else {

                            isUrlOpenInMobileBrowser = true;
                            Log.e(TAG, "This url is matched for this method=========" + isUrlOpenInMobileBrowser);
                            openUrlInBrowser(url);
                        }
                    }

                    if (url.contains(getString(R.string.u30))) {
                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (url.contains(getString(R.string.u33))) {

                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (url.contains(getString(R.string.u7))) {

                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (GlobalVariable.getUrl().contains(getString(R.string.u43))) {

                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                        if (url.contains("https://www.jlrmaximizer.in/reports/admin_stats?screenshot")) {

                            if (checker.lacksPermissions(REQUIRED_PERMISSION)) {

                                PermissionActivity.startActivityForResult(MainActivity.this, PERMISSION_REQUEST_CODE, REQUIRED_PERMISSION);

                            } else {

                                /*This code is added by Deepak Tiwari to take screenshot*/
                                webView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                                webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
                                webView.setDrawingCacheEnabled(true);
                                webView.buildDrawingCache();

                                Bitmap bm = Bitmap.createBitmap(webView.getMeasuredWidth(), webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                Canvas bigcanvas = new Canvas(bm);
                                Paint paint = new Paint();
                                int iHeight = bm.getHeight();
                                bigcanvas.drawBitmap(bm, 0, iHeight, paint);
                                webView.draw(bigcanvas);


                                if (bm != null) {

                                    //MediaStore.Images.Media.insertImage(getContentResolver(), bm, "JLRMaximizer", "ScreenShot");
                                    CapturePhotoUtils.insertImage(getContentResolver(), bm, "JLRMaximizer", "ScreenShot");
                                    Toast.makeText(MainActivity.this, "Screenshot successfully saved into gallery", Toast.LENGTH_SHORT).show();


                                }
                            }
                        }
                    } else if (url.contains(getString(R.string.u30))) {

                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (url.contains(getString(R.string.u33))) {

                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);
                    }

                    if (url.contains(getString(R.string.u8))) {
                        String urlsub = url;
                        String urlsub2 = url;
                        String surl = urlsub.substring(urlsub.indexOf("download") + 8, urlsub.length());
                        GlobalVariable.setUrl(surl);
                        url = getString(R.string.u9) + surl;

                        // Capture Rid for pdf
                        String pdfurl = urlsub2.substring(urlsub2.indexOf("download") + 13, urlsub2.indexOf("&"));

                        String printurl = getString(R.string.u10) + pdfurl
                                + ".jpg";

                        GlobalVariable.setGlobalprint(printurl);

                        statback = true;
                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
//                        homeBtn.setVisibility(View.VISIBLE);
//                        printBtn.setVisibility(View.VISIBLE);
                        // btn_screenshot.setVisibility(View.GONE);
                    }

                    if (url.contains(getString(R.string.u97))) {
                        String urlsub = url;
                        String urlsub2 = url;
                        String surl = urlsub.substring(urlsub.indexOf("download") + 8, urlsub.length());

                        Log.e(TAG, "surl = " + surl);

                        GlobalVariable.setUrl(surl);
                        url = getString(R.string.u98) + surl;
                        Log.e(TAG, "url = " + url);
                        Log.d(TAG, "MOBU98");

                        // Capture Rid for pdf
                        String pdfurl = urlsub2.substring(
                                urlsub2.indexOf("download") + 13,
                                urlsub2.indexOf("&"));

                        Log.e(TAG, "pdfurl = " + pdfurl);

                        String printurl = getString(R.string.u99) + pdfurl
                                + ".jpg";
                        Log.d(TAG, "MOBU10");
                        GlobalVariable.setGlobalprint(printurl);
                        Log.e(TAG, "printurl = " + printurl);
                        statback = true;

                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
//                        homeBtn.setVisibility(View.VISIBLE);
//                        printBtn.setVisibility(View.VISIBLE);
                        // btn_screenshot.setVisibility(View.GONE);

                    }

                    if (url.contains(getString(R.string.u120))) {
                        //		 Log.d(TAG, "INMOBU97");

                        String urlsub = url;
                        String urlsub2 = url;
                        String surl = urlsub.substring(
                                urlsub.indexOf("download") + 8, urlsub.length());

                        //		Log.e(TAG, "surl = "+surl);

                        GlobalVariable.setUrl(surl);
                        url = getString(R.string.u121) + surl;
                        String pdfurl = urlsub2.substring(
                                urlsub2.indexOf("download") + 13,
                                urlsub2.indexOf("&"));

                        String printurl = getString(R.string.u122) + pdfurl
                                + ".jpg";
                        //		Log.d(TAG, "MOBU10");
                        GlobalVariable.setGlobalprint(printurl);
                        //		Log.e(TAG, "printurl = "+printurl);
                        statback = true;

                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
//                        homeBtn.setVisibility(View.VISIBLE);
//                        printBtn.setVisibility(View.VISIBLE);
                        // btn_screenshot.setVisibility(View.GONE);

                    }

                    if (url.contains(getString(R.string.u11))) {
                        //			 Log.d(TAG, "MOBU11");
                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
//                        homeBtn.setVisibility(View.VISIBLE);
//                        printBtn.setVisibility(View.VISIBLE);

                    }

                    // Dealer

                    if (url.contains(getString(R.string.u12)) || url.contains(getString(R.string.u83)) || url.contains(getString(R.string.u106))) {
                        //		 Log.d(TAG, "INMOBU12 & 83");
                        String uidsub = url;
                        String surl = uidsub.substring(
                                uidsub.indexOf("reports/") + 8, uidsub.length());

                        GlobalVariable.setUid(surl);

                        //	Log.e(TAG, "surl = "+surl);

                        view.loadUrl(url);

                    }
                    if (url.contains(getString(R.string.u13))) {
                        //		 Log.d(TAG, "INMOBU13");
                        String uidsub = url;
                        String surl = uidsub.substring(uidsub.indexOf("reports/") + 20, uidsub.length());
                        GlobalVariable.setDuid(surl);
                        view.loadUrl(url);

                    } else {


                        if (url.substring(url.length() - 4).equalsIgnoreCase(".pdf")) {

                            webView.getSettings().setLoadsImagesAutomatically(true);
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.setVerticalScrollBarEnabled(true);
                            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                            view.loadUrl(url);

                        } else {

                            view.loadUrl(url);
                            loadingFinished = false;
                            webView.getSettings().setBuiltInZoomControls(false);
                            webView.getSettings().setSupportZoom(false);
                            webView.getSettings().setDisplayZoomControls(false);

                        }
                    }
                    if (url.contains(getString(R.string.u14))) {
                        webView.loadUrl(getString(R.string.u14));
                        webView.loadUrl(getString(R.string.u15));
                        logoutWebview();
//                        logout LogoutTask = new logout();
//                        LogoutTask.execute();


                    } else if (url.equals(getString(R.string.u16))
                            || url.equals(getString(R.string.u17))
                            || url.equals(getString(R.string.u18))
                            || url.equals(getString(R.string.u19))
                            || url.equals(getString(R.string.u20))
                            || url.equals(getString(R.string.u21))) {
                        //			 Log.d(TAG, "INMOBU1621");
                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
                        webView.getSettings().setUseWideViewPort(true);

                    } else if (url.contains(getString(R.string.u22))) {

                        //			 Log.d(TAG, "INMOBU22");
                        GlobalVariable.setOnlybackurl(url);
                    }
                    /*This lines of code is added by Deepak Tiwari*/
                    else if (url.contains(getString(R.string.u79))) {

                    } else if (url.contains(getString(R.string.u23))) {

                        GlobalVariable.setOnlybackurl(url);

                    } else if (url.contains(getString(R.string.u7))) {

                    } else if (url.contains(getString(R.string.u40))) {

                    } else if (url.contains(getString(R.string.u24))) {
                        //			 Log.d(TAG, "INMOBU24");
                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.contains(getString(R.string.u25))) {
                        //			 Log.d(TAG, "INMOBU25");
                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.contains(getString(R.string.u26))) {

                        //				 Log.d(TAG, "INMOBU26");
                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.contains(getString(R.string.u27))) {

                        //			 Log.d(TAG, "INMOBU27");
                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.contains(getString(R.string.u28))) {

                        //			 Log.d(TAG, "INMOBU28");
                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.contains(getString(R.string.u68))) {

                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.contains(getString(R.string.u39))) {

                        /*This lines of code is added by Deepak Tiwari*/
                        // btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                    } else if (url.contains(getString(R.string.u70))) {

                        /*This lines of code is added by Deepak Tiwari*/
                        //btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                        //			 Log.d(TAG, "INMOBU28");
                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.equals("https://www.jlrmaximizer.in/prospect_calling")) {

                        /*This lines of code is added by Deepak Tiwari*/
                        // btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                    } else if (url.equals("https://sa.jlrconnect.com/jlrfeed/getdata")) {

                        /*This lines of code is added by Deepak Tiwari*/
                        // btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                    } else if (url.contains(getString(R.string.u101))) {

                        /*This lines of code is added by Deepak Tiwari*/
                        // btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                    } else if (url.contains(getString(R.string.u71))) {

                        //			 Log.d(TAG, "INMOBU28");
                        GlobalVariable.setOnlybackurl(url);
                    } else if (url.contains(getString(R.string.u72))) {

                        /*This lines of code is added by Deepak Tiwari*/
                        //  btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                    } else if (url.contains(getString(R.string.u84)) || url.contains(getString(R.string.u85)) || url.contains(getString(R.string.u86)) || url.contains(getString(R.string.u87)) || url.contains(getString(R.string.u88))) {

                        GlobalVariable.setOnlybackurl(url);
                        //			Log.d(TAG, "INMOBU84-88");
                    } else if (url.contains(getString(R.string.u107)) || url.contains(getString(R.string.u108)) || url.contains(getString(R.string.u109)) || url.contains(getString(R.string.u110)) || url.contains(getString(R.string.u111))) {

                        GlobalVariable.setOnlybackurl(url);
                        //			Log.d(TAG, "INMOBU84-88");
                    }
                }

                return true;
            }


            public void onPageFinished(final WebView view, String url) {
                if (
                        url.contains(getString(R.string.u9)) ||
                        url.contains(getString(R.string.u8)) ||
                        url.contains(getString(R.string.u11)) ||
                        url.contains(getString(R.string.u120))
                ) {
                    Log.d(TAG, "onPageFinished: " + url);
                    homeBtn.setVisibility(View.VISIBLE);
                    printBtn.setVisibility(View.VISIBLE);
                }

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (!redirect) {
                    loadingFinished = true;
                }
                if (loadingFinished && !redirect) {
                } else {
                    redirect = false;
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                try {
                    webView.stopLoading();
                } catch (Exception e) {
                }
                createInfoDialog(MainActivity.this, "No Internet Connection",
                        "Please check your internet connection & try again !");
                super.onReceivedError(webView, errorCode, description,
                        failingUrl);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                if (url.contains(getString(R.string.u30))
                        || url.equals(getString(R.string.u31))
                        || url.endsWith("?type=relogin")
                        || url.equals(getString(R.string.u32))) {
                    Log.d(TAG, "onPageStarted: ");
                } else {
                    if (progressDialog == null || progressDialog.isShowing() == false) {

                        progressDialog = new ProgressDialog(MainActivity.this, R.style.full_screen_dialog) {
                            @Override
                            protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                setContentView(R.layout.fill_dialog);
                                getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                            }
                        };
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }

                }

            }

        });

        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                if (progress >= 100) {
                }
            }

        });
        webView.loadUrl(url);
    }

    /**
     * BITMAP FROM URL
     */
    public static InputStream getBitmapFromURL(String src) {
        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            // Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return input;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Photo Print SCALE MODE FIT
     */
    private void doPhotoPrint(String urlprint) {
        String abcc = urlprint;
        PrintHelper photoPrinter = new PrintHelper(this);//can use getActivity() here

        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);

        Bitmap bitmap = BitmapFactory.decodeStream(getBitmapFromURL(abcc));

        photoPrinter.printBitmap("JLR Pdf", bitmap);
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onBackPressed() {
        if (!AppUtils.isInternetOn(MainActivity.this)) {
            createInfoDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !");
        } else {

            if (webView.getUrl().startsWith("https://drive.google.com/")) {

                webView.loadUrl("https://www.jlrmaximizer.in/resources");

                Log.e(TAG, "onBackPressed: " + getString(R.string.u101));
            } else if (isUrlOpenInMobileBrowser) {

                webView.loadUrl(getString(R.string.u101));

            } else if (webView.getUrl().equals(getString(R.string.u33))) {
                Log.d(TAG, "INMOBU33");
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();

            } else if (webView.getUrl().equals(getString(R.string.u34))) {

                Log.d(TAG, "INMOBU34");

            } else if (webView.getUrl().equals("https://sa.jlrconnect.com/jlrfeed/getdata")) {

                Log.d(TAG, "JlrFeedAPI Called Here");
                webView.loadUrl(getString(R.string.u7));

            } else if (webView.getUrl().equals(getString(R.string.u35))) {
                Log.d(TAG, "INMOBU35");
                webView.loadUrl(getString(R.string.u36));
                Log.d(TAG, "MOBU36");

            } else if (webView.getUrl().equals(getString(R.string.u37))) {

                Log.d(TAG, "INMOBU37");
                webView.loadUrl(getString(R.string.u36));
                Log.d(TAG, "MOBU36-1");
            } else if (webView.getUrl().equals(getString(R.string.u38))) {

                Log.d(TAG, "INMOBU38");
                webView.loadUrl(getString(R.string.u36));


            } else if (webView.getUrl().equals(getString(R.string.u94))) {

                Log.d(TAG, "INMOBU94");
                webView.loadUrl(getString(R.string.u36));


            } else if (webView.getUrl().equals(getString(R.string.u117))) {

                Log.d(TAG, "INMOBU94");
                webView.loadUrl(getString(R.string.u36));


            } else if (webView.getUrl().equals(getString(R.string.u39))) {
                Log.d(TAG, "INMOBU39");
                webView.loadUrl(getString(R.string.u36));
                Log.d(TAG, "MOBU36-3");
            } else if (webView.getUrl().equals(getString(R.string.u40))) {
                Log.d(TAG, "INMOBU40");
                webView.loadUrl(getString(R.string.u36));
                Log.d(TAG, "MOBU36-4");
            } else if (webView.getUrl().equals(getString(R.string.u41))) {
                Log.d(TAG, "INMOBU41");
                webView.loadUrl(getString(R.string.u38));
                Log.d(TAG, "MOBU38-1");
            } else if (webView.getUrl().equals(getString(R.string.u22))) {
                Log.d(TAG, "INMOBU22-1");
                webView.loadUrl(getString(R.string.u38));
                Log.d(TAG, "MOBU38-2");
            } else if (webView.getUrl().equals(getString(R.string.u23))) {
                Log.d(TAG, "INMOBU23-1");
                webView.loadUrl(getString(R.string.u38));
                Log.d(TAG, "MOBU38-3");
            } else if (webView.getUrl().equals(getString(R.string.u24))) {
                Log.d(TAG, "INMOBU24-1");
                webView.loadUrl(getString(R.string.u38));
                Log.d(TAG, "MOBU38-4");
            } else if (webView.getUrl().equals(getString(R.string.u26))) {

                Log.d(TAG, "INMOBU26-1");
                webView.loadUrl(getString(R.string.u38));
                Log.d(TAG, "MOBU38-5");
            } else if (webView.getUrl().equals(getString(R.string.u103))) {

                Log.d(TAG, "INMOBU103");
                webView.loadUrl(getString(R.string.u38));

            }

            /** PENDING */
            else if (webView.getUrl().contains(getString(R.string.u42))) {

                Log.d(TAG, "INMOBU42");
                webView.loadUrl(getString(R.string.u43));
                Log.d(TAG, "MOBU43");

            }

            /** PENDING */
            else if (webView.getUrl().contains(getString(R.string.u44)) || (webView.getUrl().contains(getString(R.string.u81)) || webView.getUrl().contains(getString(R.string.u104)))) {
                Log.d(TAG, "INMOBU44 || 81");
                webView.loadUrl(getString(R.string.u45));
                Log.d(TAG, "MOBU45");

            } else if (webView.getUrl().contains(getString(R.string.u46))) {
                Log.d(TAG, "INMOBU46");
                webView.loadUrl(GlobalVariable.getUrl());

            } else if (webView.getUrl().contains(getString(R.string.u47)) || webView.getUrl().contains(getString(R.string.u100)) || webView.getUrl().contains(getString(R.string.u123))) {
                Log.d(TAG, "INMOBU47 & 100");
                homeBtn.setVisibility(View.GONE);
                printBtn.setVisibility(View.GONE);
//                btn_screenshot.setVisibility(View.GONE);
                webView.loadUrl(GlobalVariable.getStatusBackurl());

            } else if (webView.getUrl().contains(getString(R.string.u48))) {
                Log.d(TAG, "INMOBU48");
                webView.loadUrl(getString(R.string.u38)
                        + GlobalVariable.getUid());
                Log.d(TAG, "MOBU38-XA");
            } else if (webView.getUrl().contains(getString(R.string.u93))) {
                Log.d(TAG, "INMOBU93");
                webView.loadUrl(getString(R.string.u94)
                        + GlobalVariable.getUid());
                Log.d(TAG, "MOBU93-XA");
            } else if (webView.getUrl().contains(getString(R.string.u116))) {
                Log.d(TAG, "INMOBU116");
                webView.loadUrl(getString(R.string.u117)
                        + GlobalVariable.getUid());
                Log.d(TAG, "MOBU117-XA");
            } else if (webView.getUrl().contains(getString(R.string.u49))) {
                Log.d(TAG, "INMOBU49");
                if (webView.getUrl().contains("&utr=ADMIN")) {

                    webView.loadUrl(getString(R.string.u43));
                    Log.d(TAG, "MOBU43-SD");
                } else if (webView.getUrl().contains("&utr=DLR")) {
                    webView.loadUrl(getString(R.string.u45));
                    Log.d(TAG, "MOBU45-HF");
                }

            }
            //new
            else if (webView.getUrl().contains(getString(R.string.u95))) {
                Log.d(TAG, "INMOBU95");
                if (webView.getUrl().contains("&utr=ADMIN")) {

                    webView.loadUrl(getString(R.string.u96));
                    Log.d(TAG, "MOBU95-SD");
                } else if (webView.getUrl().contains("&utr=DLR")) {

                    webView.loadUrl(getString(R.string.u81));
                    Log.d(TAG, "MOBU95-HF");
                }

            } else if (webView.getUrl().contains(getString(R.string.u118))) {
                Log.d(TAG, "INMOBU95");
                if (webView.getUrl().contains("&utr=ADMIN")) {

                    webView.loadUrl(getString(R.string.u119));
                    Log.d(TAG, "MOBU119-SD");
                } else if (webView.getUrl().contains("&utr=DLR")) {

                    webView.loadUrl(getString(R.string.u104));
                    Log.d(TAG, "MOBU119-HF");
                }

            } else if (webView.getUrl().contains(getString(R.string.u50)) || webView.getUrl().contains(getString(R.string.u69))) {
                Log.d(TAG, "INMOBU50");
                if (webView.getUrl().contains("&ut=DLR")
                        || webView.getUrl().contains("&ut=ADMIN")
                        || webView.getUrl().contains("&ut=SDUSR")
                        || webView.getUrl().contains("&ut=SUSR")) {
                    Log.d(TAG, "IF INMOBU50");
                    String dealerbackurl = getString(R.string.u51)
                            + GlobalVariable.getUid();

                    webView.loadUrl(dealerbackurl);

                    Log.e(TAG, "dealerbackuyrl = " + dealerbackurl);


                } else {
                    Log.d(TAG, "ELSE INMOBU50");
                    Log.e(TAG, "Calling = " + GlobalVariable.getOnlybackurl());
                    webView.loadUrl(GlobalVariable.getOnlybackurl());
                }

            } else if (webView.getUrl().contains(getString(R.string.u90))) {
                Log.d(TAG, "INMOBU92");
                if (webView.getUrl().contains("&ut=DLR")
                        || webView.getUrl().contains("&ut=ADMIN")
                        || webView.getUrl().contains("&ut=SDUSR")
                        || webView.getUrl().contains("&ut=SUSR")) {
                    Log.d(TAG, "IF INMOBU92");
                    String dealerbackurl = getString(R.string.u92)
                            + GlobalVariable.getUid();

                    webView.loadUrl(dealerbackurl);

                    Log.e(TAG, "dealerbackuyrl = " + dealerbackurl);


                } else {
                    Log.d(TAG, "ELSE INMOBU92");
                    Log.e(TAG, "Calling = " + GlobalVariable.getOnlybackurl());
                    webView.loadUrl(GlobalVariable.getOnlybackurl());
                }

            } else if (webView.getUrl().contains(getString(R.string.u113))) {
                Log.d(TAG, "INMOBU113");
                if (webView.getUrl().contains("&ut=DLR")
                        || webView.getUrl().contains("&ut=ADMIN")
                        || webView.getUrl().contains("&ut=SDUSR")
                        || webView.getUrl().contains("&ut=SUSR")) {
                    Log.d(TAG, "IF INMOBU113");
                    String dealerbackurl = getString(R.string.u115)
                            + GlobalVariable.getUid();

                    webView.loadUrl(dealerbackurl);

                    Log.e(TAG, "dealerbackuyrl = " + dealerbackurl);


                } else {
                    Log.d(TAG, "ELSE INMOBU92");
                    Log.e(TAG, "Calling = " + GlobalVariable.getOnlybackurl());
                    webView.loadUrl(GlobalVariable.getOnlybackurl());
                }

            } else if (webView.getUrl().equals(getString(R.string.u52))) {
                Log.d(TAG, "INMOBU52");
                webView.loadUrl(getString(R.string.u34));
                Log.d(TAG, "MOBU34-JY");

            } else if (webView.getUrl().equals(getString(R.string.u53))
                    || webView.getUrl().equals(getString(R.string.u54))) {

                Log.d(TAG, "INMOBU5354");
                webView.loadUrl(getString(R.string.u52));
                Log.d(TAG, "MOBU52-EM");

            } else if (webView.getUrl().equals(getString(R.string.u55))
                    || webView.getUrl().equals(getString(R.string.u56))
                    || webView.getUrl().equals(getString(R.string.u57))
                    || webView.getUrl().equals(getString(R.string.u58))) {

                Log.d(TAG, "INMOBU5558");
                webView.loadUrl(getString(R.string.u53));
                Log.d(TAG, "MOBU53-NY");

            } else if (webView.getUrl().equals(getString(R.string.u59))
                    || webView.getUrl().equals(getString(R.string.u60))
                    || webView.getUrl().equals(getString(R.string.u61))
                    || webView.getUrl().equals(getString(R.string.u62))
                    || webView.getUrl().equals(getString(R.string.u63))
                    || webView.getUrl().equals(getString(R.string.u64))
                    || webView.getUrl().equals(getString(R.string.u65))
                    || webView.getUrl().equals(getString(R.string.u66))) {
                Log.d(TAG, "INMOBU5966");
                webView.loadUrl(getString(R.string.u54));
                Log.d(TAG, "INMOBU54-TR");

            } else if (webView.getUrl().equals(getString(R.string.u68))) {

                webView.loadUrl(getString(R.string.u34));
            } else if (webView.getUrl().equals(getString(R.string.u70))) {

                webView.loadUrl(getString(R.string.u34));
            } else if (webView.getUrl().equals(getString(R.string.u71))) {

                webView.loadUrl(getString(R.string.u34));
            } else if (webView.getUrl().equals(getString(R.string.u72)) || webView.getUrl().equals(getString(R.string.u101)) || webView.getUrl().equals(getString(R.string.u102))) {

                webView.loadUrl(getString(R.string.u34));
            } else if (webView.getUrl().equals(getString(R.string.u73))) {

                webView.loadUrl(getString(R.string.u34));
            } else if (webView.getUrl().equals(getString(R.string.u74))) {

                webView.loadUrl(getString(R.string.u34));
            } else if (webView.getUrl().contains(getString(R.string.u75))) {

                webView.loadUrl(getString(R.string.u45));
            } else if (webView.getUrl().contains(getString(R.string.u76))) {

                webView.loadUrl(getString(R.string.u45));
            } else if (webView.getUrl().equals(getString(R.string.u77)) || webView.getUrl().equals(getString(R.string.u80)) || webView.getUrl().equals(getString(R.string.u78))) {


                webView.loadUrl(getString(R.string.u79));
                Log.e("Hrishi", "back from stock");

            } else if (webView.getUrl().contains(getString(R.string.u82))) {

                webView.loadUrl(getString(R.string.u81));
                Log.d(TAG, "INMOBU82");

            } else if (webView.getUrl().contains(getString(R.string.u105))) {

                webView.loadUrl(getString(R.string.u104));
                Log.d(TAG, "INMOBU105");

            } else if (webView.getUrl().contains(getString(R.string.u83))) {

                webView.loadUrl(getString(R.string.u81));
                Log.d(TAG, "INMOBU83");

            } else if (webView.getUrl().contains(getString(R.string.u106))) {

                webView.loadUrl(getString(R.string.u104));
                Log.d(TAG, "INMOBU106");

            } else if (webView.getUrl().equals(getString(R.string.u84))
                    || webView.getUrl().equals(getString(R.string.u85))
                    || webView.getUrl().equals(getString(R.string.u86))
                    || webView.getUrl().equals(getString(R.string.u87))
                    || webView.getUrl().equals(getString(R.string.u88))) {
                Log.d(TAG, "INMOBU84-88");
                webView.loadUrl(getString(R.string.u89));
                Log.d(TAG, "INMOBU89");

            } else if (webView.getUrl().equals(getString(R.string.u107))
                    || webView.getUrl().equals(getString(R.string.u108))
                    || webView.getUrl().equals(getString(R.string.u109))
                    || webView.getUrl().equals(getString(R.string.u110))
                    || webView.getUrl().equals(getString(R.string.u111))) {
                Log.d(TAG, "INMOBU107-111");
                webView.loadUrl(getString(R.string.u112));
                Log.d(TAG, "INMOBU111");

            } else if (webView.getUrl().contains(getString(R.string.u92))) {

                webView.loadUrl(getString(R.string.u89));
                Log.d(TAG, "INMOBU92");

            } else if (webView.getUrl().contains(getString(R.string.u115))) {

                webView.loadUrl(getString(R.string.u112));
                Log.d(TAG, "INMOBU92");

            } else {
                Log.d(TAG, "None- ELSE");
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            trimCache(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
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
    protected void onStop() {
        super.onStop();
        // WebService.invokeHelloWorldWS("FooFoo", "booboo", "Post_PDF_App");
    }

    public void openUrlInBrowser(String url) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    //    private void doPhotoPrint(String urlprint) {
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//            Toast.makeText(getApplicationContext(),
//                    "Please update your device to KitKat or above to use the print facility or directly E-Mail the PDF.", Toast.LENGTH_LONG).show();
//        } else {
//            try {
//                String abcc = urlprint;
//                Log.d(TAG, "doPhotoPrintdoPhotoPrintdoPhotoPrint  " + abcc);
//                PrintHelper photoPrinter = new PrintHelper(getApplicationContext());
//                //PrintHelper photoPrinter = new PrintHelper(this);
//                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
//                Bitmap bitmap = BitmapFactory.decodeStream(getBitmapFromURL(abcc));
//                photoPrinter.printBitmap("JLR Pdf", bitmap);
//            } catch (Exception e) {
//                Log.d(TAG, "doPhotoPrintdoPhotoPrintdoPhotoPrint  " + e);
//                e.printStackTrace();
//            }
//
//        }
//    }

//    @SuppressWarnings("deprecation")
//    public void showAlertDialog(Context context, String title, String message,
//                                final Boolean status) {
//        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//        alertDialog.setCancelable(false);
//
//        alertDialog.setTitle(title);
//        alertDialog.setMessage(message);
//        if (status) {
//            alertDialog.setButton("Update",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                            startActivity(new Intent(
//                                    Intent.ACTION_VIEW,
//                                    Uri.parse("https://play.google.com/store/apps/details?id=com.mobilestyx.JLRMaximizer")));
//                        }
//                    });
//        } else {
//            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    // finish();
//                    // System.exit(0);
//                }
//            });
//        }
//
//        alertDialog.show();
//    }

//    private static TrustManager[] trustManagers;
//
//    public static class _FakeX509TrustManager implements
//            javax.net.ssl.X509TrustManager {
//        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};
//
//        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
//                throws CertificateException {
//        }
//
//        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
//                throws CertificateException {
//        }
//
//        public X509Certificate[] getAcceptedIssuers() {
//            return (_AcceptedIssuers);
//        }
//    }

}
