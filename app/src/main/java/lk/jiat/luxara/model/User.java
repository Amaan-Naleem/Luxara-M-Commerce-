package lk.jiat.luxara.model;

import lombok.Builder;

@Builder
public class User {

    private String uid;
    private String name;
    private String email;
    private String profilePicUrl;

    public User() {
    }

    public User(String uid, String name, String email, String profilePicUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profilePicUrl = profilePicUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
