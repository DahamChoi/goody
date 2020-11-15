package com.goody.myapplication.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.goody.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText inputName;
    private EditText inputPassword;
    private EditText inputID;
    private AppCompatButton btnSignup;
    private TextView linkLogin;
    private TextView linkLogin2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        // FireBase
        mAuth = FirebaseAuth.getInstance();

        // UI
        getSupportActionBar().hide();

        inputName = findViewById(R.id.input_name);
        inputPassword = findViewById(R.id.input_password);
        inputID = findViewById(R.id.input_id);
        btnSignup = findViewById(R.id.btn_signup);
        linkLogin = findViewById(R.id.link_login);
        linkLogin2 = findViewById(R.id.link_login2);
        Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher match, String url) {
                return "";
            }
        };

        Linkify.addLinks(linkLogin2, Pattern.compile("서비스약관"),
                "https://goodyprivacypolicy.web.app/",
                null,transformFilter);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup(){
        if (!validate()) {
            onSignupFailed();
            return;
        }

        btnSignup.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("계정 생성중...");
        progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
        progressDialog.show();

        final String name = inputName.getText().toString();
        final String id = inputID.getText().toString();
        String password = inputPassword.getText().toString();

        // TODO: Implement your own signup logic here.
        mAuth.createUserWithEmailAndPassword(id,password).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                try {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String,Object> datafiled = new HashMap<>();
                        datafiled.put("nickname",name);

                        db.collection("users").document(user.getUid()).
                                set(datafiled).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    onSignupSuccess();
                                }
                                else{
                                    onSignupFailed();
                                }
                            }
                        });
                    } else {
                        onSignupFailed();
                    }

                    progressDialog.dismiss();
                }catch (Exception e){
                    Log.e("ERROR",e.toString());
                }
            }
        });
    }

    public void onSignupSuccess() {
        btnSignup.setEnabled(true);
        setResult(RESULT_OK, null);

        Toast.makeText(getBaseContext(), "회원가입 성공!", Toast.LENGTH_LONG).show();

        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "회원가입 실패", Toast.LENGTH_LONG).show();

        btnSignup.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = inputName.getText().toString();
        String email = inputID.getText().toString();
        String password = inputPassword.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            inputName.setError("3글자 이상의 닉네임을 입력해주세요");
            valid = false;
        } else {
            inputName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputID.setError("올바른 이메일 값을 입력해주세요");
            valid = false;
        } else {
            inputID.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            inputPassword.setError("4 ~ 10 문자의 비밀번호를 입력해주세요");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }
}