package com.andriders.group;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kashimoto on 16/07/31.
 */
public class RiderGroup {

    public static final String GROUP_NAME_PREFIX = "AndridersGroup_";
    private static final String TAG = "RiderGroup";
    private static final String KEY_NAME_DATE = "date";
    private static final String KEY_NAME_LAT = "lat";
    private static final String KEY_NAME_LNG = "lng";
    private static final String KEY_NAME_CADENCE = "cadence";
    private static final String KEY_NAME_HEARTRATE = "heartrate";
    private static final String KEY_NAME_MESSAGE = "message";
    private static final String KEY_NAME_SPEED = "speed";
    private static final String KEY_NAME_NAME = "name";
    private static final String KEY_NAME_PHOTO_URL = "photo";

    /**
     * 所属グループは一つしかありえないと設定し、シングルトンで実装
     */
    private static RiderGroup sSelf = null;
    private final FirebaseUser mUser;
    private DatabaseReference mGroupRef;
    private RiderGroupListener mListener;
    private String mGroupName = null;
    private HashMap<String, RiderInfo> mRiderInfoMap;

    /**
     * 変更があったコールバック
     */
    public interface RiderGroupListener {
        public void onChanged(String uid, RiderInfo info);
        public void onMemberAdded(String uid, RiderInfo info);
        public void onMemberRemoved(String uid, RiderInfo info);
    }

    /**
     * //TODO シングルトンで初回以外はuserを受け取る気が皆無なのに、引数で設定せなあかんあたりイケてない
     * @param user
     * @return
     */
    public static RiderGroup getInstance(@NonNull FirebaseUser user) {
        // 既にあるインスタンスは全て破棄。あくまで所属は１グループ限定とするため
        if (sSelf != null) {
            sSelf = null;
        }
        sSelf = new RiderGroup(user);
        return sSelf;
    }

    /**
     *
     * @param groupName
     * @param user
     * @return
     */
    public static RiderGroup getInstance(@NonNull String groupName, @NonNull FirebaseUser user) {
        // 既にあるインスタンスは全て破棄。あくまで所属は１グループ限定とするため
        if (sSelf != null) {
            sSelf = null;
        }
        sSelf = new RiderGroup(groupName, user);
        return sSelf;
    }

    /**
     * 自分のライダー情報を更新（してグループメンバーに伝える）
     * @param info
     */
    public void setRiderInfo(@NonNull RiderInfo info) {
        // float型はdouble型にキャストしないとFirebaseが落ちるので注意
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(KEY_NAME_DATE, info.getDate());
        childUpdates.put(KEY_NAME_LAT, info.getLat());
        childUpdates.put(KEY_NAME_LNG, info.getLng());
        childUpdates.put(KEY_NAME_CADENCE, (double)info.getCadence());
        childUpdates.put(KEY_NAME_HEARTRATE, (double)info.getHeartRate());
        childUpdates.put(KEY_NAME_SPEED, (double)info.getSpeed());
        childUpdates.put(KEY_NAME_NAME, info.getName());
        childUpdates.put(KEY_NAME_MESSAGE, info.getMessage());
        childUpdates.put(KEY_NAME_PHOTO_URL, info.getPhotoUrl());
        mGroupRef.child(mUser.getUid()).updateChildren(childUpdates);
    }

    /**
     * コンストラクタ 自分でグループをつくる場合はこちらを使用
     * @param user
     */
    private RiderGroup(@NonNull FirebaseUser user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // ここでFirebase上にgroupを作成
        mGroupRef = database.getReference().child(decideGroupName(user));
        mUser = user;
        mRiderInfoMap = new HashMap<String, RiderInfo>();
    }

    /**
     * コンストラクタ 他人のグループに入る場合はこちらを使用
     */
    private RiderGroup(@NonNull String groupName, @NonNull FirebaseUser user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mGroupRef = database.getReference().child(groupName);
        mUser = user;
        mRiderInfoMap = new HashMap<String, RiderInfo>();
    }

    /**
     * グループの名前を決める関数、実装はまだ適当
     * QRコードで検索するときの識別子として、"AndridersGroup_" で始める
     * TODO ユーザーIDをそのままつかってるがハッシュ化くらいするべきか？
     * @param user
     * @return
     */
    private String decideGroupName(FirebaseUser user) {
        mGroupName = GROUP_NAME_PREFIX + user.getUid();
        Log.i(TAG, "group name : " + mGroupName);
        return mGroupName;
    }

    /**
     * リスナーをセット、他の人のデータが変更された際の通知を開始する
     * @param listener
     */
    public void startListening(RiderGroupListener listener) {
        mListener = listener;
        mGroupRef.addValueEventListener(mValueEventListener);
        mGroupRef.addChildEventListener(mChildEventListener);
    }

    /**
     * 通知を終了する
     */
    public void stopListening() {
        if (mValueEventListener != null) {
            mGroupRef.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    /**
     * グループ名を取得する
     * @return
     */
    public String getGroupName() {
        return mGroupName;
    }

    /**
     * グループを抜ける
     */
    public void exitGroup() {
        mGroupRef.child(mUser.getUid()).removeValue();
        stopListening();
    }

    public HashMap<String, RiderInfo> getRiderInfoMap() {
        return mRiderInfoMap;
    }

    /**
     * Firebaseのリアルタイムデータベースのコールバック
     */
    private ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i(TAG, "onDataChange " + dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    /**
     *
     */
    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "child added " + dataSnapshot);
            RiderInfo remoteRiderInfo = getRiderInfo(dataSnapshot);
            mRiderInfoMap.put(remoteRiderInfo.getUid(), remoteRiderInfo);

            if (mListener != null) {
                mListener.onMemberAdded(remoteRiderInfo.getUid(), remoteRiderInfo);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "child changed " + dataSnapshot);

            RiderInfo remoteRiderInfo = getRiderInfo(dataSnapshot);
            mRiderInfoMap.put(remoteRiderInfo.getUid(), remoteRiderInfo);

            for (Map.Entry<String, RiderInfo> entry : mRiderInfoMap.entrySet()) {
                Log.i(TAG, "  id : " + entry.getKey() + " name : " + entry.getValue().getName());
            }

            if (mListener != null) {
                mListener.onChanged(remoteRiderInfo.getUid(), remoteRiderInfo);
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i(TAG, "child removed " + dataSnapshot);
            RiderInfo remoteRiderInfo = getRiderInfo(dataSnapshot);
            mRiderInfoMap.remove(remoteRiderInfo.getUid());

            if (mListener != null) {
                mListener.onMemberRemoved(remoteRiderInfo.getUid(), remoteRiderInfo);
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    /**
     * DataSnapshotからRiderInfoを取り出す
     * @param data
     * @return
     */
    @NonNull
    private RiderInfo getRiderInfo(DataSnapshot data) {
        String uid = data.getKey();
        String name = data.child(KEY_NAME_NAME).getValue(String.class);
        String photoUrl = data.child(KEY_NAME_PHOTO_URL).getValue(String.class);
        String message  = data.child(KEY_NAME_MESSAGE).getValue(String.class);

        long date = data.hasChild(KEY_NAME_DATE) ? data.child(KEY_NAME_DATE).getValue(Long.class) : 0;
        double lat = data.hasChild(KEY_NAME_LAT) ? data.child(KEY_NAME_LAT).getValue(Double.class) : 0.d;
        double lng = data.hasChild(KEY_NAME_LNG) ? data.child(KEY_NAME_LNG).getValue(Double.class) : 0.d;
        float cadence   = data.hasChild(KEY_NAME_CADENCE) ? data.child(KEY_NAME_CADENCE).getValue(Float.class) : 0.f;
        float speed     = data.hasChild(KEY_NAME_SPEED) ? data.child(KEY_NAME_SPEED).getValue(Float.class) : 0.f;
        float heartrate = data.hasChild(KEY_NAME_HEARTRATE) ? data.child(KEY_NAME_HEARTRATE).getValue(Float.class) : 0.f;
        Log.i(TAG, "   name : " + name + ", id : " + uid + ", date : " + date);
        return new RiderInfo(date, name, photoUrl, uid, lat, lng, cadence, heartrate, speed, message);
    }
}
