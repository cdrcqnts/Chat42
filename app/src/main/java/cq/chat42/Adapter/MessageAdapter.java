package cq.chat42.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import cq.chat42.Aux;
import cq.chat42.Model.Chat;
import cq.chat42.R;

public class MessageAdapter extends RecyclerView.Adapter {

    private static final String TAG = "MessageAdapter";


    private Aux aux;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_TYPE_RIGHT_FIRST = 2;
    private static final int MSG_TYPE_LEFT_FIRST = 3;
    private Context mContext;
    private List<Chat> mChat;
    private String mPusername;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String mPusername) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.mPusername = mPusername;
        this.aux = new Aux(mContext);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new RightMessageHolder(view);
        }
        else if (viewType == MSG_TYPE_RIGHT_FIRST) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right_first, parent, false);
            return new RightFirstMessageHolder(view);
        }
        else if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new LeftMessageHolder(view);
        }
        else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left_first, parent, false);
            return new LeftFirstMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);

        switch (holder.getItemViewType()) {
            // send message
            case MSG_TYPE_RIGHT:
                ((RightMessageHolder) holder).bind(chat, position);
                break;
            case MSG_TYPE_RIGHT_FIRST:
                ((RightFirstMessageHolder) holder).bind(chat, position);
                break;
            // received message
            case MSG_TYPE_LEFT:
                ((LeftMessageHolder) holder).bind(chat, position);
                break;
            case MSG_TYPE_LEFT_FIRST:
                ((LeftFirstMessageHolder) holder).bind(chat, position);
                break;
        }
    }



    @Override
    public int getItemCount() {
        return mChat.size();
    }


    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        Chat chat = mChat.get(position);
        Chat prevChat = null;
        if (position > 0) {
            prevChat = mChat.get(position - 1);
        }
        String sender = chat.getSender();

        if (sender.equals(fuser.getUid())) {
            // send message
            if (prevChat == null || !prevChat.getSender().equals(fuser.getUid())) {
                return MSG_TYPE_RIGHT_FIRST;
            } else {
                return MSG_TYPE_RIGHT;
            }
        } else {
            // received message
            if (prevChat == null || prevChat.getSender().equals(fuser.getUid())) {
                return MSG_TYPE_LEFT_FIRST;
            }
            return MSG_TYPE_LEFT;
        }

    }

    private class RightMessageHolder extends RecyclerView.ViewHolder {
        TextView message, seen, delivered;

        RightMessageHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.show_message);
            delivered = itemView.findViewById(R.id.chat_item_delivered);
            seen = itemView.findViewById(R.id.chat_item_seen);
        }

        void bind(Chat chat, int position) {
            message.setText(chat.getMessage());

            if (chat.getSeen() != null && !chat.getSeen().equals("")) {
                String emojiMouthless = mContext.getResources().getString(R.string.emoji_mouthless);
                seen.setText(chat.getSeen());
                if (chat.getSeen().equals(emojiMouthless)) {
                    seen.setAlpha(Aux.ALPHA_HALF);
                }
                else {
                    seen.setAlpha(Aux.ALPHA_FULL);
                }
            }
        }
    }

    private class RightFirstMessageHolder extends RecyclerView.ViewHolder {
        TextView message, time, seen, delivered;

        RightFirstMessageHolder(View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.show_message);
            time = itemView.findViewById(R.id.chat_item_time);
            delivered = itemView.findViewById(R.id.chat_item_delivered);
            seen = itemView.findViewById(R.id.chat_item_seen);
        }

        void bind(Chat chat, int position) {
            message.setText(chat.getMessage());

            time.setText(aux.getDayHourMinute(chat.getTime()));

            if (chat.getSeen() != null && !chat.getSeen().equals("")) {
                String emojiMouthless = mContext.getResources().getString(R.string.emoji_mouthless);
                seen.setText(chat.getSeen());
                if (chat.getSeen().equals(emojiMouthless)) {
                    seen.setAlpha(Aux.ALPHA_HALF);
                }
                else {
                    seen.setAlpha(Aux.ALPHA_FULL);
                }
            }
        }
    }

    private class LeftMessageHolder extends RecyclerView.ViewHolder {
        TextView message;
        ConstraintLayout layoutMessage;

        LeftMessageHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.show_message);
            layoutMessage = itemView.findViewById(R.id.layout_show_message);
        }

        void bind(Chat chat, int position) {
            message.setText(chat.getMessage());
        }
    }

    private class LeftFirstMessageHolder extends RecyclerView.ViewHolder {
        TextView message, time, name;

        LeftFirstMessageHolder(View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.show_message);
            time = itemView.findViewById(R.id.chat_item_time);
            name = itemView.findViewById(R.id.chat_item_username);

        }

        void bind(Chat chat, int position) {
            message.setText(chat.getMessage());
            time.setText(aux.getDayHourMinute(chat.getTime()));
            name.setText(mPusername);
        }
    }



}
