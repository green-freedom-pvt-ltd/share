package com.sharesmile.share.profile;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class ViewProfilePicture extends BaseFragment {

    @BindView(R.id.profile_pic)
    ImageView imageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext())
                    .inflateTransition(android.R.transition.move));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_view_profile_picture, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

}
