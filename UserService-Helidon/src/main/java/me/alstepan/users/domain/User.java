package me.alstepan.users.domain;

import java.io.Serializable;

public class User implements Serializable{

    private long id = 0;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public User() {}

    public User(long id, String userName, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public long id() { return id; }
    public String userName() { return userName; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String email() { return email; }
    public String phone() { return phone; }

    public User setId(long id) {
        this.id = id;
        return this;
    }

    public User setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setPhone(String phone) {
        this.phone = phone;
        return this;
    }
}
