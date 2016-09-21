package com.augmentis.ayp.minemap;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private EditText inputName,
            inputEmail,
            inputPassword;

    private TextInputLayout inputLayoutName,
            inputLayoutEmail,
            inputLayoutPassword;

    private Button btnSignUp;
    private ImageView imgLogo;
    private TextView tvRegis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Drawable logo = ResourcesCompat
                .getDrawable(getResources(), R.drawable.logo, null);

        imgLogo = (ImageView) findViewById(R.id.imageView);
        imgLogo.setImageDrawable(logo);

        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);

        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        btnSignUp = (Button) findViewById(R.id.btn_signup);

        tvRegis = (TextView) findViewById(R.id.registerView);
        tvRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }
}
