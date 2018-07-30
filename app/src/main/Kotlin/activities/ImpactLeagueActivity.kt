package activities

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.MenuItem
import base.BaseActivity2
import com.sharesmile.share.R
import fragments.LeagueCodeFragment

class ImpactLeagueActivity : BaseActivity2() {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_impact_league)
        loadInitFragment(savedInstanceState)
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


}
