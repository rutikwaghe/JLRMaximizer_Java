package com.mobilestyx.JLRMaximizer.remote;

import com.google.gson.JsonObject;

import io.michaelrocks.paranoid.Obfuscate;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

@Obfuscate
public interface UserService {
    @FormUrlEncoded
    @POST("login/validate_login_webservice")
    Call<JsonObject> userLogin(@Field("userid") String userid, @Field("password") String password);

//    @GET("app?name=JLRMax")
//    Call<JsonObject> splashVersion();
}