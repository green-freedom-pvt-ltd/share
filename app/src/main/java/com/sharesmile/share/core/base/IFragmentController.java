package com.sharesmile.share.core.base;

/**
 * Controller for fragments, it contains methods which invoked by fragments and implemented by Activity holding those fragments
 * Created by ankitmaheshwari1 on 11/01/16.
 */
public interface IFragmentController {

    String TAG = "IFragmentController";

    /*
        Operation codes
        Each operation code denotes a unique operation which is requested by fragment and performed by activity
     */

    int END_RUN_START_COUNTDOWN = 100;

    int START_RUN = 101;

    int START_RUN_TEST = 102;

    int START_MAIN_ACTIVITY = 104;

    int SHOW_MESSAGE_CENTER = 105;
    int SHOW_LEAGUE_ACTIVITY = 106;
    int OPEN_HELP_CENTER = 107;
    int TAKE_FLAGGED_RUN_FEEDBACK = 108;

    int OPEN_MUSIC_PLAYER = 110;
    int TAKE_POST_RUN_SAD_FEEDBACK =111;

    int LOGIN_FOR_RESULT = 112;

    int OPEN_DRAWER = 113;
    int CLOSE_DRAWER = 114;

    /*
        Common methods which can be implemented either by abstract BaseActivity or by solid child activities
     */

    void addFragment(BaseFragment fragment, boolean addToBackStack);

    void replaceFragment(BaseFragment fragment, boolean addToBackStack);

    void replaceFragment(BaseFragment fragment, boolean addToBackStack,String tag);

    int getFrameLayoutId();

    String getName();

    void performOperation(int operationId, Object input);

    void exit();

    void goBack();

    void requestPermission(int requestCode, PermissionCallback permissionsCallback);

    void unregisterForPermissionRequest(int requestCode);

    void setToolbarTitle(String toolbarTitle);

    void updateToolBar(String title,boolean showAsUpEnable);

    void showToolbar();

    void hideToolbar();

    void setToolbarElevation(float dpValue);

    boolean isDrawerOpened();

    boolean isDrawerVisible();


}
