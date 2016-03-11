package com.eaglesakura.andriders.display;

import com.eaglesakura.andriders.dao.display.DbDisplayLayout;
import com.eaglesakura.andriders.dao.display.DbDisplayTarget;
import com.eaglesakura.andriders.db.display.DisplayLayoutDatabase;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.ExtensionInformation;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * サイコン表示窓のスロットを管理する
 */
public class DisplaySlotManager {
    /**
     * 横方向の最大スロット数
     */
    static final int MAX_HORIZONTAL_SLOTS = 2;

    /**
     * 縦方向の最大スロット数
     */
    static final int MAX_VERTICAL_SLOTS = 7;

    /**
     * 対応しているスロット一覧
     */
    private final Map<Integer, DisplaySlot> slots = new HashMap<>();

    private final Context context;

    private final String mAppPackageName;

    private DbDisplayTarget displayTarget;

    public enum Mode {
        /**
         * 読み込みモードとして使用する
         */
        ReadOnly,

        /**
         * 編集モードとして利用する
         */
        Edit,
    }

    Mode mMode;

    public DisplaySlotManager(Context context, String packageName, Mode mode) {
        this.context = context.getApplicationContext();
        this.mAppPackageName = packageName;
        this.mMode = mode;

        for (int x = 0; x < MAX_HORIZONTAL_SLOTS; ++x) {
            for (int y = 0; y < MAX_VERTICAL_SLOTS; ++y) {
                DisplaySlot slot = new DisplaySlot(x, y);
                slots.put(Integer.valueOf(slot.getId()), slot);
            }
        }
    }

    public String getAppPackageName() {
        return displayTarget != null ? displayTarget.getTargetPackage() : mAppPackageName;
    }

    public void load() {
        DisplayLayoutDatabase db = new DisplayLayoutDatabase(context);
        try {
            db.openWritable();
            if (mMode == Mode.ReadOnly) {
                displayTarget = db.loadTarget(mAppPackageName);
            } else {
                displayTarget = db.loadTargetOrCreate(mAppPackageName);
            }
            List<DbDisplayLayout> layouts = db.listLayouts(displayTarget);

            for (DbDisplayLayout layout : layouts) {
                DisplaySlot slot = slots.get(layout.getSlotId());
                if (slot == null) {
                    // スロットが欠けたので互換性のためデータを削除する
                    db.remove(layout);
                } else {
                    // スロットに表示内容をバインドする
                    slot.setValueLink(layout);
                }
            }
        } finally {
            db.close();
        }
    }

    /**
     * 表示内容を指定する
     *
     * @param slot      設定対象のスロット
     * @param extension 拡張機能
     * @param display   拡張内容
     */
    public void setLayout(DisplaySlot slot, ExtensionInformation extension, DisplayInformation display) {
        slot.setValueLink(displayTarget, extension, display);
    }

    /**
     * 表示内容を削除する
     */
    public void removeLayout(DisplaySlot slot) {
        slot.setValueLink(null);
    }

    /**
     * データを上書き保存する
     */
    public void commit() {
        DisplayLayoutDatabase db = new DisplayLayoutDatabase(context);
        try {
            db.openWritable();
            for (DisplaySlot slot : listSlots()) {
                DbDisplayLayout displayLayout = slot.getLink();
                if (displayLayout == null) {
                    // 表示内容が削除されたのでDBからも削除する
                    db.remove(displayTarget, slot.getId());
                } else {
                    // 表示内容を更新する
                    db.update(displayLayout);
                }
            }
        } finally {
            db.close();
        }
    }

    /**
     * 表示のXY位置からスロットを取得する
     */
    public DisplaySlot getSlot(int x, int y) {
        return slots.get(Integer.valueOf(DisplaySlot.getSlotId(x, y)));
    }

    /**
     * スロット一覧を取得する。
     */
    public List<DisplaySlot> listSlots() {
        return new ArrayList<>(slots.values());
    }

    /**
     * 表示用の仮組みレイアウトを作成する
     *
     * 2x7の格子状のレイアウトを作成し、ViewIdはSlotIdと同じに設定される。
     */
    public static ViewGroup newStubLayout(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        for (int y = 0; y < MAX_VERTICAL_SLOTS; ++y) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int x = 0; x < MAX_HORIZONTAL_SLOTS; ++x) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1.0f;

                FrameLayout stub = new FrameLayout(context);
                stub.setId(DisplaySlot.getSlotId(x, y));
//                stub.setBackgroundColor(Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255)));
                row.addView(stub, params);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            root.addView(row, params);
        }

        return root;
    }
}
