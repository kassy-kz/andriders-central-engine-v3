package com.eaglesakura.andriders.computer.central;

import com.eaglesakura.andriders.computer.CycleComputerManager;
import com.eaglesakura.andriders.computer.central.calculator.AltitudeDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.DistanceDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.FitnessDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.SpeedDataCalculator;
import com.eaglesakura.andriders.computer.central.geo.LocationCentral;
import com.eaglesakura.andriders.computer.central.sensor.CadenceDataCentral;
import com.eaglesakura.andriders.computer.central.sensor.HeartrateDataCentral;
import com.eaglesakura.andriders.computer.central.sensor.SensorDataCentral;
import com.eaglesakura.andriders.computer.central.sensor.SpeedDataCentral;
import com.eaglesakura.andriders.idl.remote.IdlHeartrate;
import com.eaglesakura.andriders.idl.remote.IdlLocation;
import com.eaglesakura.andriders.idl.remote.IdlSpeedAndCadence;
import com.eaglesakura.andriders.protocol.SensorProtocol;
import com.eaglesakura.util.LogUtil;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * サイクルコンピュータのデータを中央集積する
 */
public class CentralDataManager extends CycleComputerManager {
    /**
     * 受け取ったデータが無効となるデフォルト時刻
     */
    public static final long DATA_TIMEOUT_MS = 1000 * 30;

    final List<ICentral> mCentrals = new ArrayList<>();

    /**
     * 登録されたセンサー情報一覧
     */
    final Map<SensorProtocol.SensorType, SensorDataCentral> mSensorDatas = new HashMap<>();

    /**
     * ロケーション情報
     */
    final LocationCentral mLocationCentral;

    final FitnessDataCalculator mFitnessDataCalculator = new FitnessDataCalculator();

    final SpeedDataCalculator mSpeedDataCalculator = new SpeedDataCalculator();

    public CentralDataManager(Context context) {
        super(context);

        // センサー用マネージャを追加
        {
            mSensorDatas.put(SensorProtocol.SensorType.HeartrateMonitor, new HeartrateDataCentral(mFitnessDataCalculator));
            mSensorDatas.put(SensorProtocol.SensorType.SpeedSensor, new SpeedDataCentral(mSpeedDataCalculator));
            mSensorDatas.put(SensorProtocol.SensorType.CadenceSensor, new CadenceDataCentral());
            mCentrals.addAll(mSensorDatas.values());
        }

        // 位置情報を追加
        {
            mLocationCentral = new LocationCentral();
            mCentrals.add(mLocationCentral);
        }

        // 初期化タスクを投げておく
        initialize();
    }

    /**
     * 初期ロードを行う
     */
    private void initialize() {
    }

    /**
     * 定時処理を行う
     * <p/>
     * 処理はパイプラインに流される
     */
    @Override
    public void updateInPipeline(double deltaTimeSec) {
        update(mCentrals);
    }

    /**
     * GPS座標を更新する
     */
    public void setLocation(final IdlLocation loc) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                // GPS座標を更新する
                mLocationCentral.setLocation(loc);

                // GPS由来の速度を更新する
                DistanceDataCalculator distanceDataCalculator = mLocationCentral.getDistanceDataCalculator();
                ((SpeedDataCentral) mSensorDatas.get(SensorProtocol.SensorType.SpeedSensor))
                        .setGpsSensorSpeed(distanceDataCalculator.getGeoSpeedKmh());

                LogUtil.log("GPS lat(%f) lng(%f) alt(%f) acc(%f) spd(%.1f km/h)",
                        loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), loc.getAccuracyMeter(),
                        distanceDataCalculator.getGeoSpeedKmh()
                );
            }
        });
    }

    /**
     * Speed&Cadenceセンサーの情報を更新する
     */
    public void setSpeedAndCadence(final IdlSpeedAndCadence sc) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                SpeedDataCentral speedDataCentral = ((SpeedDataCentral) mSensorDatas.get(SensorProtocol.SensorType.SpeedSensor));
                CadenceDataCentral cadenceDataCentral = ((CadenceDataCentral) mSensorDatas.get(SensorProtocol.SensorType.CadenceSensor));
                // ケイデンス設定を行う
                cadenceDataCentral.setCadence(sc.getCrankRpm(), sc.getCrankRevolution());

                // S&Cセンサー由来の速度更新を行う
                speedDataCentral.setBleSensorSpeed(sc.getWheelRpm(), sc.getWheelRevolution());
            }
        });
    }

    /**
     * 心拍を更新する
     */
    public void setHeartrate(final IdlHeartrate heartrate) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                ((HeartrateDataCentral) mSensorDatas.get(SensorProtocol.SensorType.HeartrateMonitor))
                        .setHeartrate(heartrate.getBpm());
            }
        });
    }

    /**
     * セッション開始
     */
    public void startSession() {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                // 位置ステートをリセットする
                mLocationCentral.setAltitudeDataCalculator(new AltitudeDataCalculator());
                mLocationCentral.setDistanceDataCalculator(new DistanceDataCalculator());
            }
        });
    }

    /**
     * セッション終了
     */
    public void finishSession() {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    /**
     * 不要になったデータを削除する
     */
    <T extends ICentral> void update(Iterable<T> datas) {
        synchronized (datas) {
            Iterator<T> iterator = datas.iterator();
            while (iterator.hasNext()) {
                T data = iterator.next();
                if (data.isDelete(this)) {
                    iterator.remove();
                } else {
                    data.onUpdate(this);
                }
            }
        }
    }

    /**
     * サイコン情報として登録されたデータ
     */
    public interface ICentral {
        void onUpdate(CentralDataManager parent);

        boolean isDelete(CentralDataManager parent);
    }

}