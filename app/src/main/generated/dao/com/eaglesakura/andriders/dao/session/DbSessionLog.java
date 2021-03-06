package com.eaglesakura.andriders.dao.session;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "DB_SESSION_LOG".
 */
@Entity
public class DbSessionLog {

    @Id
    @NotNull
    @Unique
    private String sessionId;

    @NotNull
    private java.util.Date startTime;

    @NotNull
    private java.util.Date endTime;
    private long activeTimeMs;
    private double activeDistanceKm;

    @Index
    private double maxSpeedKmh;
    private int maxCadence;
    private int maxHeartrate;
    private double sumAltitude;

    @Index
    private double sumDistanceKm;
    private double calories;
    private double exercise;

    @Generated
    public DbSessionLog() {
    }

    public DbSessionLog(String sessionId) {
        this.sessionId = sessionId;
    }

    @Generated
    public DbSessionLog(String sessionId, java.util.Date startTime, java.util.Date endTime, long activeTimeMs, double activeDistanceKm, double maxSpeedKmh, int maxCadence, int maxHeartrate, double sumAltitude, double sumDistanceKm, double calories, double exercise) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeTimeMs = activeTimeMs;
        this.activeDistanceKm = activeDistanceKm;
        this.maxSpeedKmh = maxSpeedKmh;
        this.maxCadence = maxCadence;
        this.maxHeartrate = maxHeartrate;
        this.sumAltitude = sumAltitude;
        this.sumDistanceKm = sumDistanceKm;
        this.calories = calories;
        this.exercise = exercise;
    }

    @NotNull
    public String getSessionId() {
        return sessionId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSessionId(@NotNull String sessionId) {
        this.sessionId = sessionId;
    }

    @NotNull
    public java.util.Date getStartTime() {
        return startTime;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setStartTime(@NotNull java.util.Date startTime) {
        this.startTime = startTime;
    }

    @NotNull
    public java.util.Date getEndTime() {
        return endTime;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEndTime(@NotNull java.util.Date endTime) {
        this.endTime = endTime;
    }

    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    public void setActiveTimeMs(long activeTimeMs) {
        this.activeTimeMs = activeTimeMs;
    }

    public double getActiveDistanceKm() {
        return activeDistanceKm;
    }

    public void setActiveDistanceKm(double activeDistanceKm) {
        this.activeDistanceKm = activeDistanceKm;
    }

    public double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public void setMaxSpeedKmh(double maxSpeedKmh) {
        this.maxSpeedKmh = maxSpeedKmh;
    }

    public int getMaxCadence() {
        return maxCadence;
    }

    public void setMaxCadence(int maxCadence) {
        this.maxCadence = maxCadence;
    }

    public int getMaxHeartrate() {
        return maxHeartrate;
    }

    public void setMaxHeartrate(int maxHeartrate) {
        this.maxHeartrate = maxHeartrate;
    }

    public double getSumAltitude() {
        return sumAltitude;
    }

    public void setSumAltitude(double sumAltitude) {
        this.sumAltitude = sumAltitude;
    }

    public double getSumDistanceKm() {
        return sumDistanceKm;
    }

    public void setSumDistanceKm(double sumDistanceKm) {
        this.sumDistanceKm = sumDistanceKm;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getExercise() {
        return exercise;
    }

    public void setExercise(double exercise) {
        this.exercise = exercise;
    }

}
