package com.eaglesakura.andriders.service.central.display;

import com.eaglesakura.andriders.display.data.DataDisplayManager;
import com.eaglesakura.andriders.display.data.DataLayoutManager;
import com.eaglesakura.andriders.display.data.DataViewBinder;
import com.eaglesakura.andriders.display.data.LayoutSlot;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginConnector;
import com.eaglesakura.andriders.plugin.PluginManager;
import com.eaglesakura.andriders.service.central.CentralContext;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.Util;

import android.app.Service;
import android.view.View;
import android.view.ViewGroup;

/**
 * Serviceで表示するサイコン情報を管理するマネージャ
 */
public class DisplayRenderer {
    final Service mService;

    /**
     * ディスプレイの表示更新間隔
     */
    final int DISPLAY_REFRESH_SEC = 1;

    final CentralContext mCentralContext;

    HandlerLoopController mLoopController;

    /**
     * ディスプレイのスロット管理
     */
    DataLayoutManager mDisplayLayoutManager;

    /**
     * 表示対象のStub格納先
     */
    ViewGroup mDisplayStub;

    /**
     * 現在表示中のアプリパッケージ名
     */
    String mCurrentAppPackageName;

    DataViewBinder mDataViewBinder;

    public DisplayRenderer(Service service, CentralContext centralContext) {
        mService = service;
        mDataViewBinder = new DataViewBinder(service, Clock.getRealtimeClock());
        mCentralContext = centralContext;
    }

    /**
     * 表示対象のViewを作成する
     */
    public void setDisplayStub(ViewGroup displayStub) {
        if (displayStub.getChildCount() != 0) {
            throw new IllegalArgumentException();
        }
        mDisplayStub = displayStub;
    }


    /**
     * スロット情報のリロードを行う
     */
    public void reloadSlots(final String appPackageName) {
        AndroidThreadUtil.assertBackgroundThread();

        if (mDisplayLayoutManager != null) {
            if (Util.equals(appPackageName, mCurrentAppPackageName)) {
                // 同じアプリ名なので何もしなくて良い
                return;
            }
        }

        DataLayoutManager layoutManager = new DataLayoutManager(mService);
        layoutManager.load(DataLayoutManager.Mode.ReadOnly, appPackageName);
        mDisplayLayoutManager = layoutManager;
        mCurrentAppPackageName = appPackageName;
    }

    /**
     * 拡張機能のディスプレイ機能とUIを結びつける
     */
    private void bindExtensionDisplay() {
        if (mDisplayStub == null) {
            // 表示対象が無い
            return;
        }

        if (mDisplayStub.getChildCount() == 0) {
            ViewGroup root = DataLayoutManager.newStubLayout(mService);
            mDisplayStub.addView(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        PluginManager extensionClientManager = mCentralContext.getPluginManager();
        DataDisplayManager displayManager = mCentralContext.getDisplayManager();

        for (LayoutSlot slot : mDisplayLayoutManager.listSlots()) {
            ViewGroup viewSlot = (ViewGroup) mDisplayStub.findViewById(slot.getId());
            DisplayKey information = null;
            if (slot.hasLink()) {
                // 値にリンクされている場合、インフォメーションを取得する
                information = extensionClientManager.findDisplayInformation(slot.getExtensionId(), slot.getDisplayValueId());
            }

            if (information == null) {
                // 表示内容が無いので何もしない
                viewSlot.setVisibility(View.INVISIBLE);
                continue;
            } else {
                PluginConnector client = extensionClientManager.findClient(slot.getExtensionId());
                // ディスプレイに対して表示内容を更新させる
                displayManager.bind(client, information, mDataViewBinder, viewSlot);
            }
        }
    }

    /**
     * 処理を開始する
     */
    public void connect() {
        mLoopController = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                onDisplayRefresh();
            }
        };
        mLoopController.setFrameRate(DISPLAY_REFRESH_SEC);
        mLoopController.connect();

        mCentralContext.newTask(SubscribeTarget.Pipeline, task -> {
            reloadSlots(mService.getPackageName());
            return this;
        }).start();
    }

    /**
     * メモリを開放する
     */
    public void dispose() {
        mLoopController.disconnect();
        mLoopController.dispose();
        mLoopController = null;

        // 子のViewを全て開放する
        if (mDisplayStub != null) {
            mDisplayStub.removeAllViews();
            mDisplayStub = null;
        }
    }

    /**
     * 通知のレンダリングを行う
     */
    private void onDisplayRefresh() {
        PluginManager extensionClientManager = mCentralContext.getPluginManager();

        if (mDisplayLayoutManager == null || !extensionClientManager.isConnected()) {
            return;
        }
        // 毎フレーム更新をかける
        bindExtensionDisplay();
    }
}
