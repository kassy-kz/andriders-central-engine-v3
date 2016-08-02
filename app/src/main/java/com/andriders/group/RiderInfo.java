package com.andriders.group;

import android.net.Uri;

/**
 * Created by kashimoto on 16/07/31.
 */
public class RiderInfo {
    private String mName;
    private String mPhotoUrl;
    private String mUid;
    private double mLat;
    private double mLng;
    private float mCadence;
    private float mHeartRate;
    private float mSpeed;
    private long mDate;
    private String mMessage;

    // TODO 適当だが、photoUrlがない場合に備えたデフォアイコン
    private static final String DEFAULT_ICON_URL = "https://dl.dropboxusercontent.com/u/32997453/ic_account_circle_black_48dp.png";

    /**
     * コンストラクタその１（photoUrlがUriクラス）
     *
     * @param date
     * @param name
     * @param photoUrl
     * @param uid
     * @param lat
     * @param lng
     * @param cadence
     * @param heartRate
     * @param speed
     * @param message
     */
    public RiderInfo(long date, String name, Uri photoUrl, String uid, double lat, double lng, float cadence, float heartRate, float speed, String message) {
        mDate = date;
        mName = name;
        if (photoUrl != null) {
            mPhotoUrl = photoUrl.toString();
        } else {
            mPhotoUrl = DEFAULT_ICON_URL;
        }
        mUid = uid;
        mLat = lat;
        mLng = lng;
        mCadence = cadence;
        mHeartRate = heartRate;
        mSpeed = speed;
        mMessage = message;
    }

    /**
     * コンストラクタその２，photoUrlがString
     * @param date
     * @param name
     * @param photoUrl
     * @param uid
     * @param lat
     * @param lng
     * @param cadence
     * @param heartRate
     * @param speed
     * @param message
     */
    public RiderInfo(long date, String name, String photoUrl, String uid, double lat, double lng, float cadence, float heartRate, float speed, String message) {
        mDate = date;
        mName = name;
        mPhotoUrl = photoUrl;
        mUid = uid;
        mLat = lat;
        mLng = lng;
        mCadence = cadence;
        mHeartRate = heartRate;
        mSpeed = speed;
        mMessage = message;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        mPhotoUrl = photoUrl;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double lng) {
        mLng = lng;
    }

    public float getCadence() {
        return mCadence;
    }

    public void setCadence(float cadence) {
        mCadence = cadence;
    }

    public float getHeartRate() {
        return mHeartRate;
    }

    public void setHeartRate(float heartRate) {
        mHeartRate = heartRate;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
