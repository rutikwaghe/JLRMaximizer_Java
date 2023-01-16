package com.mobilestyx.jlrmaximizer.remote;

import com.mobilestyx.jlrmaximizer.model.LoginRequest;
import com.mobilestyx.jlrmaximizer.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserService {

    @Headers({"Content-Type: application/json","Cache-Control:no-cache"})
    @POST("login/")
    Call<LoginResponse> userLogin(@Body LoginRequest loginRequest);


}