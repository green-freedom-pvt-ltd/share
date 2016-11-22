package fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import base.BaseFragment2
import com.sharesmile.share.R
import kotlinx.android.synthetic.main.fragment_league_registration.*

/**
 * Created by Shine on 17/11/16.
 */
class LeagueRegistrationFragment : BaseFragment2(), View.OnClickListener {


    companion object {
        public fun getInstance(): LeagueRegistrationFragment {
            val fragment = LeagueRegistrationFragment();
            return fragment;
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater?.inflate(R.layout.fragment_league_registration, container, false);
        return view;
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }


    override fun screenTitle(): String {
        return getString(R.string.team);
    }
}