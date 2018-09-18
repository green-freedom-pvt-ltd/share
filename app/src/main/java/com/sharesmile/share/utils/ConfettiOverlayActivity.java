package com.sharesmile.share.utils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sharesmile.share.R;
import com.sharesmile.share.core.event.UpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.dionsegijn.konfetti.KonfettiView;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

public class ConfettiOverlayActivity extends AppCompatActivity {

    @BindView(R.id.view_konfetti)
    KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        getWindow().addFlags(FLAG_NOT_TOUCHABLE);
        setContentView(R.layout.activity_confetti_overlay);
        ButterKnife.bind(this);
        Utils.startKonfetti(konfettiView, getResources());
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.OnKonfettiFinish onKonfettiFinish) {
        finish();
    }
}
