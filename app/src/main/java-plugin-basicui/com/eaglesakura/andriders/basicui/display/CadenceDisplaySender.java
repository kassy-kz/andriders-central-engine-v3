package com.eaglesakura.andriders.basicui.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.display.ZoneColor;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.display.BasicValue;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.MargarineKnife;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * ケイデンス更新を行う
 */
public class CadenceDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "DISPLAY_ID_CADENCE";

    @NonNull
    private final ZoneColor mZoneColor;

    @BindStringArray(R.array.Display_Cadence_ZoneName)
    @NonNull
    String[] mZoneTitles;

    public CadenceDisplaySender(@NonNull CentralEngineConnection connection, @NonNull ZoneColor zoneColor) {
        super(connection);
        mZoneColor = zoneColor;

        MargarineKnife.bind(this, this);
    }

    public CadenceDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mCadenceHandler);
        }
        return this;
    }

    private SensorDataReceiver.CadenceHandler mCadenceHandler = new SensorDataReceiver.CadenceHandler() {
        @Override
        public void onReceived(@NonNull RawCentralData master, @NonNull RawSensorData.RawCadence sensor) {
            Context context = getContext();

            DisplayData data = new DisplayData(context, DISPLAY_ID);

            BasicValue value = new BasicValue();
            value.setTitle(context.getString(R.string.Display_Common_Cadence));
            value.setValue(String.valueOf(sensor.rpm));
            value.setBarColorARGB(mZoneColor.getColor(sensor.zone));
            value.setZoneText(mZoneTitles[sensor.zone.ordinal()]);

            data.setValue(value);
            mSession.getDisplayExtension().setValue(data);
        }

        @Override
        public void onDisconnectedSensor(@NonNull RawCentralData master) {
            Context context = getContext();

            DisplayData data = new DisplayData(context, DISPLAY_ID);
            data.setTimeoutMs(1000 * 60);

            BasicValue value = new BasicValue();
            value.setTitle(context.getString(R.string.Display_Common_Cadence));
            value.setValue(context.getString(R.string.Display_Common_Reconnect));
            value.setBarColorARGB(0x00);

            data.setValue(value);
            mSession.getDisplayExtension().setValue(data);
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Display_Common_Cadence));
        return result;
    }
}
