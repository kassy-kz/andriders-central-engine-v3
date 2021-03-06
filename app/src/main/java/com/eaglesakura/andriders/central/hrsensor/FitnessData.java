package com.eaglesakura.andriders.central.hrsensor;

import com.eaglesakura.andriders.central.base.BaseCalculator;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.sensor.HeartrateZone;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.serialize.RawSessionData;
import com.eaglesakura.andriders.serialize.RawSpecs;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.util.Timer;

import android.support.annotation.NonNull;

/**
 * 運動データの計算を行う
 */
public class FitnessData extends BaseCalculator {
    /**
     * 現在のMETs値
     */
    private float mCurrentMets;

    /**
     * 合計カロリー
     */
    private float mSumCalories;

    /**
     * 合計エクササイズ
     */
    private float mSumExercise;

    /**
     * 現在の心拍値
     */
    private float mHeartrate;

    /**
     * 心拍の更新時刻
     */
    private long mHeartrateDataTime;

    @NonNull
    private AppSettings mSettings;

    public FitnessData(@NonNull Clock clock, @NonNull AppSettings settings) {
        super(clock);
        mSettings = settings;
    }

    /**
     * データが有効であればtrue
     */
    public boolean valid() {
        return (now() - mHeartrateDataTime) < DATA_TIMEOUT_MS;
    }

    public float getCurrentMets() {
        return mCurrentMets;
    }

    @NonNull
    public AppSettings getSettings() {
        return mSettings;
    }

    public float getUserWeight() {
        return (float) getSettings().getUserProfiles().getUserWeight();
    }

    public int getMaxHeartrate() {
        return getSettings().getUserProfiles().getMaxHeartrate();
    }

    public int getNormalHeartrate() {
        return getSettings().getUserProfiles().getNormalHeartrate();
    }

    /**
     * 現在の心拍を取得する
     */
    public float getHeartrate() {
        return mHeartrate;
    }

    public float getSumCalories() {
        return mSumCalories;
    }

    public float getSumExercise() {
        return mSumExercise;
    }

    public long getHeartrateDataTime() {
        return mHeartrateDataTime;
    }

    /**
     * 現在の心拍ゾーンを取得する
     */
    public HeartrateZone getZone() {
        final double userMaxHeartrate = (double) getMaxHeartrate();

        if (mHeartrate < (userMaxHeartrate * 0.5)) {
            // 安静
            return HeartrateZone.Repose;
        } else if (mHeartrate < (userMaxHeartrate * 0.6)) {
            // イージー
            return HeartrateZone.Easy;
        } else if (mHeartrate < (userMaxHeartrate * 0.7)) {
            // 脂肪燃焼
            return HeartrateZone.FatCombustion;
        } else if (mHeartrate < (userMaxHeartrate * 0.8)) {
            // 有酸素
            return HeartrateZone.PossessionOxygenMotion;
        } else if (mHeartrate < (userMaxHeartrate * 0.9)) {
            // 無酸素
            return HeartrateZone.NonOxygenatedMotion;
        } else {
            // オーバーワーク
            return HeartrateZone.Overwork;
        }
    }

    /**
     * 心拍を更新する
     *
     * @param bpm 心拍
     */
    public void setHeartrate(int bpm) {
        mHeartrateDataTime = now();
        mHeartrate = bpm;
    }


    /**
     * フィットネス情報を取得する
     */
    public void getSpec(RawSpecs.RawFitnessSpec dst) {
        dst.weight = getUserWeight();
        dst.heartrateMax = (short) getMaxHeartrate();
        dst.heartrateNormal = (short) getNormalHeartrate();
    }

    /**
     * フィットネス情報を取得する
     */
    public void getFitness(RawSessionData.RawFitnessStatus dst) {
        dst.calorie = getSumCalories();
        dst.exercise = getSumExercise();
        dst.mets = getCurrentMets();
    }

    /**
     * センサー情報を取得する
     *
     * @return センサー情報を書き込んだ場合true
     */
    public boolean getSensor(RawSensorData dstSensor) {
        if (!valid()) {
            return false;
        }

        dstSensor.heartrate = new RawSensorData.RawHeartrate();
        dstSensor.heartrate.bpm = (short) getHeartrate();
        dstSensor.heartrate.date = mHeartrateDataTime;
        dstSensor.heartrate.zone = getZone();
        return true;
    }

    /**
     * データの更新を行う
     *
     * 最後にsetされた心拍が継続しているものと判断する。
     *
     * @param diffTimeMs 前回からの差分時間
     */
    public void onUpdateTime(long diffTimeMs) {
        final int normalHeartrate = getNormalHeartrate();
        final int maxHeartrate = getMaxHeartrate();

        // METsを計算
        if (mHeartrate <= normalHeartrate || normalHeartrate <= 0 || maxHeartrate <= 0 || normalHeartrate >= maxHeartrate) {
            // data error
            mCurrentMets = 1;
        } else {
            final float mets = ((mHeartrate - normalHeartrate) / (float) (maxHeartrate - normalHeartrate) * 10.0f);
            mCurrentMets = Math.max(mets, 1.0f);
        }

        // 消費カロリー計算
        {
            // 消費カロリー = METs x 時間(h) x 体重(kg) x 1.05
            // MEMO 先に体重をかけておくことで、精度誤差をマシにする
            final double diffTimeHour = Timer.msToHour(diffTimeMs);
            final double diffCalories = mCurrentMets * getUserWeight() * 1.05 * diffTimeHour;

            mSumCalories += diffCalories;
        }

        // 獲得エクササイズ値計算
        // 厚労省データでは3METs以上が活発な値となる
        // http://www.mhlw.go.jp/bunya/kenkou/undou01/pdf/data.pdf
        if (mCurrentMets >= 3) {
            // 獲得エクササイズ値
            double diffExercise = ((mCurrentMets * diffTimeMs) / 1000.0 / 60.0 / 60.0);
            mSumExercise += diffExercise;
        }
    }
}
