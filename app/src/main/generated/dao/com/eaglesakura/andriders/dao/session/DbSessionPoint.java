package com.eaglesakura.andriders.dao.session;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "DB_SESSION_POINT".
 */
@Entity
public class DbSessionPoint {

    @Id
    @NotNull
    @Unique
    private java.util.Date date;

    @NotNull
    private byte[] central;

    @Generated
    public DbSessionPoint() {
    }

    public DbSessionPoint(java.util.Date date) {
        this.date = date;
    }

    @Generated
    public DbSessionPoint(java.util.Date date, byte[] central) {
        this.date = date;
        this.central = central;
    }

    @NotNull
    public java.util.Date getDate() {
        return date;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDate(@NotNull java.util.Date date) {
        this.date = date;
    }

    @NotNull
    public byte[] getCentral() {
        return central;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCentral(@NotNull byte[] central) {
        this.central = central;
    }

}
