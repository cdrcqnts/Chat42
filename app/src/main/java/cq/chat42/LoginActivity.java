package cq.chat42;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import cq.chat42.Model.User;

import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    Aux aux;
    Snackbar snackbarInternet;
    AwesomeValidation mAwesomeValidation;

    TextInputLayout email, password;
    Button btn_login;
    ProgressBar progress;

    FirebaseAuth auth;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        aux = new Aux(getApplicationContext());
        snackbarInternet = Snackbar.make(findViewById(R.id.constraint_layout), Aux.MSG_ERR_INTERNET, Snackbar.LENGTH_LONG);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();

        mAwesomeValidation = new AwesomeValidation(TEXT_INPUT_LAYOUT);

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        btn_login = findViewById(R.id.btn_login);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        String regexPassword = "[a-zA-Z]{6,64}";
        mAwesomeValidation.addValidation(LoginActivity.this, R.id.login_email, android.util.Patterns.EMAIL_ADDRESS, R.string.err_email);
        mAwesomeValidation.addValidation(LoginActivity.this, R.id.login_password, regexPassword, R.string.err_password);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAwesomeValidation.validate()) {
                    if (aux.internetIsAvailable()) {
                        disableBtnLogin();
                        String txt_email = email.getEditText().getText().toString().trim();
                        String txt_password = password.getEditText().getText().toString().trim();
                        login(txt_email, txt_password);
                    } else {
                        snackbarInternet.show();
                    }
                }


            }
        });
    }

    private void login(String txt_email, String txt_password) {
        auth.signInWithEmailAndPassword(txt_email, txt_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    redirect();
                } else {
                    // Login failed
                    enableBtnLogin();
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.constraint_layout), task.getException().getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    private void disableBtnLogin() {
        btn_login.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        btn_login.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorChatBackgroundShadow));
    }

    private void enableBtnLogin() {
        btn_login.setEnabled(true);
        progress.setVisibility(View.INVISIBLE);
        btn_login.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
    }

    private void redirect() {
        Intent intent = new Intent(LoginActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
