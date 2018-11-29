package cq.chat42.Model;

public class User {
    private String id;
    private String username;
    private String email;
    private String version;
    private String pid;
    private String pusername;
    private String pemail;
    private long partnered;
    private Boolean online;
    private long seen;

    public User(String id, String username, String email, String version, String pid, String pusername, String pemail, long partnered, Boolean online, long seen) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.version = version;
        this.pid = pid;
        this.pusername = pusername;
        this.pemail = pemail;
        this.partnered = partnered;
        this.online = online;
        this.seen = seen;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPusername() {
        return pusername;
    }

    public void setPusername(String pusername) {
        this.pusername = pusername;
    }

    public String getPemail() {
        return pemail;
    }

    public void setPemail(String pemail) {
        this.pemail = pemail;
    }

    public long getPartnered() {
        return partnered;
    }

    public void setPartnered(long partnered) {
        this.partnered = partnered;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public long getSeen() {
        return seen;
    }

    public void setSeen(long seen) {
        this.seen = seen;
    }
}
