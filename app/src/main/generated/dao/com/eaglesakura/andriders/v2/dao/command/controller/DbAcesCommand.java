package com.eaglesakura.andriders.v2.dao.command.controller;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

/**
 * Entity mapped to table DB_ACES_COMMAND.
 */
public class DbAcesCommand {

    /** Not-null value. */
    private String commandKey;
    /** Not-null value. */
    private String packageName;
    /** Not-null value. */
    private byte[] iconPng;
    /** Not-null value. */
    private String appExtraKey;
    private byte[] intentData;
    private Double timerIntervalMin;
    private Boolean timerSyncRealTime;
    private Double distanceIntervalKm;
    private Integer distanceIntervalType;
    private Integer distanceCommandLoopNum;
    private Integer speedType;
    private Double speedKmh;

    public DbAcesCommand() {
    }

    public DbAcesCommand(String commandKey) {
        this.commandKey = commandKey;
    }

    public DbAcesCommand(String commandKey, String packageName, byte[] iconPng, String appExtraKey, byte[] intentData, Double timerIntervalMin, Boolean timerSyncRealTime, Double distanceIntervalKm, Integer distanceIntervalType, Integer distanceCommandLoopNum, Integer speedType, Double speedKmh) {
        this.commandKey = commandKey;
        this.packageName = packageName;
        this.iconPng = iconPng;
        this.appExtraKey = appExtraKey;
        this.intentData = intentData;
        this.timerIntervalMin = timerIntervalMin;
        this.timerSyncRealTime = timerSyncRealTime;
        this.distanceIntervalKm = distanceIntervalKm;
        this.distanceIntervalType = distanceIntervalType;
        this.distanceCommandLoopNum = distanceCommandLoopNum;
        this.speedType = speedType;
        this.speedKmh = speedKmh;
    }

    /** Not-null value. */
    public String getCommandKey() {
        return commandKey;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    /** Not-null value. */
    public String getPackageName() {
        return packageName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /** Not-null value. */
    public byte[] getIconPng() {
        return iconPng;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setIconPng(byte[] iconPng) {
        this.iconPng = iconPng;
    }

    /** Not-null value. */
    public String getAppExtraKey() {
        return appExtraKey;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAppExtraKey(String appExtraKey) {
        this.appExtraKey = appExtraKey;
    }

    public byte[] getIntentData() {
        return intentData;
    }

    public void setIntentData(byte[] intentData) {
        this.intentData = intentData;
    }

    public Double getTimerIntervalMin() {
        return timerIntervalMin;
    }

    public void setTimerIntervalMin(Double timerIntervalMin) {
        this.timerIntervalMin = timerIntervalMin;
    }

    public Boolean getTimerSyncRealTime() {
        return timerSyncRealTime;
    }

    public void setTimerSyncRealTime(Boolean timerSyncRealTime) {
        this.timerSyncRealTime = timerSyncRealTime;
    }

    public Double getDistanceIntervalKm() {
        return distanceIntervalKm;
    }

    public void setDistanceIntervalKm(Double distanceIntervalKm) {
        this.distanceIntervalKm = distanceIntervalKm;
    }

    public Integer getDistanceIntervalType() {
        return distanceIntervalType;
    }

    public void setDistanceIntervalType(Integer distanceIntervalType) {
        this.distanceIntervalType = distanceIntervalType;
    }

    public Integer getDistanceCommandLoopNum() {
        return distanceCommandLoopNum;
    }

    public void setDistanceCommandLoopNum(Integer distanceCommandLoopNum) {
        this.distanceCommandLoopNum = distanceCommandLoopNum;
    }

    public Integer getSpeedType() {
        return speedType;
    }

    public void setSpeedType(Integer speedType) {
        this.speedType = speedType;
    }

    public Double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(Double speedKmh) {
        this.speedKmh = speedKmh;
    }

}