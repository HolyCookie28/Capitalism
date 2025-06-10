package com.ohad_d.model;

import android.graphics.Bitmap;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class User extends BaseEntity implements Serializable {
    protected String name;
    protected String picture;
    protected String email;
    protected String password;

    public User() {}

    public User(String name, String picture, String email, String password) {
        this.name = name;
        this.picture = picture;
        this.email = email;
        this.password = password;
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(picture, user.picture) && Objects.equals(email, user.email) && Objects.equals(password, user.password);
    }
}
