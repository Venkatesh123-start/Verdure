package com.example.verdure;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.verdure.db.DBHelper;
import com.example.verdure.util.Security;

public class RegisterActivity extends AppCompatActivity {

    EditText regUser, regPass, regPass2, regQ, regAns;
    Button btnCreate;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regUser = findViewById(R.id.regUsername);
        regPass = findViewById(R.id.regPassword);
        regPass2 = findViewById(R.id.regPassword2);
        regQ = findViewById(R.id.regSecQ);
        regAns = findViewById(R.id.regSecAns);
        btnCreate = findViewById(R.id.buttonCreate);
        db = new DBHelper(this);

        btnCreate.setOnClickListener(v -> {
            String u = regUser.getText().toString().trim();
            String p1 = regPass.getText().toString().trim();
            String p2 = regPass2.getText().toString().trim();
            String q = regQ.getText().toString().trim();
            String ans = regAns.getText().toString().trim();

            if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p1) || TextUtils.isEmpty(p2) || TextUtils.isEmpty(q) || TextUtils.isEmpty(ans)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!p1.equals(p2)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.userExists(u)) {
                Toast.makeText(this, "User already exists. Please login.", Toast.LENGTH_LONG).show();
                return;
            }
            String passHash = Security.sha256(p1);
            String ansHash = Security.sha256(ans);
            long id = db.createUser(u, passHash, 0, q, ansHash);
            if (id > 0) {
                Toast.makeText(this, "Account created. Please login.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to create account.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
