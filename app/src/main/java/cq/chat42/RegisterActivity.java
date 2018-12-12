package cq.chat42;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;


import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

public class RegisterActivity extends AppCompatActivity {

    final String TAG = "RegisterActivity";
    Aux aux;
    Snackbar snackbarInternet;
    AwesomeValidation mAwesomeValidation;

    TextInputLayout username, email, password, version;
    Button btn_register;
    ProgressBar progress;

    FirebaseAuth auth;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        aux = new Aux(getApplicationContext());
        snackbarInternet = Snackbar.make(findViewById(R.id.constraint_layout), Aux.MSG_ERR_INTERNET, Snackbar.LENGTH_LONG);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();

        mAwesomeValidation = new AwesomeValidation(TEXT_INPUT_LAYOUT);

        username = findViewById(R.id.register_username);
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        version = findViewById(R.id.register_version);
        btn_register = findViewById(R.id.btn_register);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        String regexUsername = "[a-zA-Z]{3,21}";
        String regexPassword = "[a-zA-Z]{6,64}";
        String regexVersion = Aux.GROUP_A + "|" + Aux.GROUP_B + "|" + Aux.GROUP_C;

        mAwesomeValidation.addValidation(RegisterActivity.this, R.id.register_username, regexUsername, R.string.err_name);
        mAwesomeValidation.addValidation(RegisterActivity.this, R.id.register_email, android.util.Patterns.EMAIL_ADDRESS, R.string.err_email);
        mAwesomeValidation.addValidation(RegisterActivity.this, R.id.register_password, regexPassword, R.string.err_password);
        mAwesomeValidation.addValidation(RegisterActivity.this, R.id.register_version, regexVersion, R.string.err_version);
        mAwesomeValidation.addValidation(RegisterActivity.this, R.id.register_password_confirm, R.id.register_password, R.string.err_password_confirmation);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAwesomeValidation.validate()) {
                    if (aux.internetIsAvailable()) {
                        btn_register.setEnabled(false);
                        progress.setVisibility(View.VISIBLE);
                        btn_register.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorChatBackgroundShadow));
                        String txt_username = username.getEditText().getText().toString().trim();
                        String txt_email = email.getEditText().getText().toString().trim();
                        String txt_password = password.getEditText().getText().toString().trim();
                        String txt_version = version.getEditText().getText().toString().trim();

                        register(txt_username, txt_email, txt_password, txt_version);
                    } else {
                        snackbarInternet.show();
                    }
                }
            }
        });
    }

    private void register(final String username, final String email, String password, final String version) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username", username);
                    hashMap.put("email", email);
                    hashMap.put("version", version);
                    hashMap.put("pid", "");
                    hashMap.put("pusername", "");
                    hashMap.put("pemail", "");
                    hashMap.put("partnered", 0);
                    hashMap.put("seen", ServerValue.TIMESTAMP);
                    hashMap.put("online", false);

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, FindPartnerActivity.class);
                                intent.putExtra("flavour", version);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                enableBtnRegister();
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.constraint_layout), task.getException().getMessage(), Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        }
                    });
                } else {
                    enableBtnRegister();
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.constraint_layout), task.getException().getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    private void enableBtnRegister() {
        btn_register.setEnabled(true);
        progress.setVisibility(View.INVISIBLE);
        btn_register.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
    }
}
