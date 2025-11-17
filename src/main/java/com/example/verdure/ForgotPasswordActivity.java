package com.example.verdure;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.verdure.db.DBHelper;
import com.example.verdure.util.Security;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText editUsername, editAns, editNewPass;
    TextView txtQ;
    Button btnFetchQ, btnReset;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editUsername = findViewById(R.id.forgotUsername);
        editAns = findViewById(R.id.forgotAns);
        editNewPass = findViewById(R.id.forgotNewPass);
        txtQ = findViewById(R.id.textSecQ);
        btnFetchQ = findViewById(R.id.buttonFetchQ);
        btnReset = findViewById(R.id.buttonReset);
        db = new DBHelper(this);

        btnFetchQ.setOnClickListener(v -> {
            String u = editUsername.getText().toString().trim();
            if (TextUtils.isEmpty(u)) {
                Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show();
                return;
            }
            String q = db.getSecurityQuestion(u);
            if (q == null) {
                txtQ.setText("No such user");
            } else {
                txtQ.setText(q);
            }
        });

        btnReset.setOnClickListener(v -> {
            String u = editUsername.getText().toString().trim();
            String ans = editAns.getText().toString().trim();
            String np = editNewPass.getText().toString().trim();
            if (TextUtils.isEmpty(u) || TextUtils.isEmpty(ans) || TextUtils.isEmpty(np)) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            String ansHash = Security.sha256(ans);
            String newPassHash = Security.sha256(np);
            boolean ok = db.verifySecAnsAndUpdatePassword(u, ansHash, newPassHash);
            if (ok) {
                Toast.makeText(this, "Password reset. Please login.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Incorrect answer or user not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
