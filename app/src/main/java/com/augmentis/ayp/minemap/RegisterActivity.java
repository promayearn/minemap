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

public class RegisterActivity extends AppCompatActivity {

    private EditText inputName,
            inputEmail,
            inputPassword,
            confirmPassword;

    private TextInputLayout inputLayoutName,
            inputLayoutEmail,
            inputLayoutPassword,
            inputLayoutConfirm;

    private Button btnRegister;
    private ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Drawable logo = ResourcesCompat
                .getDrawable(getResources(), R.drawable.logo, null);

        imgLogo = (ImageView) findViewById(R.id.logoView);
        imgLogo.setImageDrawable(logo);

        inputName = (EditText) findViewById(R.id.input_name);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        confirmPassword = (EditText) findViewById(R.id.input_confirm);

        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputLayoutConfirm = (TextInputLayout) findViewById(R.id.input_layout_confirm);

        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
