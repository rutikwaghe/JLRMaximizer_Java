package com.mobilestyx.JLRMaximizer.utils;

import java.util.List;

import io.michaelrocks.paranoid.Obfuscate;
import okhttp3.Cookie;

@Obfuscate
public class GlobalVariable {
    public static String urlshare = "";
    public static String urljsshare = "";
    public static String urlstatlink = "";
    public static String filename = "";
    public static String statusBackurl = "";
    public static String surl = "";
    public static String url = "";
    public static String webresponse = "";
    public static String onlybackurl = "";
    public static String dealerBackurl = "";
    public static String uid = "";
    public static String duid = "";
    public static String globalprint = "";
    public static String UserAgent = "";


    public static String getUserAgent() {
        return UserAgent;
    }

    public static void setUserAgent(String userAgent) {
        UserAgent = userAgent;
    }

    public static String getGlobalprint() {
        return globalprint;
    }

    public static void setGlobalprint(String globalprint) {
        GlobalVariable.globalprint = globalprint;
    }

    public static void setDuid(String duid) {
        GlobalVariable.duid = duid;
    }

    public static String getUid() {
        return uid;
    }

    public static void setUid(String uid) {
        GlobalVariable.uid = uid;
    }

    public static String phpresponse = "";
    public static List<Cookie> cookies;

    public static void setCookies(List<Cookie> cookies2) {
        GlobalVariable.cookies = cookies2;
    }

    public static String getPhpresponse() {
        return phpresponse;
    }

    public static void setPhpresponse(String phpresponse) {
        GlobalVariable.phpresponse = phpresponse;
    }

    public static String getOnlybackurl() {
        return onlybackurl;
    }

    public static void setOnlybackurl(String onlybackurl) {
        //	Log.e(TAG, "Setting onlybackurl to "+onlybackurl);
        GlobalVariable.onlybackurl = onlybackurl;

    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        //	Log.e(TAG, "setting url = "+url);
        GlobalVariable.url = url;
    }

    public static String getStatusBackurl() {
        return statusBackurl;
    }

    public static void setStatusBackurl(String statusBackurl) {
        GlobalVariable.statusBackurl = statusBackurl;
        //	Log.e(TAG, "Setting statusBackurl to "+statusBackurl);

    }

}
