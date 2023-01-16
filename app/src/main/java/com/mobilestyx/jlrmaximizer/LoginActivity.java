package com.mobilestyx.jlrmaximizer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilestyx.jlrmaximizer.model.LoginRequest;
import com.mobilestyx.jlrmaximizer.model.LoginResponse;
import com.mobilestyx.jlrmaximizer.remote.ApiClient;
import com.mobilestyx.jlrmaximizer.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    String username1, password1;
    Button login;
    CheckBox checkbox;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        checkbox = (CheckBox) findViewById(R.id.checkbox);

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (username.length() == 0) {
                    Toast.makeText(getBaseContext(), "Please Enter User ID", Toast.LENGTH_SHORT).show();
                    username.requestFocus();
                    username.setText("");
                } else if (password.length() == 0) {
                    Toast.makeText(getBaseContext(), "Please Enter Password", Toast.LENGTH_SHORT).show();
                    password.requestFocus();
                    password.setText("");
                } else {
                    doLogin();
                }

            }


        });


    }

    public void doLogin() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username.getText().toString());
        loginRequest.setPassword(password.getText().toString());

        Call<LoginResponse> loginResponseCall = ApiClient.getUserService().userLogin(loginRequest);
        loginResponseCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
               if(response.isSuccessful()){
                    Toast.makeText(LoginActivity.this,"Login Successful", Toast.LENGTH_LONG).show();
                    LoginResponse loginResponse = response.body();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this,"Login Success:-  " + loginResponse.getUsername(), Toast.LENGTH_LONG).show();
//                            startActivity(new Intent(MainActivity.this,MainActivity.class).putExtra("data",loginResponse.getUsername()));
                        }
                    },700);

                }else{
                    Toast.makeText(LoginActivity.this,"Login Failed", Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,"Throwable "+t.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            }
        });


    }

    @Override
    public void onBackPressed() {
        AppUtils.createInfoDialog(this, "title", "message").show();
    }
}