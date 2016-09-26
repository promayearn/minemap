package com.augmentis.ayp.minemap;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.text.TextUtils;
import android.widget.Toast;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText inputName,
            inputEmail,
            inputPasswordOne,
            inputPasswordTwo;

    private TextInputLayout inputLayoutName,
            inputLayoutEmail,
            inputLayoutPassword,
            inputLayoutConfirm;

    private Button btnRegister;
    private ImageView imgLogo;
    private String password;
    private String confirmPassword;
    private String name;
    private String email;

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
        inputPasswordOne = (EditText) findViewById(R.id.input_password);
        inputPasswordTwo = (EditText) findViewById(R.id.input_confirm);

        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPasswordOne.addTextChangedListener(new MyTextWatcher(inputPasswordOne));
        inputPasswordTwo.addTextChangedListener(new MyTextWatcher(inputPasswordTwo));

        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputLayoutConfirm = (TextInputLayout) findViewById(R.id.input_layout_confirm);

        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                submitForm();
//                Intent intent = new Intent(RegisterActivity.this, LocationDescription.class);
//                startActivity(intent);
            }
        });
    }

    // validate Form
    public void submitForm() {
        if (!validateName()) {
            return;
        }
        if (!validateEmail()) {
            return;
        }
        if (!validatePassword()) {
            return;
        }
        if (!validateConfirm()) {
            return;
        }
        if (!checkPassword()) {
            return;
        }

        Log.d(TAG, "submit form");
        sendToDatabase();

    }

    public boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
//            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    public boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
//            requestFocus(inputEmail);
            return false;
        } else {

            inputLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    public boolean validatePassword() {
        if (inputPasswordOne.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }
        return true;
    }

    public boolean validateConfirm() {
        if (inputPasswordTwo.getText().toString().trim().isEmpty()) {
            inputLayoutConfirm.setError(getString(R.string.err_msg_confirm));
            return false;
        } else {
            inputLayoutConfirm.setErrorEnabled(false);
        }
        return true;
    }

    public boolean checkPassword() {
        password = inputPasswordOne.getText().toString();
        confirmPassword = inputPasswordTwo.getText().toString();

        if (password.equals(confirmPassword)) {
            Log.d(TAG, "Correct Password !");
        } else {
            Log.d(TAG, " Incorrect Password !!!!");
            return false;
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
                case R.id.input_confirm:
                    checkPassword();
                    break;
            }
        }
    }

    public void sendToDatabase() {
        name = inputName.getText().toString();
        email = inputEmail.getText().toString();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        String strURL = "http://minemap.hol.es/register.php?name=" + name + "&email=" + email + "&password=" + password;
        JsonHttp jsonHttp = new JsonHttp();
        String strJson = jsonHttp.getJSONUrl(strURL);
        Log.e("Update : ", strJson);

        Toast.makeText(this, "Ordering successfully", Toast.LENGTH_LONG).show();
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
    }
}


