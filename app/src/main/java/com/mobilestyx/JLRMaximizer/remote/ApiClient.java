package com.mobilestyx.JLRMaximizer.remote;

import io.michaelrocks.paranoid.Obfuscate;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Obfuscate
public class ApiClient {

    private static Retrofit getRetrofit() {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        httpBuilder.addInterceptor(httpLoggingInterceptor);

        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add("www.jlrmaximizer.in", "sha256/0e4kSeOp00XjU+LnIBR+b1j0rfDmIQtrsLD+kuxr4dk=")
                .build();

        OkHttpClient client1 = httpBuilder.certificatePinner(certificatePinner).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://www.jlrmaximizer.in/")
                .client(client1)
                .build();

        return retrofit;
    }

    public static UserService getUserService() {
        UserService userService = getRetrofit().create(UserService.class);

        return userService;
    }

//    private static CertificatePinner getCertificatePinner() {
//        // To get latest certificate https://www.ssllabs.com/ssltest/analyze.html?d=www.iciciprulife.com
//        // To get latest certificate https://www.ssllabs.com/ssltest/analyze.html?d=api.iciciprulife.com
//        return new CertificatePinner.Builder()
//                .add("https://www.jlrmaximizer.in/", "sha256/nkg9VQd+wjcCEJdZ+84DxZz9OZ97qGpyrB7v4qMqS8g=")
//                .build();
//    }

}
