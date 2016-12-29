package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari1 on 11/01/16.
 */
public interface IFragmentController {

    String TAG = "IFragmentController";

    int END_RUN_START_COUNTDOWN = 100;

    int START_RUN = 101;

    int START_RUN_TEST = 102;

    int SAY_THANK_YOU = 103;

    int START_MAIN_ACTIVITY = 104;

    int SHOW_MESSAGE_CENTER = 105;
    int SHOW_LEAGUE_ACTIVITY = 106;

    void addFragment(BaseFragment fragment, boolean addToBackStack);

    void replaceFragment(BaseFragment fragment, boolean addToBackStack);

    void loadInitialFragment();

    int getFrameLayoutId();

    String getName();

    void performOperation(int operationId, Object input);

    void exit();

    void requestPermission(int requestCode, PermissionCallback permissionsCallback);

    void unregisterForPermissionRequest(int requestCode);

    void updateToolBar(String title,boolean showAsUpEnable);


}
