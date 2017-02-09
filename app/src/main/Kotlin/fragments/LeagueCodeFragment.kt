package fragments

import Models.LeagueTeam
import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.BaseFragment2
import com.sharesmile.share.MainApplication
import com.sharesmile.share.R
import com.sharesmile.share.analytics.Analytics
import com.sharesmile.share.core.Constants
import com.sharesmile.share.network.NetworkAsyncCallback
import com.sharesmile.share.network.NetworkDataProvider
import com.sharesmile.share.network.NetworkException
import com.sharesmile.share.utils.*
import kotlinx.android.synthetic.main.fragment_secret_code.view.*
import java.util.*


/**
 * Created by Shine on 17/11/16.
 */
class LeagueCodeFragment : BaseFragment2(), View.OnClickListener {


    companion object {
        public fun getInstance(): LeagueCodeFragment {
            val fragment = LeagueCodeFragment();
            return fragment;
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater?.inflate(R.layout.fragment_secret_code, container, false);
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById(R.id.submit_btn)?.setOnClickListener(this);
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            (R.id.submit_btn) -> {
                submitButtonClicked();
            }
        }
    }

    fun submitButtonClicked() {

        Utils.hideKeyboard(view!!.secret_code, context);
        var code = view!!.secret_code.text.toString();
        if (TextUtils.isEmpty(code)) {
            view!!.code_layout.error = "Enter Secret team code"
            return;
        }
        verifySecretCode(code);

    }

    private fun verifySecretCode(code: String) {
        fragmentListener.showProgressBar()
        val data = ArrayList<NameValuePair>()
        data.add(BasicNameValuePair("user", MainApplication.getInstance().userID.toString()))
        data.add(BasicNameValuePair("team", code))

        NetworkDataProvider.doPostCallAsyncWithFormData(Urls.getLeagueUrl(), data, object : NetworkAsyncCallback<LeagueTeam>() {
            override fun onNetworkFailure(ne: NetworkException?) {
                fragmentListener.showActivityContent();
                invalidCode(ne);
            }

            override fun onNetworkSuccess(leagueTeam: LeagueTeam?) {
                fragmentListener.showActivityContent();
                getTeamDetails(leagueTeam!!, code);
            }
        })
    }

    private fun getTeamDetails(leagueData: LeagueTeam, code: String) {
        var location: ArrayList<String> = ArrayList<String>();
        var department: ArrayList<String> = ArrayList<String>();

        val forEach = leagueData?.companyDetails?.forEach {
            if (it.city != null) {
                location.add(it.city!!)
            }

            if (it.department != null) {
                department.add(it.department!!)
            }
        }

        SharedPrefsManager.getInstance().setInt(Constants.PREF_LEAGUE_TEAM_ID, leagueData.teamCode!!);
        // Setting team code for Analytics
        Analytics.getInstance().setUserImpactLeagueTeamCode(leagueData.teamCode!!);
        activity.setResult(Activity.RESULT_OK);
        fragmentListener.replaceFragment(LeagueRegistrationFragment.getInstance(location, department = department, code = code, banner = leagueData?.banner), false, null);
    }

    private fun invalidCode(error: NetworkException?) {

        if (error?.httpStatusCode == 406) {
            view!!.code_layout.error = "Sorry, the team is already full."

        } else {
            view!!.code_layout.error = "Sorry, that’s not the code."
        }
    }

    override fun screenTitle(): String {
        return getString(R.string.secret_code);
    }
}


