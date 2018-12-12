package cq.chat42;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Aux extends Application {
    private static final String TAG = "Aux";
    Context mContext;

    public static final int TRIAL_DAYS = 20;

    public static final String GROUP_A = "horseradish";
    public static final String GROUP_B = "cauliflower";
    public static final String GROUP_C = "rutabaga";

    public static final String EMOJI_WINK = "\uD83D\uDE09";
    public static final String EMOJI_TONG = "\uD83D\uDE1B";
    public static final String EMOJI_TONG_WINK = "\uD83D\uDE1C";
    public static final String EMOJI_SMIRK = "\uD83D\uDE0F";
    public static final String EMOJI_SMILE = "\uD83D\uDE03";
    public static final String EMOJI_SCREAM = "\uD83D\uDE31";
    public static final String EMOJI_RELAXED = "\u263A";
    public static final String EMOJI_RAGE = "\uD83D\uDE21";
    public static final String EMOJI_LAUGHING = "\uD83D\uDE06";
    public static final String EMOJI_KISSING = "\uD83D\uDE17";
    public static final String EMOJI_FLUSHED = "\uD83D\uDE33";
    public static final String EMOJI_DISAPPOINTED = "\uD83D\uDE1E";

    public static final float ALPHA_TRANSPARENT = 0;
    public static final float ALPHA_DISABLED = (float) .1;
    public static final float ALPHA_HALF = (float) .5;
    public static final float ALPHA_FULL = 1;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static final String MSG_ERR_INTERNET = "No internet connection.";
    public static final String MSG_ERR_CAMERA = "No front camera detected.";
    public static final String MSG_NEW_MESSAGES = "You have new messages! Swipe up or clicking on the news button.";
    public static final String MSG_EMOJI_RECEIPT = "Failed sending emoji read receipt.";

    // constructor
    public Aux(Context context){
        this.mContext = context;
    }

    public boolean frontCameraIsAvailable() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    public boolean internetIsAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean trialIsOver(long partnered) {
        return 0 >= timeLeftForTrial(partnered);
    }

    public long timeLeftForTrial(long partnered) {
        Date trialStart = new Date(partnered);
        Calendar c = Calendar.getInstance();
        c.setTime(trialStart);
        c.add(Calendar.DATE, TRIAL_DAYS);
        Date trialEnd = c.getTime();
        Date now = new Date();

        long daysLeft = getDifferenceDays(now, trialEnd);

        return daysLeft;
    }

    public long daysSinceTrialStarted(long partnered) {
        Date trialStart = new Date(partnered);
        Date today = new Date();
        long daysPassed = getDifferenceDays(trialStart, today) + 1;

        return daysPassed;
    }

    public static long getDifferenceDays(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public String getDayHourMinute(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, H:mm");
        return dateFormat.format(time);
    }

    public String getDate(long time) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MMM yyyy");
        return dateFormat.format(time);
    }

    public String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "just now";
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
