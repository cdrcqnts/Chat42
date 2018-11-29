package cq.chat42.Notifications;

public class Data {
    private String uid;
    private int icon;
    private String body;
    private String title;
    private String pid;

    public Data(String uid, int icon, String body, String title, String pid) {
        this.uid = uid;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.pid = pid;
    }

    public Data () {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
