package com.mobilestyx.jlrmaximizer.remote;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserService {
    @FormUrlEncoded
    @POST("login/validate_login_webservice")
    Call<JsonObject> userLogin(@Field("userid") String userid, @Field("password") String password);

//    @GET("app/?name=JLRMax")
//    Call<JsonObject> splashVersion();
}