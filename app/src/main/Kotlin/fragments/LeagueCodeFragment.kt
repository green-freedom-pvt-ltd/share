package fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import base.BaseFragment2
import com.sharesmile.share.R
import kotlinx.android.synthetic.main.fragment_secret_code.*

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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById(R.id.submit_btn)?.setOnClickListener(this);

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            (R.id.submit_btn) -> {
                //   fragmentListener.replaceFragment()
                submitButtonClicked();
            }
        }
    }

    fun submitButtonClicked() {
        Toast.makeText(activity, "Great ", Toast.LENGTH_SHORT).show();

        var code = secret_code.text.toString();
        if (!TextUtils.isEmpty(code)) {
            code_layout.error = "Enter Secret code"
            return;
        }

        verifySecretCode(code);

    }

    private fun verifySecretCode(code: String) {

        getTeamDetails();
        // invalidCode();
    }

    private fun getTeamDetails() {
        fragmentListener.replaceFragment(LeagueRegistrationFragment.getInstance(), true, null);
    }

    private fun invalidCode() {
        code_layout.error = "Sorry, that’s not the code."
    }

    override fun screenTitle(): String {
        return getString(R.string.team);
    }
}