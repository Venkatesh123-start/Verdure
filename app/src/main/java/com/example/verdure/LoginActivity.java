package com.example.verdure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.verdure.db.DBHelper;
import com.example.verdure.util.Security;

public class LoginActivity extends AppCompatActivity {

    EditText editUser, editPass;
    Button btnLogin, btnRegister;
    TextView txtForgot;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUser = findViewById(R.id.editUsername);
        editPass = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        btnRegister = findViewById(R.id.buttonRegister);
        txtForgot = findViewById(R.id.textForgot);
        db = new DBHelper(this);

        btnLogin.setOnClickListener(v -> {
            String u = editUser.getText().toString().trim();
            String p = editPass.getText().toString().trim();

            // ✅ FIELD VALIDATION (this is where you add it)
            if (TextUtils.isEmpty(u)) {
                ((com.google.android.material.textfield.TextInputLayout)
                        findViewById(R.id.tilUsername)).setError("Enter username");
                return;
            } else {
                ((com.google.android.material.textfield.TextInputLayout)
                        findViewById(R.id.tilUsername)).setError(null);
            }

            if (TextUtils.isEmpty(p)) {
                ((com.google.android.material.textfield.TextInputLayout)
                        findViewById(R.id.tilPassword)).setError("Enter password");
                return;
            } else {
                ((com.google.android.material.textfield.TextInputLayout)
                        findViewById(R.id.tilPassword)).setError(null);
            }

            // ✅ Continue with login if both filled
            String hash = com.example.verdure.util.Security.sha256(p);
            long userId = db.validateUser(u, hash);
            if (userId > 0) {
                int isAdmin = db.getIsAdmin((int) userId);
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("userId", (int) userId);
                i.putExtra("username", u);
                i.putExtra("isAdmin", isAdmin);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials. Please check username or password.", Toast.LENGTH_LONG).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            // launch register screen
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        });

        txtForgot.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(i);
        });
    }
}
