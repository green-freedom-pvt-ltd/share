package activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sharesmile.share.R
import fragments.LeagueCodeFragment

class LeagueLeaderBoard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_league_leader_board)
        loadInitFragment(savedInstanceState);
    }

    private fun loadInitFragment(savedInstanceState: Bundle?) {

        if (savedInstanceState == null) {
         //   addFragment(LeagueCodeFragment.getInstance(), false, null);
        }
    }
}
