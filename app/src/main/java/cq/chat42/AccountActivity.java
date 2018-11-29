package cq.chat42;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    Aux aux;

    String uid, pid, flavour, pusername;
    long partnered;
    Intent intent;

    TextView emojis_1_4, emojis_5_8, emojis_9_12;
    TextView username, email, partnerUsername, pemail, trialStart, trialEnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        aux = new Aux(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        emojis_1_4 = findViewById(R.id.emoijs_1_4);
        emojis_5_8 = findViewById(R.id.emojis_5_8);
        emojis_9_12 = findViewById(R.id.emojis_9_12);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        partnerUsername = findViewById(R.id.pusername);
        pemail = findViewById(R.id.pemail);
        trialStart = findViewById(R.id.trialStart);
        trialEnd = findViewById(R.id.trialEnd);

        intent = getIntent();
        uid = intent.getStringExtra("uid");
        pid = intent.getStringExtra("pid");
        flavour = intent.getStringExtra("flavour");
        pusername = intent.getStringExtra("pusername");
        partnered = intent.getLongExtra("partnered", 0);

        username.setText(intent.getStringExtra("username"));
        email.setText(intent.getStringExtra("email"));
        partnerUsername.setText(intent.getStringExtra("pusername"));
        pemail.setText(intent.getStringExtra("pemail"));
        trialStart.setText(aux.getDate(partnered));
        trialEnd.setText(String.valueOf(aux.timeLeftForTrial(partnered)));

//        Log.d(TAG, "onCreate: partnered " + intent.getLongExtra("partnered", 0));
//        Log.d(TAG, "onCreate: aux.timeleftfortrial: " + aux.timeLeftForTrial(partnered));

        aux.getDate(partnered);

        emojis_1_4.setText(Aux.EMOJI_WINK + " " + Aux.EMOJI_TONG + " " + Aux.EMOJI_TONG_WINK + " " + Aux.EMOJI_SMIRK);
        emojis_5_8.setText(Aux.EMOJI_SMILE + " " + Aux.EMOJI_SCREAM + " " + Aux.EMOJI_RELAXED + " " + Aux.EMOJI_RAGE);
        emojis_9_12.setText(Aux.EMOJI_LAUGHING + " " + Aux.EMOJI_KISSING + " " + Aux.EMOJI_FLUSHED + " " + Aux.EMOJI_DISAPPOINTED);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("uid", uid);
                intent.putExtra("pid", pid);
                intent.putExtra("flavour", flavour);
                intent.putExtra("pusername", pusername);
                intent.putExtra("partnered", partnered);
                startActivity(intent);
                finish();
            }
        });
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
                intent =  new Intent(AccountActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }
}
