package cq.chat42;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.text.emoji.widget.EmojiAppCompatTextView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EmojiDialog extends AppCompatDialogFragment {
    private TextView emojiBtnWink;
    private TextView emojiBtnTong;
    private TextView emojiBtnTongWink;
    private TextView emojiBtnSmirk;
    private TextView emojiBtnSmile;
    private TextView emojiBtnScream;
    private TextView emojiBtnRelaxed;
    private TextView emojiBtnRage;
    private TextView emojiBtnLaughing;
    private TextView emojiBtnKissing;
    private EmojiAppCompatTextView emojiBtnFlushed;
    private TextView emojiBtnDisappointed;

    Aux aux;

    private EmojiDialogListener listener;

    private String emoji = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.emoji_dialog_layout, null);
        builder.setView(view)
                .setTitle("Insert Emoji")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


        emojiBtnWink = view.findViewById(R.id.emoji_btn_wink);
        emojiBtnTong = view.findViewById(R.id.emoji_btn_tong);
        emojiBtnTongWink = view.findViewById(R.id.emoji_btn_tong_wink);
        emojiBtnSmirk = view.findViewById(R.id.emoji_btn_smirk);
        emojiBtnSmile = view.findViewById(R.id.emoji_btn_smile);
        emojiBtnScream = view.findViewById(R.id.emoji_btn_scream);
        emojiBtnRelaxed = view.findViewById(R.id.emoji_btn_relaxed);
        emojiBtnRage = view.findViewById(R.id.emoji_btn_rage);
        emojiBtnLaughing = view.findViewById(R.id.emoji_btn_laughing);
        emojiBtnKissing = view.findViewById(R.id.emoji_btn_kissing);
        emojiBtnFlushed = view.findViewById(R.id.emoji_btn_flushed);
        emojiBtnDisappointed = view.findViewById(R.id.emoji_btn_disapp);

        emojiBtnWink.setText(Aux.EMOJI_WINK);
        emojiBtnTong.setText(Aux.EMOJI_TONG);
        emojiBtnTongWink.setText(Aux.EMOJI_TONG_WINK);
        emojiBtnSmirk.setText(Aux.EMOJI_SMIRK);
        emojiBtnSmile.setText(Aux.EMOJI_SMILE);
        emojiBtnScream.setText(Aux.EMOJI_SCREAM);
        emojiBtnRelaxed.setText(Aux.EMOJI_RELAXED);
        emojiBtnRage.setText(Aux.EMOJI_RAGE);
        emojiBtnLaughing.setText(Aux.EMOJI_LAUGHING);
        emojiBtnKissing.setText(Aux.EMOJI_KISSING);
        emojiBtnFlushed.setText(Aux.EMOJI_FLUSHED);
        emojiBtnDisappointed.setText(Aux.EMOJI_DISAPPOINTED);



        emojiBtnWink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_WINK);
            }
        });

        emojiBtnTong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_TONG);
            }
        });

        emojiBtnTongWink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_TONG_WINK);
            }
        });

        emojiBtnSmirk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_SMIRK);
            }
        });

        emojiBtnSmile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_SMILE);
            }
        });

        emojiBtnScream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_SCREAM);
            }
        });


        emojiBtnRelaxed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_RELAXED);
            }
        });

        emojiBtnRage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_RAGE);
            }
        });

        emojiBtnLaughing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_LAUGHING);
            }
        });

        emojiBtnKissing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_KISSING);
            }
        });

        emojiBtnFlushed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_FLUSHED);
            }
        });

        emojiBtnDisappointed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getSelectedEmoji(Aux.EMOJI_DISAPPOINTED);
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (EmojiDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement InsertEmojiDialogListener");
        }
    }

    public interface EmojiDialogListener{
        void getSelectedEmoji(String emoji);
    }
}
