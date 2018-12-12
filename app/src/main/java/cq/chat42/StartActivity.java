package cq.chat42;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cq.chat42.Model.User;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    Aux aux;
    Snackbar snackbarInternet, snackbarCamera;

    Button btn_login, btn_register;
    ProgressBar progress;
    FirebaseUser currentUser;
    DatabaseReference reference;

    @Override
    protected void onStart() {
        super.onStart();
        checkRestrictions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        aux = new Aux(getApplicationContext());

        btn_login = findViewById(R.id.start_btn_login);
        btn_register = findViewById(R.id.start_btn_register);
        progress = findViewById(R.id.progress);

        progress.setVisibility(View.VISIBLE);
        btn_login.setVisibility(View.INVISIBLE);
        btn_register.setVisibility(View.INVISIBLE);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aux.internetIsAvailable()) {
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    snackbarInternet.show();
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aux.internetIsAvailable()) {
                    Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
                    startActivity(intent);
                } else {
                    snackbarInternet.show();
                }
            }
        });
    }

    private void checkRestrictions() {
        snackbarInternet = Snackbar.make(findViewById(R.id.constraint_layout), Aux.MSG_ERR_INTERNET, Snackbar.LENGTH_INDEFINITE);
        snackbarCamera = Snackbar.make(findViewById(R.id.constraint_layout), Aux.MSG_ERR_CAMERA, Snackbar.LENGTH_INDEFINITE);
        snackbarInternet.setAction("Retry", new InternetListener());
        snackbarCamera.setAction("Retry", new CameraListener());
        if (aux.internetIsAvailable()) {
            if (aux.frontCameraIsAvailable()) {
                redirect();
            } else {
                snackbarCamera.show();
            }
        } else {
            snackbarInternet.show();
        }
    }

    private void redirect() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // is the user registered and logged in?
        if (currentUser != null) {
            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Intent intent;
                    // is the trial over?
                    if (user.getPartnered() != 0 && aux.trialIsOver(user.getPartnered())) {
                        // redirect to TrialIsOverActivity
                        intent = new Intent(StartActivity.this, TrialIsOverActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // does the user have a partner?
                        if (user.getPid() == null || user.getPid().equals("")) {
                            intent = new Intent(StartActivity.this, FindPartnerActivity.class);
                            intent.putExtra("flavour", user.getVersion());
                        } else {
                            intent = new Intent(StartActivity.this, MainActivity.class);
                            intent.putExtra("uid", user.getId());
                            intent.putExtra("pid", user.getPid());
                            intent.putExtra("flavour", user.getVersion());
                            intent.putExtra("pusername", user.getPusername());
                            intent.putExtra("partnered", user.getPartnered());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            //stay here
            progress.setVisibility(View.INVISIBLE);
            btn_login.setVisibility(View.VISIBLE);
            btn_register.setVisibility(View.VISIBLE);
        }
    }

    private class InternetListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            checkRestrictions();
        }
    }


    private class CameraListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            checkRestrictions();
        }
    }
}
