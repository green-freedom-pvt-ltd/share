package fragments

import Models.FaqList
import adapters.FaqAdapter
import android.opengl.Visibility
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sharesmile.share.R
import com.sharesmile.share.core.BaseFragment
import com.sharesmile.share.network.NetworkAsyncCallback
import com.sharesmile.share.network.NetworkDataProvider
import com.sharesmile.share.network.NetworkException
import com.sharesmile.share.utils.Urls
import kotlinx.android.synthetic.main.fragment_faq.view.*


/**
 * Created by Shine on 21/07/16.
 */
class FaqFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater?.inflate(R.layout.fragment_faq, container, false);
        return view;

    }

    private lateinit var mAdapter: FaqAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = FaqAdapter(fragmentController);
        view.recycler_view.layoutManager = LinearLayoutManager(context);
        view.recycler_view.adapter = mAdapter;
        view.recycler_view.setHasFixedSize(true);
        getFaqList();
        fragmentController.updateToolBar(getString(R.string.title_faq), true);

    }

    private fun getFaqList() {

        view?.progress_bar?.visibility = View.VISIBLE;

        NetworkDataProvider.doGetCallAsync(Urls.getFaqUrl(), object : NetworkAsyncCallback<FaqList>() {
            override fun onNetworkFailure(ne: NetworkException?) {
                view?.progress_bar?.visibility = View.GONE;

            }

            override fun onNetworkSuccess(wrapper: FaqList?) {

                view?.progress_bar?.visibility = View.GONE;
                mAdapter.setData(wrapper?.faqList);

            }

        })
    }


}