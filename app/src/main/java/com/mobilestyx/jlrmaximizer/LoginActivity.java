package com.mobilestyx.jlrmaximizer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilestyx.jlrmaximizer.utils.AppUtils;

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
                    Toast.makeText(getBaseContext(), "Successfully login", Toast.LENGTH_SHORT).show();


                }

            }


        });


    }

    @Override
    public void onBackPressed() {
        AppUtils.createInfoDialog(this, "title", "message").show();
    }
}