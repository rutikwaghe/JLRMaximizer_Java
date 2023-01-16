package com.mobilestyx.jlrmaximizer.remote;

import com.mobilestyx.jlrmaximizer.R;

import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit getRetrofit() {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
//                .certificatePinner(getCertificatePinner())
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(String.valueOf(R.string.baseUrl))
                .client(okHttpClient)
                .build();

        return retrofit;
    }

//    private static CertificatePinner getCertificatePinner() {
//        // To get latest certificate https://www.ssllabs.com/ssltest/analyze.html?d=www.iciciprulife.com
//        // To get latest certificate https://www.ssllabs.com/ssltest/analyze.html?d=api.iciciprulife.com
//        return new CertificatePinner.Builder()
//                .add("https://www.jlrmaximizer.in", "sha256/nkg9VQd+wjcCEJdZ+84DxZz9OZ97qGpyrB7v4qMqS8g=")
//                .build();
//    }

    public static UserService getUserService() {
        UserService userService = getRetrofit().create(UserService.class);

        return userService;
    }

}
