package fragments

import activities.MessageVideoActivity
import adapters.MessageCenterAdapter
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.sharesmile.share.core.event.UpdateEvent
import com.sharesmile.share.core.application.MainApplication
import com.sharesmile.share.Message
import com.sharesmile.share.MessageDao
import com.sharesmile.share.R
import com.sharesmile.share.core.base.BaseFragment
import com.sharesmile.share.core.Constants
import com.sharesmile.share.feed.FeedDetailFragment
import com.sharesmile.share.core.sync.SyncHelper
import com.sharesmile.share.core.SharedPrefsManager
import com.sharesmile.share.utils.Utils
import kotlinx.android.synthetic.main.fragment_message_center.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FeedListFragment : BaseFragment(), MessageCenterAdapter.MessageInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        var view = inflater?.inflate(R.layout.fragment_message_center, container, false);
        mAdapter = MessageCenterAdapter(this);
        EventBus.getDefault().register(this)
        return view!!;
    }

    private lateinit var mAdapter: MessageCenterAdapter;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(context);
        recycler_view.adapter = mAdapter;
        recycler_view.setHasFixedSize(true);
        fragmentController.updateToolBar(getString(R.string.title_messages), true);
        // Rolling back to old feed
        SyncHelper.syncMessageCenterData()
        fetchMessageDataFromDb()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(runDataUpdated: UpdateEvent.MessageDataUpdated) {
        fetchMessageDataFromDb()
    }

    fun fetchMessageDataFromDb() {
        val messageDao = MainApplication.getInstance().dbWrapper.daoSession.messageDao
        var messageList = messageDao.queryBuilder().orderDesc(MessageDao.Properties.Id).list()
        mAdapter.setData(messageList)
        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_UNREAD_MESSAGE, false)
        if (messageList.size > 0) {
            progress_bar.visibility = View.GONE
        }
    }

    override fun onShareMessageClick(message: Message) {
        if (!TextUtils.isEmpty(message.videoId!!)){
            // To share a video
            Utils.share(context, message.shareTemplate)
        }else{
            // To share an image
            Utils.shareImageWithMessage(context, message.message_image, message.shareTemplate)
        }
    }

    override fun onMessageCardClick(message: Message) {
        if (TextUtils.isEmpty(message.videoId)) {
            fragmentController.replaceFragment(FeedDetailFragment.getInstance(message), true);
        } else {
            var intent = Intent(activity, MessageVideoActivity::class.java)
            intent.putExtra(MessageVideoActivity.BUNDLE_MESSAGE_OBJECT, Gson().toJson(message))
            startActivity(intent)
        }
    }

}