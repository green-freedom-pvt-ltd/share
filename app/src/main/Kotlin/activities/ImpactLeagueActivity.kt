package activities

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.MenuItem
import base.BaseActivity2
import com.sharesmile.share.R
import com.sharesmile.share.core.Constants
import com.sharesmile.share.core.event.UpdateEvent
import com.sharesmile.share.refer_program.SomethingIsCookingDialog
import fragments.LeagueCodeFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ImpactLeagueActivity : BaseActivity2() {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_impact_league)
        loadInitFragment(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    private fun loadInitFragment(savedInstanceState: Bundle?) {

        if (savedInstanceState == null) {
            addFragment(LeagueCodeFragment.getInstance(), false, null);
        }
    }


    override fun useNavDrawer(): Boolean {
        return false;
    }

    override fun configureToolbar() {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP.or(ActionBar.DISPLAY_SHOW_TITLE);
    }

    override fun setUpNavigationView() {

    }

    override fun getFrameLayoutId(): Int {
        return R.id.activity_impact_league;
    }

    override fun getName(): String {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(onReferrerSuccessful: UpdateEvent.OnReferrerSuccessful) {
        if (onReferrerSuccessful.referrerDetails != null) {
            val somethingIsCookingDialog = SomethingIsCookingDialog(this,
                    Constants.USER_OLD, onReferrerSuccessful.referrerDetails)
            somethingIsCookingDialog.show()
        }
    }


}
