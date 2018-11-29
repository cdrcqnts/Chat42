package cq.chat42;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.mikepenz.iconics.view.IconicsImageView;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.xw.repo.XEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cq.chat42.Adapter.MessageAdapter;
import cq.chat42.Model.Chat;
import cq.chat42.Model.User;
import cq.chat42.Notifications.APIService;
import cq.chat42.Notifications.Client;
import cq.chat42.Notifications.Data;
import cq.chat42.Notifications.MyResponse;
import cq.chat42.Notifications.Sender;
import cq.chat42.Notifications.Token;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements cq.chat42.EmojiDialog.EmojiDialogListener, EasyPermissions.PermissionCallbacks, Detector.ImageListener, CameraDetector.CameraEventListener {

    private static final String TAG = "MainActivity";
    Aux aux;
    Snackbar snackbar;
    String title;

    FirebaseUser fuser;
    DatabaseReference reference, reference_partner, reference_chats, reference_user;

    String pid, pusername, pemail, uid, username, email, flavour;
    long partnered;
    Intent intent;

    IconicsImageView btn_emoji, btn_send;
    XEditText txt_send;
    FloatingActionButton btn_read, btn_current_emoji;
    TextView txt_current_emoij;

    RelativeLayout bottom;

    MessageAdapter messageAdapter;
    List<Chat> mChat;
    Chat mNotYetRead;
    Chat mNotYetReacted;
    int countUnread;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    SwipyRefreshLayout mSwipeLayout;

    // Notification
    APIService apiService;
    boolean notify = false;

    EmojiDialog emojiDialog;

    //Affdexsdk
    String currentEmoji, lastEmoji, emojiMouthless, emojiUnknown;
    SurfaceView cameraPreview;
    CameraDetector detector;
    RelativeLayout mainLayout;


    int previewWidth = 0;
    int previewHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        // Initialize Emojis
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);

        emojiMouthless = getApplicationContext().getResources().getString(R.string.emoji_mouthless);
        emojiUnknown = getApplicationContext().getResources().getString(R.string.emoji_unknown);
        currentEmoji = getApplicationContext().getResources().getString(R.string.emoji_unknown);
        lastEmoji = getApplicationContext().getResources().getString(R.string.emoji_unknown);

        aux = new Aux(getApplicationContext());
        snackbar = Snackbar.make(findViewById(R.id.coordinator_layout_chat), "", Snackbar.LENGTH_SHORT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setSubtitle("");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        mSwipeLayout = findViewById(R.id.swipe_layout);
        mSwipeLayout.setEnabled(false);

        bottom = findViewById(R.id.bottom);

        btn_send = findViewById(R.id.btn_send);
        btn_emoji = findViewById(R.id.btn_emoji);
        txt_send = findViewById(R.id.txt_send);
        txt_send.setEnabled(true);

        btn_read = findViewById(R.id.fab_read_new_msg);
        btn_current_emoji = findViewById(R.id.fab_current_emoji);
        btn_read.hide();
        btn_current_emoji.hide();
        btn_current_emoji.setEnabled(false);

        txt_current_emoij = findViewById(R.id.txt_current_emoji);
        txt_current_emoij.setVisibility(View.INVISIBLE);
        txt_current_emoij.setText(emojiUnknown);
        //txt_current_emoij.setAlpha((float) 0.3);

        btn_send.setIcon("gmd-send");
        btn_send.setEnabled(false);
        btn_send.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorLightGrey));

        intent = getIntent();
        uid = intent.getStringExtra("uid");
        pid = intent.getStringExtra("pid");
        flavour = intent.getStringExtra("flavour");
        pusername = intent.getStringExtra("pusername");
        partnered = intent.getLongExtra("partnered", 0);
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        //Log.d(TAG, "onCreate: FLAVOUR " + flavour);

        // affdexsdk
        mainLayout = findViewById(R.id.main_layout);

        cameraPreview = new SurfaceView(this) {
            @Override
            public void onMeasure(int widthSpec, int heightSpec) {
                int measureWidth = MeasureSpec.getSize(widthSpec);
                int measureHeight = MeasureSpec.getSize(heightSpec);
                int width;
                int height;
                if (previewHeight == 0 || previewWidth == 0) {
                    width = measureWidth;
                    height = measureHeight;
                } else {
                    float viewAspectRatio = (float)measureWidth/measureHeight;
                    float cameraPreviewAspectRatio = (float) previewWidth/previewHeight;

                    if (cameraPreviewAspectRatio > viewAspectRatio) {
                        width = measureWidth;
                        height =(int) (measureWidth / cameraPreviewAspectRatio);
                    } else {
                        width = (int) (measureHeight * cameraPreviewAspectRatio);
                        height = measureHeight;
                    }
                }
                setMeasuredDimension(width,height);
            }
        };

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        cameraPreview.setLayoutParams(params);
        cameraPreview.setAlpha(0);
        cameraPreview.getHolder().setFixedSize(1, 1);
        mainLayout.addView(cameraPreview,0);

        //detectorAlt = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT)
        detector = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT, cameraPreview);
        detector.setSendUnprocessedFrames(false);
        detector.setDetectAllEmojis(true);
        detector.setImageListener(this);
        detector.setOnCameraEventListener(this);
        detector.setMaxProcessRate(2);
        detector.disableAnalytics();

//        if (!flavour.equals(aux.GROUP_A)) {
//            startDetector();
//        }

        updateToken(FirebaseInstanceId.getInstance().getToken());

        reference_chats = FirebaseDatabase.getInstance().getReference("Chats").child(flavour);
        reference_partner = FirebaseDatabase.getInstance().getReference("Users").child(pid);
        reference_user = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        reference_partner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User partner = dataSnapshot.getValue(User.class);
                pusername = partner.getUsername();
                pemail = partner.getEmail();
                username = partner.getPusername();
                email = partner.getPemail();
                partnered = partner.getPartnered();
                getSupportActionBar().setTitle(pusername);
                if (partner.getOnline() != null && partner.getOnline()) {
                    getSupportActionBar().setSubtitle("online");
                } else {
                    String seen = aux.getTimeAgo(partner.getSeen());
                    getSupportActionBar().setSubtitle("Seen " + seen);
                }
                readMessages(uid, pid, pusername);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (aux.internetIsAvailable()) {
                    if (mNotYetRead != null) {
                        deactivateButtons();
                        sendSeenReceipt(mNotYetRead);
                    }
                } else {
                    snackbar.setText(aux.MSG_ERR_INTERNET);
                    snackbar.show();
                    if (mSwipeLayout.isRefreshing()) {
                        mSwipeLayout.setRefreshing(false);
                    }
                }

            }
        });

        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aux.internetIsAvailable()) {
                    if (mNotYetRead != null) {
                        deactivateButtons();
                        sendSeenReceipt(mNotYetRead);
                    }
                } else {
                    snackbar.setText(aux.MSG_ERR_INTERNET);
                    snackbar.show();
                }
            }
        });

        txt_send.setOnXTextChangeListener(new XEditText.OnXTextChangeListener() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBtnSend();
                Log.d(TAG, "onTextChanged: " + s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aux.internetIsAvailable()) {
                    if (mNotYetRead == null) {
                        String msg = txt_send.getText().toString().trim();
                        if (!msg.equals("")) {
                            deactivateButtons();
                            notify = true;
                            txt_send.setText("");
                            sendMessage(uid, pid, msg);
                        }
                    } else {
                        snackbar.setText(aux.MSG_NEW_MESSAGES);
                        snackbar.show();
                    }
                } else {
                    snackbar.setText(aux.MSG_ERR_INTERNET);
                    snackbar.show();
                }

            }
        });

        btn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (flavour.equals(aux.GROUP_A)) {
                    // emoji dialog
                    openEmojiDialog();
                } else {
                    // emoji cam
                    if (btn_current_emoji.isShown()) {
                        btn_emoji.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorDarkGrey));
                        btn_current_emoji.hide();
                        btn_current_emoji.setEnabled(false);
                        txt_current_emoij.setVisibility(View.INVISIBLE);
                    } else {
                        btn_emoji.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
                        btn_current_emoji.show();
                        btn_current_emoji.setEnabled(true);
                        txt_current_emoij.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        btn_current_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_send.setDisableEmoji(false);
                txt_send.getText().insert(txt_send.getSelectionStart(), currentEmoji + " ");
                txt_send.setDisableEmoji(true);
                btn_emoji.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorDarkGrey));
                btn_current_emoji.setEnabled(false);
                btn_current_emoji.hide();
                txt_current_emoij.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (flavour.equals(aux.GROUP_C) && mNotYetReacted != null) {
            sendEmojiReceipt(mNotYetReacted);
        }
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void deactivateButtons() {
        mSwipeLayout.setEnabled(false);
        btn_read.hide();
        btn_send.setEnabled(false);
        btn_send.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorLightGrey));
    }

    private void sendSeenReceipt(final Chat chat) {
        // if i received a message from my partner and the message has not been seen yet
        // i send seen = ✔

        // disable live emoj preview when reading starts
        disableBtnEmoji();

        lastEmoji = emojiMouthless;
        String emojiCheck = getApplicationContext().getResources().getString(R.string.emoji_heavy_checkmark);
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        reference_chats.child(chat.getId()).child("seen").setValue(emojiCheck).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mNotYetRead = null;
                    mNotYetReacted = chat;
                    readMessages(uid, pid, pusername);
                }
                if (mSwipeLayout.isRefreshing()) {
                    mSwipeLayout.setRefreshing(false);
                }
            }
        });
    }

    private void disableBtnEmoji() {
        btn_emoji.setEnabled(false);
        btn_emoji.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorLightGrey));

        if (!flavour.equals(aux.GROUP_A)) {
            hideBtnCurrentEmoji();
        }
    }

    private void enableBtnEmoji() {
        btn_emoji.setEnabled(true);
        btn_emoji.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorDarkGrey));
    }

    private void enableBtnCurrentEmoji() {
        if (!btn_current_emoji.isShown()) {
            btn_current_emoji.show();
            txt_current_emoij.setVisibility(View.VISIBLE);
        }
    }

    private void hideBtnCurrentEmoji() {
        if (btn_current_emoji.isShown()) {
            btn_current_emoji.hide();
            txt_current_emoij.setVisibility(View.INVISIBLE);
        }
    }

    private void sendEmojiReceipt(final Chat chat) {
        String emoji;
        String emojiUnknown = getApplicationContext().getResources().getString(R.string.emoji_unknown);

        if (lastEmoji.equals(emojiMouthless)) {
            emoji = lastEmoji;
        }
        else if ((currentEmoji.equals(emojiUnknown) && lastEmoji.equals(emojiUnknown)) || !currentEmoji.equals(emojiUnknown)) {
            emoji = currentEmoji;
        }
        else {
            emoji = lastEmoji;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        reference_chats.child(chat.getId()).child("seen").setValue(emoji).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mNotYetReacted = null;
                    readMessages(uid, pid, pusername);
                } else {
                    snackbar.setText(aux.MSG_EMOJI_RECEIPT);
                    snackbar.show();
                }

                // enable live emoj preview when reading ends
//                if (flavour.equals(aux.GROUP_C)) {
//                    btn_emoji.setEnabled(true);
//                    btn_emoji.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorDarkGrey));
//
//                    if (!btn_current_emoji.isShown()) {
//                        btn_current_emoji.show();
//                        txt_current_emoij.setVisibility(View.VISIBLE);
//                    }
//                }
            }
        });
    }

    private void sendMessage(String sender, final String receiver, final String message) {
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String id = reference_chats.push().getKey();
        Date today = new Date();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("time", ServerValue.TIMESTAMP);
        hashMap.put("seen", "");
        hashMap.put("day", aux.daysSinceTrialStarted(partnered));

        reference_chats.child(id).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                } else {
                    txt_send.setText(message);
                }
            }
        });

        // send notification
        final String msg = message;

        reference_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, username, msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, final String username, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.drawable.ic_stat_chat, "Touch to read the message.", "New Message from " + username, pid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if (response.code() == 200) {
                                    if (response.body().success != 1) {
                                        //Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    //Toast.makeText(MainActivity.this, "response code is not 200", Toast.LENGTH_SHORT).show();
                                }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(final String uid, final String pid, final String pusername) {
        mChat = new ArrayList<>();
        reference_chats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                countUnread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    Boolean partnerSend = chat.getReceiver().equals(uid) && chat.getSender().equals(pid);
                    Boolean iSend = chat.getReceiver().equals(pid) && chat.getSender().equals(uid);
                    if (partnerSend || iSend) {
                        Boolean iNotSeen = chat.getSeen() == null || chat.getSeen().equals("");
                        if (partnerSend && iNotSeen) {
                            countUnread++;
                            if (mNotYetRead == null) {
                                mNotYetRead = chat;
                            }
                        } else {
                            mChat.add(chat);
                        }
                    }
                }

                if (countUnread == 0) {
                    // no new messages
                    getSupportActionBar().setTitle(pusername);
                    mSwipeLayout.setEnabled(false);
                    bottom.setBackgroundResource(R.drawable.border_new_message_off);
                    bottom.setPadding(5, 10, 5, 5);
                    //txt_send.setEnabled(true);
                    btn_read.hide();
                    enableBtnEmoji();
                } else {
                    // new messages!
                    getSupportActionBar().setTitle(pusername + " (" + countUnread + ")");
                    mSwipeLayout.setEnabled(true);
                    bottom.setBackgroundResource(R.drawable.border_new_message_on);
                    bottom.setPadding(5, 10, 5, 5);
                    //txt_send.setEnabled(false);
                    btn_read.show();
                    disableBtnEmoji();
                }

                String msg = txt_send.getText().toString().trim();

                // disable btn_send if new message comes in while writing
                if (msg.length() != 0 && mNotYetRead == null) {
                    btn_send.setEnabled(true);
                    btn_send.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorDarkGrey));
                } else {
                    btn_send.setEnabled(false);
                    btn_send.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorLightGrey));
                }

                messageAdapter = new MessageAdapter( MainActivity.this, mChat, pusername);
                recyclerView.setAdapter(messageAdapter);
                linearLayoutManager.scrollToPosition(mChat.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateBtnSend() {
        String msg = txt_send.getText().toString().trim();
        if (msg.length() == 0 || mNotYetRead != null) {
            btn_send.setEnabled(false);
            btn_send.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorLightGrey));
        } else {
            btn_send.setEnabled(true);
            btn_send.getIcon().color(ContextCompat.getColor(getApplicationContext(),R.color.colorDarkGrey));
        }
    }

    private void currentUser(String uid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREPS", MODE_PRIVATE).edit();
        editor.putString("uid", uid);
        editor.apply();
    }


    private void status(Boolean online) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", online);
        hashMap.put("seen", ServerValue.TIMESTAMP);
        reference_user.updateChildren(hashMap);
    }

    private void openEmojiDialog() {
        emojiDialog = new EmojiDialog();
        emojiDialog.show(getSupportFragmentManager(), "Emoji Dialog");
    }

    @Override
    public void getSelectedEmoji(String emoji) {
        txt_send.setDisableEmoji(false);
        txt_send.getText().insert(txt_send.getSelectionStart(), emoji + " ");
        txt_send.setDisableEmoji(true);
        emojiDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(true);
        currentUser(uid);
        if (!flavour.equals(aux.GROUP_A)) {
            startDetector();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(false);
        currentUser("none");
        if (!flavour.equals(aux.GROUP_A)) {
            stopDetector();
        }

        if (flavour.equals(aux.GROUP_C) && mNotYetReacted != null) {
            sendEmojiReceipt(mNotYetReacted);
        }
    }

    @AfterPermissionGranted(123)
    void startDetector() {
        String[] perms = {android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // When permission are granted start the detector
            if (!detector.isRunning()) {
                detector.start();
                //detector.reset();
            }
        } else {
            EasyPermissions.requestPermissions(this, "Access to camera, storage and internet is required.", 123, perms);
        }
    }

    void stopDetector() {
        if (detector.isRunning()) {
            detector.stop();
        }
    }




        // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                intent =  new Intent(MainActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            case R.id.account:
                intent = new Intent(MainActivity.this, AccountActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("pid", pid);
                intent.putExtra("flavour", flavour);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                intent.putExtra("pusername", pusername);
                intent.putExtra("pemail", pemail);
                intent.putExtra("partnered", partnered);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onCameraSizeSelected(int width, int height, Frame.ROTATE rotate) {
        if (rotate == Frame.ROTATE.BY_90_CCW || rotate == Frame.ROTATE.BY_90_CW) {
            previewWidth = height;
            previewHeight = width;
        } else {
            previewHeight = height;
            previewWidth = width;
        }
        cameraPreview.requestLayout();
    }

    @Override
    public void onImageResults(List<Face> list, Frame frame, float v) {
        if (list == null)
            return;
        if (list.size() == 0) {
            txt_current_emoij.setAlpha((float) 0.3);
        } else {
            txt_current_emoij.setAlpha((float) 1);
            Face face = list.get(0);
            String capturedEmoji = face.emojis.getDominantEmoji().getUnicode();
            if (!capturedEmoji.equals(txt_current_emoij.getText())) {
                lastEmoji = currentEmoji;
                currentEmoji = capturedEmoji;
                txt_current_emoij.setText(capturedEmoji);
            }
            if (!btn_current_emoji.isEnabled()) {
                btn_current_emoji.setEnabled(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }


    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
        }
    }
}
