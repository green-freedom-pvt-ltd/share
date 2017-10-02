package fragments

import Models.LeagueTeam
import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import base.BaseFragment2
import com.google.gson.Gson
import com.sharesmile.share.LeaderBoardDataStore
import com.sharesmile.share.MainApplication
import com.sharesmile.share.R
import com.sharesmile.share.network.NetworkAsyncCallback
import com.sharesmile.share.network.NetworkDataProvider
import com.sharesmile.share.network.NetworkException
import com.sharesmile.share.rfac.models.UserDetails
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
        view?.findViewById<Button>(R.id.submit_btn)?.setOnClickListener(this);
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
            return
        }
        verifySecretCode(code);

    }

    private fun verifySecretCode(code: String) {
        fragmentListener.showProgressBar()
        val data = ArrayList<NameValuePair>()
        data.add(BasicNameValuePair("user", MainApplication.getInstance().userID.toString()))
        data.add(BasicNameValuePair("team", code))

        NetworkDataProvider.doPostCallAsyncWithFormData(Urls.getLeagueRegistrationUrl(), data,
                object : NetworkAsyncCallback<LeagueTeam>() {
            override fun onNetworkFailure(ne: NetworkException?) {
                if (fragmentListener != null){
                    fragmentListener.showActivityContent()
                    invalidCode(ne)
                }
            }

            override fun onNetworkSuccess(leagueTeam: LeagueTeam?) {
                if (fragmentListener != null){
                    val gson = Gson()
                    Logger.d("LeagueCodeFragment", "LeagueTeam response: " + gson.toJson(leagueTeam))
                    fragmentListener.showActivityContent()
                    getTeamDetails(leagueTeam!!, code)
                }
            }
        })
    }

    private fun getTeamDetails(leagueData: LeagueTeam, code: String) {
        var location: ArrayList<String> = ArrayList<String>();
        var department: ArrayList<String> = ArrayList<String>();

        val forEach = leagueData?.companyDetails?.forEach {
            if (it.city != null) {
                if (!location.contains(it.city!!)){
                    location.add(it.city!!)
                }
            }

            if (it.department != null) {
                if (!department.contains(it.department!!)){
                    department.add(it.department!!)
                }
            }
        }
        val userDetails: UserDetails = MainApplication.getInstance().getUserDetails()
        if (userDetails.teamId != leagueData.teamCode!!){
            userDetails.teamId = leagueData.teamCode!!
            LeaderBoardDataStore.getInstance().updateMyTeamId(leagueData.teamCode!!)
            MainApplication.getInstance().userDetails = userDetails
        }

        if (leagueData.metaDataRequired == null || leagueData.metaDataRequired!!){
            // Show City Department screen
            fragmentListener.replaceFragment(LeagueRegistrationFragment.getInstance(location, department = department, code = code, banner = leagueData?.banner), false, null)
        }else{
            // Pass success result to MainActivity and exit
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }

    }

    private fun invalidCode(error: NetworkException?) {

        if (error?.httpStatusCode == 406) {
            view!!.code_layout.error = "Sorry, the team is already full."

        } else {
            view!!.code_layout.error = "Sorry, that’s not the code."
        }
    }

    override fun screenTitle(): String {
        return getString(R.string.impact_league)
    }
}


