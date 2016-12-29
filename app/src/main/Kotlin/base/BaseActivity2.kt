package base

import com.nuvoex.library.LumiereBaseActivity

/**
 * Created by Shine on 17/11/16.
 */

abstract class BaseActivity2 :LumiereBaseActivity(){

    override fun useToolbar(): Boolean {
        return true;
    }

    override fun useNavDrawer(): Boolean {
        return super.useNavDrawer()
    }


}