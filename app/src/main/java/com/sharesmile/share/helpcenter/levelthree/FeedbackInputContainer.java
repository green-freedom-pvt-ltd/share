package com.sharesmile.share.helpcenter.levelthree;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sharesmile.share.core.application.MainApplication.getContext;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public class FeedbackInputContainer {

    @BindView(R.id.tv_prompt_text)
    TextView promptTextView;

    @BindView(R.id.et_user_input)
    EditText inputEditText;

    @BindView(R.id.bt_feedback_submit)
    Button submitButton;

    @BindView(R.id.help_mail_us)
    TextView helpMailUs;


    private View containerView;
    private String promptText;
    private String hintText;
    private SubmitListener listener;

    public FeedbackInputContainer(String promptText, String hintText, View container, SubmitListener listener) {
        this.promptText = promptText;
        this.hintText = hintText;
        this.containerView = container;
        this.listener = listener;
        ButterKnife.bind(this, containerView);
        init();
    }

    private void init(){
        String somethingElsePrompt = "Let us know about your issue. Send feedback or chat with us.";
        //TODO: This is a short term hack which needs to be removed
        if (somethingElsePrompt.equals(promptText)){
            // Remove top margin on container
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) containerView.getLayoutParams();
            lp.topMargin = 0;
            containerView.post(new Runnable() {
                @Override
                public void run() {
                    containerView.setLayoutParams(lp);
                }
            });
        }

        promptTextView.setText(promptText);
        Typeface font = Typeface.createFromAsset(containerView.getContext().getAssets(), "fonts/Montserrat-Light.otf");
        inputEditText.setTypeface(font);
        inputEditText.setHint(hintText);

        setMailStringSpannable(helpMailUs);
    }

    private void setMailStringSpannable(TextView textView) {
        String s = getContext().getString(R.string.help_center_mail_description);
        SpannableString ss = new SpannableString(s);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Utils.launchUri(getContext(), Uri.parse("mailto:contact@impactrun.com"));
                AnalyticsEvent.create(Event.ON_CLICK_CONTACT_US_EMAIL_LINK)
                        .buildAndDispatch();
            }
        },24,45, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setHighlightColor(
                Color.TRANSPARENT); // prevent TextView change background when highlight
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(ss, TextView.BufferType.SPANNABLE);
    }

    public String getUserInput(){
        return inputEditText.getText().toString();
    }

    @OnClick(R.id.bt_feedback_submit)
    public void onFeedbackSubmit(){
        if (TextUtils.isEmpty(getUserInput()) || getUserInput().trim().length() == 0){
            MainApplication.showToast(R.string.please_enter_feedback);
        }else {
            listener.onFeedbackSubmit(getUserInput());
        }
    }

    public interface SubmitListener {
        void onFeedbackSubmit(String feedbackText);
    }

}
