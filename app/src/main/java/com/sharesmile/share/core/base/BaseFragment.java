package com.sharesmile.share.core.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.refer_program.SomethingIsCookingDialog;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by ankitmaheshwari1 on 11/01/16.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    private IFragmentController controller;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.controller = (IFragmentController) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.controller = null;
    }

    public void setToolbarTitle(String title) {
        Logger.d(TAG, "setToolbarTitle: " + title);
        getFragmentController().updateToolBar(title, true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int launchCount = SharedPrefsManager.getInstance().getInt(Constants.PREF_SCREEN_LAUNCH_COUNT_PREFIX + getName());
        SharedPrefsManager.getInstance().setInt(Constants.PREF_SCREEN_LAUNCH_COUNT_PREFIX + getName(), ++launchCount);
    }

    public int getScreenLaunchCount() {
        return SharedPrefsManager.getInstance().getInt(Constants.PREF_SCREEN_LAUNCH_COUNT_PREFIX + getName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public boolean isAttachedToActivity() {
        return controller != null;
    }

    public IFragmentController getFragmentController() {
        return controller;
    }

    /**
     * Override this method if you want your fragment to receive a back press callback.
     *
     * @return true if your fragment consumes the back press event,
     * false if the back press should be handled by the activity. Returns false
     * by default.
     */
    protected boolean handleBackPress() {
        return false;
    }

    /**
     * Ask activity to trigger back button behaviour
     * Child fragment can override it and use it as needed
     *
     * @return true if back behaviour has been honoured, false otherwise
     */
    protected boolean goBack() {
        if (isAttachedToActivity()) {
            getFragmentController().goBack();
            return true;
        }
        return false;
    }

    public String getName() {
        return getClass().getCanonicalName();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.OnReferrerSuccessful onReferrerSuccessful) {
        if (onReferrerSuccessful.referrerDetails != null) {
            if (WorkoutSingleton.getInstance().isWorkoutActive()) {
                SharedPrefsManager.getInstance().setObject(Constants.PREF_SMC_NOTI_FCM_INVITEE_DETAILS, onReferrerSuccessful.referrerDetails);
            } else {
                SomethingIsCookingDialog somethingIsCookingDialog = new SomethingIsCookingDialog(getContext(),
                        Constants.USER_OLD, onReferrerSuccessful.referrerDetails);
                somethingIsCookingDialog.show();
            }
        }
    }

    public interface FragmentInterface {
        public void updateNavigationMenu();
    }

}
