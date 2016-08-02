package com.andriders.group;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import com.eaglesakura.andriders.R;

public class GroupManageActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, RiderGroup.RiderGroupListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGNIN = 1;
    private static final int REQUEST_CODE_SEARCH_GROUP = 2;

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private RiderGroup mRiderGroup = null;
    private ImageView mImageGroupName;
    private TextView mTxtGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerGpsLogger);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        // auth
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // ui
        mImageGroupName = (ImageView) findViewById(R.id.imgGroupName);
        findViewById(R.id.btnCreateGroup).setOnClickListener(this);
        findViewById(R.id.btnJoinGroup).setOnClickListener(this);
        findViewById(R.id.btnExitGroup).setOnClickListener(this);
        mTxtGroupName = (TextView)findViewById(R.id.txtGroupName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, REQUEST_CODE_SIGNIN);
            return;
        }
    }

    /**
     * onClick
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreateGroup: {
                // グループを作成
                mRiderGroup = RiderGroup.newInstance(mUser);
                mAdapter = new RecyclerAdapter();
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);

                // QRコードを取得
                String url = "http://chart.apis.google.com/chart?cht=qr&chs=150x150&chl=" + mRiderGroup.getGroupName();
                Glide.with(this).load(url).into(mImageGroupName);

                // 表示切り替え
                mTxtGroupName.setText(mRiderGroup.getGroupName());
                changeViewMode(true);

                // 自分の情報をセットしておく
                RiderInfo info = new RiderInfo(System.currentTimeMillis(), mUser.getDisplayName(), mUser.getPhotoUrl(), mUser.getUid(), 35.123d, 136.123d, 90.f, 64.f, 32.f, "welcome");
                mRiderGroup.setRiderInfo(info);

                // リスナーセット
                mRiderGroup.startListening(this);
                break;
            }
            case R.id.btnJoinGroup: {
                Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SEARCH_GROUP);
                break;
            }
            case R.id.btnExitGroup: {
                // グループ脱退
                mRiderGroup.exitGroup();
                mRiderGroup = null;
                mRecyclerView.setVisibility(View.GONE);
                // 表示切り替え
                changeViewMode(false);
            }
        }
    }

    /**
     * 表示状態切り替え
     * @param hasGroup グループに属する状態はtrue、そうでなければfalse
     */
    private void changeViewMode(boolean hasGroup) {
        if (hasGroup) {
            findViewById(R.id.layoutCreateGroup).setVisibility(View.GONE);
            findViewById(R.id.layoutShowGroup).setVisibility(View.VISIBLE);
        } else {
            mTxtGroupName.setText("");
            findViewById(R.id.layoutCreateGroup).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutShowGroup).setVisibility(View.GONE);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onChanged(String uid, RiderInfo info) {

    }

    /**
     * メンバーが新たに参加したとき
     * @param uid
     * @param info
     */
    @Override
    public void onMemberAdded(String uid, RiderInfo info) {
        Log.i(TAG, "onMemberAdded : " + mRiderGroup.getRiderInfoMap().size());
//        mAdapter.notifyItemInserted(mRiderGroup.getRiderInfoMap().size()-1);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * メンバーが離脱したとき
     * TODO notifyItemRemovedを使いたいが...どうしたものか...
     * @param uid
     * @param info
     */
    @Override
    public void onMemberRemoved(String uid, RiderInfo info) {
        mAdapter.notifyDataSetChanged();
    }


    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        public RecyclerAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.txtGroupMember);
                mImageView = (ImageView) v.findViewById(R.id.imgGroupMember);
            }
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group_member, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ArrayList<RiderInfo> infoList = new ArrayList<RiderInfo>(mRiderGroup.getRiderInfoMap().values());
            for (RiderInfo info : infoList) {
                Log.i(TAG, "  recycler pos : " + position + " : " + info.getName());
            }
            holder.mTextView.setText(infoList.get(position).getName());
            Glide.with(GroupManageActivity.this).load(infoList.get(position).getPhotoUrl()).into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
//            return mLogDataList.size();
            return mRiderGroup.getRiderInfoMap().values().size();
        }
    }

    /**
     * 認証を終えて帰ってきた
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SIGNIN : {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    Log.i(TAG, "success 1");
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                } else {
                    Log.i(TAG, "failed 1");
                    // Google Sign In failed, update UI appropriately
                    // ...
                }
                break;
            }

            case REQUEST_CODE_SEARCH_GROUP : {
                if (resultCode == RESULT_OK) {
                    mUser = FirebaseAuth.getInstance().getCurrentUser();

                    String groupName = data.getStringExtra(BarcodeCaptureActivity.EXTRA_GROUP_NAME);
                    mRiderGroup = RiderGroup.newInstance(groupName, mUser);
                    mTxtGroupName.setText(groupName);
                    changeViewMode(true);

                    // QRコードを取得
                    String url = "http://chart.apis.google.com/chart?cht=qr&chs=150x150&chl=" + mRiderGroup.getGroupName();
                    Glide.with(this).load(url).into(mImageGroupName);

                    // リスナーセット
                    mRiderGroup.startListening(this);

                    // 自分の最初の情報をセットしておく
                    RiderInfo info = new RiderInfo(System.currentTimeMillis(), mUser.getDisplayName(), mUser.getPhotoUrl(), mUser.getUid(), 35.123d, 136.123d, 90.f, 64.f, 32.f, "hello");
                    mRiderGroup.setRiderInfo(info);

                    // recyclerセット
                    mAdapter = new RecyclerAdapter();
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle : " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(GroupManageActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            // Name, email address, and profile photo Url
                            String name = user.getDisplayName();
                            String email = user.getEmail();
                            Uri photoUrl = user.getPhotoUrl();
                            String uid = user.getUid();
                            mUser = user;
                            Log.i(TAG, "firebase user : " + name + ", " + email + ", " + photoUrl  + ", " + uid);
                        }


                        // ...
                    }
                });
    }

}
