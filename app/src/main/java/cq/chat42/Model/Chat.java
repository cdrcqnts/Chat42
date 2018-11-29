package cq.chat42.Model;

public class Chat {

    private String id;
    private String sender;
    private String receiver;
    private String message;
    private long time;
    private String seen;
    private int day;

    public Chat(String id, String sender, String receiver, String message, long time, String seen, int day) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.seen = seen;
        this.day = day;
    }

    public Chat() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
