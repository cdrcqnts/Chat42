package cq.chat42;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cq.chat42.Model.User;

import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

public class FindPartnerActivity extends AppCompatActivity {

    private static final String TAG = "FindPartnerActivity";
    Aux aux;
    Snackbar snackbarInternet;
    AwesomeValidation mAwesomeValidation;

    TextInputLayout email;
    Button btn_search;
    FirebaseUser currentUser;
    DatabaseReference reference;
    ProgressBar progress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_partner);
        aux = new Aux(getApplicationContext());
        snackbarInternet = Snackbar.make(findViewById(R.id.constraint_layout), aux.MSG_ERR_INTERNET, Snackbar.LENGTH_LONG);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find your partner");

        mAwesomeValidation = new AwesomeValidation(TEXT_INPUT_LAYOUT);

        email = findViewById(R.id.find_partner_email);
        btn_search = findViewById(R.id.btn_pair);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        mAwesomeValidation.addValidation(FindPartnerActivity.this, R.id.find_partner_email, Patterns.EMAIL_ADDRESS, R.string.err_email);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mAwesomeValidation.validate()) {
                    if (aux.internetIsAvailable()) {
                        disableBtnSearch();
                        currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String txt_email = email.getEditText().getText().toString().trim();
                        assert currentUser != null;
                        // is the email address equal to the current users' email address?
                        if (!Objects.equals(currentUser.getEmail(), txt_email)) {
                            Query queryPartner = FirebaseDatabase.getInstance().getReference("Users")
                                    .orderByChild("email").equalTo(txt_email).limitToFirst(1);
                            queryPartner.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // is the email address registered?
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot entry: dataSnapshot.getChildren()) {
                                            User partner = entry.getValue(User.class);
                                            // does the potential partner have a partner already?
                                            if (partner.getPid() == null || partner.getPid().equals("")) {
                                                pairUsers(currentUser.getUid(), partner);
                                            } else {
                                                // am i the partner?
                                                // this happens if both users are in the FindPartnerActivity
                                                if (partner.getPid().equals(currentUser.getUid())) {
                                                    redirectToStart();
                                                }
                                                else {
                                                    enableBtnSearch();
                                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.constraint_layout), "This user is already paired.", Snackbar.LENGTH_SHORT);
                                                    snackbar.show();
                                                }
                                            }
                                        }
                                    } else {
                                        enableBtnSearch();
                                        Snackbar snackbar = Snackbar.make(findViewById(R.id.constraint_layout), "This email address is not registered yet.", Snackbar.LENGTH_SHORT);
                                        snackbar.show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            enableBtnSearch();
                            Snackbar snackbar = Snackbar.make(v, "E-mail address must be different from your own.", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                    } else {
                        snackbarInternet.show();
                    }
                }

            }


        });
    }

    private void disableBtnSearch() {
        btn_search.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        btn_search.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorChatBackgroundShadow));
    }

    private void enableBtnSearch() {
        btn_search.setEnabled(true);
        progress.setVisibility(View.INVISIBLE);
        btn_search.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
    }

    private void pairUsers(final String uid, final User partner) {
        Query queryCurrentUser = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("id").equalTo(uid).limitToFirst(1);
        queryCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot entry : dataSnapshot.getChildren()) {
                        User user = entry.getValue(User.class);

                        //pair partners
                        final String now = DateFormat.getDateTimeInstance().format(new Date());
                        Map requestMap = new HashMap();
                        requestMap.put("Users/" + uid + "/pid", partner.getId());
                        requestMap.put("Users/" + uid + "/partnered", ServerValue.TIMESTAMP);
                        requestMap.put("Users/" + uid + "/pemail", partner.getEmail());
                        requestMap.put("Users/" + uid + "/pusername", partner.getUsername());

                        requestMap.put("Users/" + partner.getId() + "/pid", uid);
                        requestMap.put("Users/" + partner.getId() + "/partnered", ServerValue.TIMESTAMP);
                        requestMap.put("Users/" + partner.getId() + "/pemail", user.getEmail());
                        requestMap.put("Users/" + partner.getId() + "/pusername", user.getUsername());

                        reference = FirebaseDatabase.getInstance().getReference();
                        reference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    enableBtnSearch();
                                    String error = databaseError.getMessage();
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.constraint_layout), error, Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                } else {
                                    redirectToStart();
                                }
                            }
                        });
                    }
                } else {
                    enableBtnSearch();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void redirectToStart() {
        Intent intent = new Intent(FindPartnerActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                intent =  new Intent(FindPartnerActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }
}
