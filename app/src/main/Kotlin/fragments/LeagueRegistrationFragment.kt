package fragments

import Models.LeagueTeam
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import base.BaseFragment2
import com.sharesmile.share.MainApplication
import com.sharesmile.share.R
import com.sharesmile.share.core.Constants
import com.sharesmile.share.network.NetworkAsyncCallback
import com.sharesmile.share.network.NetworkDataProvider
import com.sharesmile.share.network.NetworkException
import com.sharesmile.share.utils.BasicNameValuePair
import com.sharesmile.share.utils.NameValuePair
import com.sharesmile.share.utils.SharedPrefsManager
import com.sharesmile.share.utils.Urls
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_league_registration.view.*
import java.util.*

/**
 * Created by Shine on 17/11/16.
 */
class LeagueRegistrationFragment : BaseFragment2(), View.OnClickListener {


    companion object {
        const private val BUNDLE_DEPARTMENT_ARRAY: String = "bundle_department_array";
        const private val BUNDLE_LOCATION_ARRAY: String = "bundle_location_array";
        const private val BUNDLE_LEAGUE_CODE: String = "bundle_secret_code";
        const private val BUNDLE_LEAGUE_BANNER: String = "bundle_secret_banner";

        fun getInstance(location: ArrayList<String>, department: ArrayList<String>, code: String, banner: String?): LeagueRegistrationFragment {
            val fragment = LeagueRegistrationFragment();
            var bundle = Bundle();
            bundle.putStringArrayList(BUNDLE_LOCATION_ARRAY, location);
            bundle.putStringArrayList(BUNDLE_DEPARTMENT_ARRAY, department);
            bundle.putString(BUNDLE_LEAGUE_CODE, code);
            bundle.putString(BUNDLE_LEAGUE_BANNER, banner);
            fragment.arguments = bundle;
            return fragment;
        }
    }

    private lateinit var mDepartmentArray: ArrayList<String>;
    private lateinit var mLocationArray: ArrayList<String>;
    private var mCode: String? = "";
    private var mDepartmentAdapter: ArrayAdapter<String>? = null;
    private var mLocationAdapter: ArrayAdapter<String>? = null;
    private var mSelectedDepartment: String = "";
    private var mSelectedLocation: String = "";
    private var mBanner: String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLocationArray = arguments.getStringArrayList(BUNDLE_LOCATION_ARRAY);
        mDepartmentArray = arguments.getStringArrayList(BUNDLE_DEPARTMENT_ARRAY);
        mCode = arguments.getString(BUNDLE_LEAGUE_CODE);
        mBanner = arguments.getString(BUNDLE_LEAGUE_BANNER);
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater?.inflate(R.layout.fragment_league_registration, container, false);
        return view;
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDepartmentAdapter = ArrayAdapter(context, R.layout.spinner_item, R.id.text1, mDepartmentArray)
        mLocationAdapter = ArrayAdapter(context, R.layout.spinner_item, R.id.text1, mLocationArray)
        view!!.department.adapter = mDepartmentAdapter;
        view!!.location.adapter = mLocationAdapter;

        view!!.department.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSelectedDepartment = mDepartmentArray[position];
            }
        }

        view!!.location.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSelectedLocation = mLocationArray[position];
            }
        }

        view!!.findViewById(R.id.submit).setOnClickListener(this);
        Picasso.with(context).load(mBanner).into(view!!.league_image);
    }

    private fun onSubmit() {
        fragmentListener.showProgressBar()
        val data = ArrayList<NameValuePair>()
        data.add(BasicNameValuePair("user", MainApplication.getInstance().userID.toString()))
        data.add(BasicNameValuePair("team", mCode))
        data.add(BasicNameValuePair("city", mSelectedLocation))
        data.add(BasicNameValuePair("department", mSelectedDepartment))

        NetworkDataProvider.doPutCallAsyncWithForData(Urls.getLeagueUrl(), data, object : NetworkAsyncCallback<LeagueTeam>() {
            override fun onNetworkFailure(ne: NetworkException?) {
                fragmentListener.showActivityContent();
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }

            override fun onNetworkSuccess(leagueTeam: LeagueTeam?) {
                fragmentListener.showActivityContent();
                SharedPrefsManager.getInstance().setString(Constants.PREF_LEAGUE_TEAM_CODE, leagueTeam?.team);
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            }
        })
    }

    override fun onClick(v: View?) {
        onSubmit();
    }

    override fun screenTitle(): String {
        return getString(R.string.team);
    }
}