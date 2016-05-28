package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotalCollection;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.MainContentActivity;
import com.eaglesakura.andriders.ui.navigation.log.gpx.GpxTourFragmentMain;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentPager;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnMenuClick;
import com.eaglesakura.android.rx.RxTask;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewPager;

import icepick.State;

/**
 * ログ表示画面のメインFragment
 */
public class UserLogFragmentMain extends BaseNavigationFragment implements UserLogFragmentParent {

    @NonNull
    final SupportFragmentPager mPager = new SupportFragmentPager(R.id.UserActivity_Main_Pager);

    SessionTotalCollection mSessionTotalCollection;

    @Bind(R.id.UserActivity_Main_Pager)
    ViewPager mViewPager;

    @State
    int mCurrentPage;

    public UserLogFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_user_log_main);
        mFragmentDelegate.setOptionMenuId(R.menu.fragment_userlog_import);
        mPager.addFragment(SupportFragmentPager.newFragmentCreator(UserLogSynthesisFragment.class));
        mPager.addFragment(SupportFragmentPager.newFragmentCreator(UserLogDailyFragment.class));
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        mViewPager.setAdapter(mPager.newAdapter(getChildFragmentManager()));
        mViewPager.setCurrentItem(mCurrentPage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mCurrentPage = mViewPager.getCurrentItem();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSessionTotal();
    }

    @UiThread
    void loadSessionTotal() {
        mSessionTotalCollection = null;
        asyncUI((RxTask<SessionTotalCollection> task) -> {
            SessionLogDatabase db = new SessionLogDatabase(getActivity());
            try {
                db.openReadOnly();
                return db.loadTotal(SessionTotalCollection.Order.Desc);
            } finally {
                db.close();
            }
        }).completed((result, task) -> {
            mSessionTotalCollection = result;
        }).start();
    }

    @Nullable
    @Override
    public SessionTotalCollection getUserLogCollection() {
        return mSessionTotalCollection;
    }

    @OnMenuClick(R.id.UserLog_Import_GPX)
    public void clickImportGpx() {
        nextNavigation(new GpxTourFragmentMain(), MainContentActivity.NAVIGATION_FLAG_BACKSTACK);
    }

    public static UserLogFragmentMain newInstance(Context context) {
        return new UserLogFragmentMain();
    }
}