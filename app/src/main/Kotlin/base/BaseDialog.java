package base;

import android.content.Context;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;

/**
 * Created by ankitmaheshwari on 6/26/17.
 */

public abstract class BaseDialog extends AppCompatDialog {

    private static final String TAG = "BaseDialog";

    protected Listener listener;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public BaseDialog(Context context) {
        super(context);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
    }

    protected BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public interface Listener
    {
        void onPrimaryClick(BaseDialog dialog);
        void onSecondaryClick(BaseDialog dialog);
    }

}
