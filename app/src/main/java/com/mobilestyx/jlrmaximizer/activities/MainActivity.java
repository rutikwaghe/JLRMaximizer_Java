package com.mobilestyx.jlrmaximizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import static com.mobilestyx.jlrmaximizer.PermissionsActivity.PERMISSION_REQUEST_CODE;
import static com.mobilestyx.jlrmaximizer.PermissionsChecker.REQUIRED_PERMISSION;

//import android.support.v4.print.PrintHelper;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressDialog progressDialog;
    boolean loadingFinished = true;
    boolean redirect = false;
    View activity;
    // Button btnPrint;
    public static float width;
    public static float height;
    Context context;
    RelativeLayout.LayoutParams bparams, b2params;
    Button printBtn, homeBtn, btn_screenshot;
    boolean statback = true;
    boolean newVehiclback = true;
    String verME = null;
    String result = null;
    int latestver = 0;
    PackageInfo pinfo = null;
    List<Cookie> sessionCookie;
    int id = 0;
    BufferedReader in = null;
    ProgressDialog pDialog, pDialog1;
    KeyStore keyStore = null;
    private String fileName = "", fileNameCSV="";
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
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        checker = new PermissionsChecker(this);
        proceedFurther();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    public void proceedFurther() {

        webView = (WebView) findViewById(R.id.webView1);
        //ll_main = (LinearLayout) findViewById(R.id.ll_main);
        // main_ac_relativelayout = (RelativeLayout) findViewById(R.id.main_ac_relativelayout);
        homeBtn = (Button) findViewById(R.id.button1);
        printBtn = (Button) findViewById(R.id.button2);
        //   btn_screenshot = (Button) findViewById(R.id.btn_screenshot);
        scrollview2 = (ScrollView) findViewById(R.id.scrollview2);

        homeBtn.setVisibility(View.GONE);
        printBtn.setVisibility(View.GONE);

        activity = getWindow().getDecorView();
        webView.getSettings().setJavaScriptEnabled(false);
        // webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setPluginState(PluginState.ON);
        webView.getSettings().setUserAgentString(GlobalVariable.getUserAgent());
        webView.getSettings().setLoadWithOverviewMode(true);
        // double tap zoom below
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        webView.getSettings().setSaveFormData(true);
        webView.getSettings() .setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAllowFileAccess(false);
        context = getApplicationContext();




        startWebView(GlobalVariable.getUrl());
        Log.e(TAG, "proceedFurther:=========================================== " + GlobalVariable.getUrl());
    }


    private class logout extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {

            /** New Loader */
            pDialog = new ProgressDialog(MainActivity.this,
                    R.style.full_screen_dialog) {
                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.fill_dialog);
                    getWindow().setLayout(LayoutParams.FILL_PARENT,
                            LayoutParams.FILL_PARENT);
                }
            };
            pDialog.getWindow().setBackgroundDrawableResource(R.drawable.splash);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        protected String doInBackground(String... params) {

            return null;

        }

        @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
        @Override
        protected void onPostExecute(String result) {
            webView.clearCache(true);
            pDialog.dismiss();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            String keyIdentifer = null;

            i.putExtra("code", "2");
            startActivity(i);
            finish();

        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    static DefaultHttpClient client = createClient();

    static DefaultHttpClient createClient() {
        BasicHttpParams params = new BasicHttpParams();
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        final SSLSocketFactory sslSocketFactory = SSLSocketFactory
                .getSocketFactory();
        schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
                schemeRegistry);
        DefaultHttpClient httpclient = new DefaultHttpClient(cm, params);
        httpclient.getCookieStore().getCookies();
        return httpclient;
    }

    public static DefaultHttpClient getClient() {
        return client;
    }

    private void startWebView(String url) {

        webView.setWebViewClient(new WebViewClient() {

            @SuppressLint("MissingPermission")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.e(TAG, "shouldOverrideUrlLoading ================ " + url);

                if (!loadingFinished) {
                    redirect = true;
                }

                if (!WebPrintActivity
                        .checkInternetConnection(MainActivity.this)) {

                    // Toast.makeText(LoginActivity.this,
                    // "Please check your internet connection and try again !",
                    // Toast.LENGTH_SHORT).show();
                    showAlertDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !", false);

                } else {

                    if (url.endsWith("?type=relogin")) {

                        // showAlertDialog(MainActivity.this, "Alert!",
                        // "Please login with your new password!", false);

                        // webView.stopLoading();
                        //		 Log.d(TAG, "MOBU5");
                        webView.loadUrl(getString(R.string.u5));
                        Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
                        // i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        // Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        String keyIdentifer = null;

                        i2.putExtra("code", "1");// adding additional data using
                        // putExtras()
                        startActivity(i2);
                        finish();

                    }

                    if (url.contains("call")) {

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 11);

                        } else {


                            String callNumber = url.substring(url.indexOf("call") + 5, url.length());
                            //	Log.d(TAG, "Calling = "+callNumber);

                            Intent intent = new Intent(Intent.ACTION_CALL);

                            intent.setData(Uri.parse("tel:" + callNumber));
                            startActivity(intent);
                        }
                    }

                    if (url.contains(getString(R.string.u6)) || url.contains(getString(R.string.u69)) || url.contains(getString(R.string.u90)) || url.contains(getString(R.string.u113))) {
                        //		 Log.d(TAG, "INMOBU6 90");
                        GlobalVariable.setStatusBackurl(url);

                        printBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                if (!WebPrintActivity
                                        .checkInternetConnection(MainActivity.this)) {

                                    // Toast.makeText(LoginActivity.this,
                                    // "Please check your internet connection and try again !",
                                    // Toast.LENGTH_SHORT).show();
                                    showAlertDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !", false);

                                } else {

                                    // doPhotoPrint(webView.getUrl());
                                    doPhotoPrint(GlobalVariable.getGlobalprint());
                                }
                            }
                        });

                        homeBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                if (!WebPrintActivity
                                        .checkInternetConnection(MainActivity.this)) {

                                    // Toast.makeText(LoginActivity.this,
                                    // "Please check your internet connection and try again !",
                                    // Toast.LENGTH_SHORT).show();
                                    showAlertDialog(
                                            MainActivity.this,
                                            "No Internet Connection",
                                            "Please check your internet connection & try again !",
                                            false);
                                } else {

                                    homeBtn.setVisibility(View.GONE);
                                    printBtn.setVisibility(View.GONE);
                                    //  btn_screenshot.setVisibility(View.GONE);

                                    webView.loadUrl(getString(R.string.u7));
                                    //				 Log.d(TAG, "MOBU7");

                                }
                            }
                        });

                        view.loadUrl(url);
                        loadingFinished = false;

                    }

                    if (url.substring(url.length() - 4).equalsIgnoreCase(".csv")) {

                        if (!WebPrintActivity.checkInternetConnection(MainActivity.this)) {

                            showAlertDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !", false);

                        } else {

                            if (checker.lacksPermissions(REQUIRED_PERMISSION)) {


                                PermissionsActivity.startActivityForResult(MainActivity.this, PERMISSION_REQUEST_CODE, REQUIRED_PERMISSION);

                            } else {

//                                if (!url.startsWith("https://drive.google.com")) {
//
//                                    url = "http://drive.google.com/viewerng/viewer?embedded=true&url=" + url;
//
//                                }

                                fileNameCSV = url.substring(url.lastIndexOf('/') + 1, url.length());
                                Log.e(TAG,"CSV Name: "+ fileNameCSV);

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
                            //new DownloadTask(MainActivity.this, url);
                        }
                    }

                    if (url.substring(url.length() - 4).equalsIgnoreCase(".pdf")) {

                        if (!WebPrintActivity.checkInternetConnection(MainActivity.this)) {

                            showAlertDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !", false);

                        } else {

                            if (checker.lacksPermissions(REQUIRED_PERMISSION)) {


                                PermissionsActivity.startActivityForResult(MainActivity.this, PERMISSION_REQUEST_CODE, REQUIRED_PERMISSION);

                            } else {

//                                if (!url.startsWith("https://drive.google.com")) {
//
//                                    url = "http://drive.google.com/viewerng/viewer?embedded=true&url=" + url;
//
//                                }

                                fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
                                Log.e(TAG,"PDF Name: "+ fileName);

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
                            //new DownloadTask(MainActivity.this, url);
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

                        // btn_screenshot.setVisibility(View.GONE);
                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (url.contains(getString(R.string.u33))) {

                        // btn_screenshot.setVisibility(View.GONE);
                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (url.contains(getString(R.string.u7))) {

                        //  btn_screenshot.setVisibility(View.GONE);
                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (GlobalVariable.getUrl().contains(getString(R.string.u43))) {

                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                        if (url.contains("https://www.jlrmaximizer.in/reports/admin_stats?screenshot")) {

                            if (checker.lacksPermissions(REQUIRED_PERMISSION)) {

                                PermissionsActivity.startActivityForResult(MainActivity.this, PERMISSION_REQUEST_CODE, REQUIRED_PERMISSION);

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
                                    /*File direct = new File(Environment.getExternalStorageDirectory() + "/JLRMaximizerSreenShot");
                                    if (!direct.exists()) {

                                        File imageDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/JLRMaximizerSreenShot");
                                        Log.e(TAG, "File Path = " + Environment.getExternalStorageDirectory().getPath());

                                        imageDirectory.mkdirs();
                                        imageDirectory.setReadable(true);

                                        imageDirectory.setWritable(true);
                                        imageDirectory.setExecutable(true);
                                    }
                                    Date date = new Date();
                                    SimpleDateFormat dateformat = new SimpleDateFormat("ddMMyyHHmmss");

                                    String filename = "ScreenShot" + dateformat.format(date) + ".png";
                                    File file = new File(new File(Environment.getExternalStorageDirectory().getPath() + "/JLRMaximizerSreenShot"), filename);

                                    FileOutputStream fos;
                                    try {
                                        fos = new FileOutputStream(file);
                                        bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                        addImageToGallery(bitMapToString(bm), getApplicationContext());
                                        Toast.makeText(MainActivity.this, "Screenshot saved successfully into gallery", Toast.LENGTH_SHORT).show();
                                        fos.flush();
                                        fos.close();
                                        bm.recycle();
                                    } catch (FileNotFoundException e) {
                                        Log.e("FileNotFoundException", e.getMessage(), e);
                                    } catch (IOException e) {
                                        Log.e("IOException", e.getMessage(), e);
                                    }*/
                                   /*String path = Environment.getExternalStorageDirectory().toString();

                                    Log.e(TAG, "path = " + path);
                                    OutputStream fOut = null;
                                    File file = new File(path, "/JLRMaximizer_Image.png");
                                    try {
                                        fOut = new FileOutputStream(file);
                                        bm.compress(Bitmap.CompressFormat.PNG, 50, fOut);
                                        fOut.flush();
                                        fOut.close();
                                        bm.recycle();
                                    } catch (FileNotFoundException e1) {

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }*/


                                }
                            }
                            /* Toast.makeText(MainActivity.this, "ScreenShot button is clicked", Toast.LENGTH_SHORT).show();*/
                            /*This code is added by Deepak Tiwari to take screenshot*/

                                /* Bitmap bitmap = loadBitmapFromView(ll_main, ll_main.getWidth(), ll_main.getHeight());
                                saveBitmap(bitmap);*/

                        }
                    } else if (url.contains(getString(R.string.u30))) {

                        // btn_screenshot.setVisibility(View.GONE);
                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);

                    } else if (url.contains(getString(R.string.u33))) {

                        //btn_screenshot.setVisibility(View.GONE);
                        printBtn.setVisibility(View.GONE);
                        homeBtn.setVisibility(View.GONE);
                    }
                    /*Following Lines of code are Added by Deepak Tiwari*/

                    if (url.contains(getString(R.string.u8))) {
                        //			 Log.d(TAG, "INMOBU8");

                        String urlsub = url;
                        String urlsub2 = url;
                        String surl = urlsub.substring(urlsub.indexOf("download") + 8, urlsub.length());
                        //			Log.e(TAG, "surl = "+surl);
                        GlobalVariable.setUrl(surl);
                        url = getString(R.string.u9) + surl;
                        //			Log.e(TAG, "url = "+url);
                        //			 Log.d(TAG, "MOBU9");

                        // Capture Rid for pdf
                        String pdfurl = urlsub2.substring(urlsub2.indexOf("download") + 13, urlsub2.indexOf("&"));

                        //			Log.e(TAG, "pdfurl = "+pdfurl);

                        String printurl = getString(R.string.u10) + pdfurl
                                + ".jpg";
                        //			Log.d(TAG, "MOBU10");
                        GlobalVariable.setGlobalprint(printurl);
                        //			Log.e(TAG, "printurl = "+printurl);
                        statback = true;

                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
                        homeBtn.setVisibility(View.VISIBLE);
                        printBtn.setVisibility(View.VISIBLE);
                        // btn_screenshot.setVisibility(View.GONE);
                    }

                    if (url.contains(getString(R.string.u97))) {
                        //		 Log.d(TAG, "INMOBU97");

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
                        homeBtn.setVisibility(View.VISIBLE);
                        printBtn.setVisibility(View.VISIBLE);
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
                        //		Log.e(TAG, "url = "+url);
                        //		 Log.d(TAG, "MOBU98");

                        // Capture Rid for pdf
                        String pdfurl = urlsub2.substring(
                                urlsub2.indexOf("download") + 13,
                                urlsub2.indexOf("&"));

                        //		Log.e(TAG, "pdfurl = "+pdfurl);

                        String printurl = getString(R.string.u122) + pdfurl
                                + ".jpg";
                        //		Log.d(TAG, "MOBU10");
                        GlobalVariable.setGlobalprint(printurl);
                        //		Log.e(TAG, "printurl = "+printurl);
                        statback = true;

                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
                        homeBtn.setVisibility(View.VISIBLE);
                        printBtn.setVisibility(View.VISIBLE);
                        // btn_screenshot.setVisibility(View.GONE);

                    }

                    if (url.contains(getString(R.string.u11))) {
                        //			 Log.d(TAG, "MOBU11");
                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setDisplayZoomControls(true);
                        homeBtn.setVisibility(View.VISIBLE);
                        printBtn.setVisibility(View.VISIBLE);
                        // btn_screenshot.setVisibility(View.GONE);
                        // view.loadUrl(url);
                        // loadingFinished = false;

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
                        //		 Log.d(TAG, "INMOBU14");

                        webView.loadUrl(getString(R.string.u15));
                        //		 Log.d(TAG, "MOBU15");
                        logout LogoutTask = new logout();
                        LogoutTask.execute();

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

                        /*This lines of code is added by Deepak Tiwari*/
                        // main_ac_relativelayout.setBackgroundColor(Color.WHITE);
                        // btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                    }/*This lines of code is added by Deepak Tiwari*/ else if (url.contains(getString(R.string.u23))) {

                        //			 Log.d(TAG, "INMOBU23");
                        GlobalVariable.setOnlybackurl(url);

                    } else if (url.contains(getString(R.string.u7))) {

                        /*This lines of code is added by Deepak Tiwari*/
                        // main_ac_relativelayout.setBackgroundColor(Color.BLACK);
                        //btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                    } else if (url.contains(getString(R.string.u40))) {

                        /*This lines of code is added by Deepak Tiwari*/
                        //btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

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

                        /*This lines of code is added by Deepak Tiwari*/
                        //btn_screenshot.setVisibility(View.GONE);
                        /*This lines of code is added by Deepak Tiwari*/

                        //			 Log.d(TAG, "INMOBU28");
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

                // Log.d(TAG, "onPageFinished = "+url);

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

            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

                try {
                    webView.stopLoading();
                } catch (Exception e) {
                }

                showAlertDialog(MainActivity.this, "No Internet Connection",
                        "Please check your internet connection & try again !",
                        false);

                super.onReceivedError(webView, errorCode, description,
                        failingUrl);

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                // Log.d(TAG, "onPageStarted = "+url);

                if (id == 0) {
                    id = 1;

                    DefaultHttpClient httpclient = LoginActivity.getClient();
                    Cookie sessionInfo;
                    List<Cookie> cookies = httpclient.getCookieStore()
                            .getCookies();
                    if (!cookies.isEmpty()) {
                        CookieSyncManager.createInstance(MainActivity.this);
                        CookieManager cookieManager = CookieManager
                                .getInstance();

                        for (Cookie cookie : cookies) {
                            sessionInfo = cookie;
                            String cookieString = sessionInfo.getName() + "="
                                    + sessionInfo.getValue() + "; domain="
                                    + sessionInfo.getDomain();

                            // Log.d(TAG, "Beta, cookie string is " +
                            // cookieString);

                            cookieManager.setCookie(getString(R.string.u29), cookieString);
                            // Log.d(TAG, "MOBU29");
                            CookieSyncManager.getInstance().sync();
                        }
                    } else {

                    }

                }

                if (url.contains(getString(R.string.u30))
                        || url.equals(getString(R.string.u31))
                        || url.endsWith("?type=relogin")
                        || url.equals(getString(R.string.u32))) {

                    //		 Log.d(TAG, "INMOB3032");

                } else {

                    if (progressDialog == null
                            || progressDialog.isShowing() == false) {

                        /** Old Loader */
                        // progressDialog = new
                        // ProgressDialog(MainActivity.this);
                        // progressDialog.setCancelable(false);
                        // progressDialog.setCanceledOnTouchOutside(false);
                        // progressDialog.setMessage("Loading...");
                        // progressDialog.show();

                        // /**new Loader*/
                        progressDialog = new ProgressDialog(MainActivity.this,
                                R.style.full_screen_dialog) {
                            @Override
                            protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                setContentView(R.layout.fill_dialog);
                                getWindow().setLayout(LayoutParams.FILL_PARENT,
                                        LayoutParams.FILL_PARENT);
                            }
                        };
                        progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.splash);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }

                }

            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {

                if (progress >= 100) {

                    // if (progressDialog != null || progressDialog.isShowing())
                    // {
                    // System.out.println("onProgressChangedDialog DISMISSED!");
                    // progressDialog.dismiss();
                    //
                    // }

                }

            }

        });

        webView.loadUrl(url);
        // Comment Below After Testing!
        // if (url.endsWith("?type=relogin")){
        //
        // System.out.println("in RELOGIN shouldoverride -----------------------------------");
        //
        // // showAlertDialog(MainActivity.this, "Alert!",
        // // "Please login with your new password!", false);
        //
        // webView.stopLoading();
        // Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
        // //i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
        // Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // String keyIdentifer = null;
        // System.out.println("PASSED CODE 1");
        // i2.putExtra("code", "1");//adding additional data using putExtras()
        // startActivity(i2);
        // finish();
        //
        // }

    }

    @SuppressWarnings("deprecation")
    public void showAlertDialog(Context context, String title, String message,
                                final Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setCancelable(false);

        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        if (status) {
            alertDialog.setButton("Update",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.mobilestyx.JLRMaximizer")));
                        }
                    });
        } else {
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // finish();
                    // System.exit(0);
                }
            });
        }

        alertDialog.show();
    }

    public void jsAlertDialog1() {
        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int newProgress) {

                // Log.e("onProgressChanged", " Value is: " + newProgress);
                if (newProgress >= 98) {
                    if (pDialog != null) {
                        pDialog.dismiss();
                    }
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                final JsResult finalRes = result;
                new AlertDialog.Builder(view.getContext())
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finalRes.confirm();
                                    }
                                })

                        .setNegativeButton(android.R.string.cancel,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finalRes.cancel();
                                    }
                                })

                        .create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, JsResult result) {

                final JsResult finalRes = result;
                new AlertDialog.Builder(view.getContext())
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finalRes.confirm();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        // dialog.dismiss();
                                        finalRes.cancel();
                                    }
                                })

                        .setCancelable(false).create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message,
                                      String defaultValue, JsPromptResult result) {
                final JsPromptResult finalRes = result;
                new AlertDialog.Builder(view.getContext())
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finalRes.confirm();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        finalRes.cancel();
                                    }
                                })

                        .setCancelable(false).create().show();
                return true;
            }

        });
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

            Toast.makeText(getApplicationContext(),
                    "Please update your device to KitKat or above to use the print facility or directly E-Mail the PDF.", Toast.LENGTH_LONG).show();

        } else {

            try {

                String abcc = urlprint;
                //		Log.e(TAG, "print = "+abcc);

                PrintHelper photoPrinter = new PrintHelper(MainActivity.this);
                //PrintHelper photoPrinter = new PrintHelper(this);
                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                Bitmap bitmap = BitmapFactory.decodeStream(getBitmapFromURL(abcc));
                photoPrinter.printBitmap("JLR Pdf", bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBackPressed() {


        if (!WebPrintActivity.checkInternetConnection(MainActivity.this)) {

            // Toast.makeText(LoginActivity.this,
            // "Please check your internet connection and try again !",
            // Toast.LENGTH_SHORT).show();
            showAlertDialog(MainActivity.this, "No Internet Connection", "Please check your internet connection & try again !", false);

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
                btn_screenshot.setVisibility(View.GONE);
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

        // Log.d(TAG, "onDestroy MainActivity");
        super.onDestroy();
        try {
            trimCache(this);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /** Not For KitKat */
    /*
     * public static void trimCache(Context context) { try { File dir =
     * context.getCacheDir(); if (dir != null && dir.isDirectory()) {
     * deleteDir(dir); } } catch (Exception e) { // TODO: handle exception } }
     *
     * public static boolean deleteDir(File dir) { if (dir != null &&
     * dir.isDirectory()) { String[] children = dir.list(); for (int i = 0; i <
     * children.length; i++) { boolean success = deleteDir(new File(dir,
     * children[i])); if (!success) { return false; } } }
     */

    /**
     * KitKat
     */
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

    private static TrustManager[] trustManagers;

    public static class _FakeX509TrustManager implements
            javax.net.ssl.X509TrustManager {
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        public boolean isClientTrusted(X509Certificate[] chain) {
            return (true);
        }

        public boolean isServerTrusted(X509Certificate[] chain) {
            return (true);
        }

        public X509Certificate[] getAcceptedIssuers() {
            return (_AcceptedIssuers);
        }
    }

    public static void allowAllSSL() // third call
    {
        javax.net.ssl.HttpsURLConnection
                .setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

        javax.net.ssl.SSLContext context = null;

        if (trustManagers == null) {
            trustManagers = new javax.net.ssl.TrustManager[]{new _FakeX509TrustManager()};
        }

        try {
            context = javax.net.ssl.SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            // Log.e("allowAllSSL", e.toString());
        } catch (KeyManagementException e) {
            // Log.e("allowAllSSL", e.toString());
        }
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(context
                .getSocketFactory());
    }

    /*Following Code is Added by Deepak tiwari*/

    public void saveBitmap(Bitmap bitmap) {


        File direct = new File(Environment.getExternalStorageDirectory() + "/JLRMaximizerSreenShot");

        if (!direct.exists()) {

            File imageDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/JLRMaximizerSreenShot");
            Log.e(TAG, "File Path = " + Environment.getExternalStorageDirectory().getPath());

            imageDirectory.mkdirs();
            imageDirectory.setReadable(true);
            imageDirectory.setWritable(true);
            imageDirectory.setExecutable(true);
        }
        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("ddMMyyHHmmss");
        String filename = "ScreenShot" + dateformat.format(date) + ".png";
        File file = new File(new File(Environment.getExternalStorageDirectory().getPath() + "/JLRMaximizerSreenShot"), filename);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(getApplicationContext(), "Screenshot saved successfully", Toast.LENGTH_SHORT).show();
            Log.e("JLRMaximizerSreenShot", "Statement Page");
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    }

    public String bitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public void openUrlInBrowser(String url) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

}
