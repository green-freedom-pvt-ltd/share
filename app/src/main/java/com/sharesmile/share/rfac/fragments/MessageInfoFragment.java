package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sharesmile.share.Message;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.MLTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shine on 8/27/2016.
 */
public class MessageInfoFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "MessageInfoFragment";

    public static final String BUNDLE_MESSAGE_OBJECT = "bundle_cause_object";

    @BindView(R.id.run_screen_description)
    MLTextView mDescription;

    @BindView(R.id.image_run)
    ImageView mMessageImage;

    @BindView(R.id.share)
    Button mShareBtn;

    @BindView(R.id.run_screen_title)
    TextView mTitle;

    private Message message;


    public static MessageInfoFragment getInstance(Message message) {

        MessageInfoFragment fragment = new MessageInfoFragment();
        Bundle arg = new Bundle();
        arg.putString(BUNDLE_MESSAGE_OBJECT, new Gson().toJson(message));
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        message = new Gson().fromJson(arg.getString(BUNDLE_MESSAGE_OBJECT), Message.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_info, null);
        ButterKnife.bind(this, view);
        init();
        return view;
    }


    private void init() {
        mDescription.setText(message.getMessage_description());
        mTitle.setText(message.getMessage_title());
        mShareBtn.setOnClickListener(this);
        //load image
        ShareImageLoader.getInstance().loadImage(message.getMessage_image(), mMessageImage,
                ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));
        updateActionbar();
    }

    private void updateActionbar() {
        getFragmentController().updateToolBar(getString(R.string.messages), true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share:
                onShareMessageClick(message);
                AnalyticsEvent.create(Event.ON_CLICK_FEED_CARD_SHARE)
                        .put("feed_card_id", message.getId())
                        .buildAndDispatch();
                break;
        }
    }

    private void onShareMessageClick(final Message message) {
        if (!TextUtils.isEmpty(message.getVideoId())){
            // To share a video
            Utils.share(getContext(), message.getShareTemplate());
        }else{
            // To share an image
            Utils.shareImageWithMessage(getContext(), message.getMessage_image(), message.getShareTemplate());
        }
    }
}

