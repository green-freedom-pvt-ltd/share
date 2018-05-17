package com.sharesmile.share.profile.badges;

import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.home.homescreen.OnboardingOverlay;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.profile.EditProfileFragment;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;
import com.sharesmile.share.profile.history.ProfileHistoryFragment;
import com.sharesmile.share.profile.stats.BarChartDataSet;
import com.sharesmile.share.profile.stats.BarChartEntry;
import com.sharesmile.share.profile.streak.StreakFragment;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import Models.Level;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_RUN;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;
import static com.sharesmile.share.core.Constants.PROFILE_SCREEN;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class AchieviedBadgeFragment extends BaseFragment {

    private static final String TAG = "AchieviedBadgeFragment";
    AchievedBadgesData achievedBadgesData;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_badge, null);
        ButterKnife.bind(this, v);
        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        achievedBadgesData = bundle.getParcelable(Constants.BUNDLE_CAUSE_DATA);
        initUi();
    }

    private void initUi() {

    }

}
