package fragments

import adapters.MessageCenterAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sharesmile.share.*
import com.sharesmile.share.Events.DBEvent
import com.sharesmile.share.core.BaseFragment
import com.sharesmile.share.core.Constants
import com.sharesmile.share.rfac.fragments.MessageInfoFragment
import com.sharesmile.share.sync.SyncHelper
import com.sharesmile.share.utils.SharedPrefsManager
import com.sharesmile.share.utils.Utils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_message_center.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Shine on 26/08/16.
 */
class MessageCenterFragment : BaseFragment(), MessageCenterAdapter.MessageInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        var view = inflater?.inflate(R.layout.fragment_message_center, container, false);
        mAdapter = MessageCenterAdapter(this);
        EventBus.getDefault().register(this)
        return view!!;
    }

    private lateinit var mAdapter: MessageCenterAdapter;

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(context);
        recycler_view.adapter = mAdapter;
        recycler_view.setHasFixedSize(true);
        fragmentController.updateToolBar(getString(R.string.title_messages), true);
        SyncHelper.syncMessageCenterData(context)
        progress_bar.visibility = View.VISIBLE;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(runDataUpdated: DBEvent.MessageDataUpdated) {
        fetchMessageDataFromDb()
    }

    fun fetchMessageDataFromDb() {
        val messageDao = MainApplication.getInstance().dbWrapper.daoSession.messageDao
        var messageList = messageDao.queryBuilder().orderDesc(MessageDao.Properties.Id).list()
        mAdapter.setData(messageList)
        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_UNREAD_MESSAGE, false)
        if (messageList.size > 0) {
            progress_bar.visibility = View.GONE;

        }
    }

    override fun onShareMessageClick(message: Message) {
        progress_bar.visibility = View.VISIBLE
        Picasso.with(context).load(message.message_image).into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                progress_bar.visibility = View.GONE

                Utils.share(context, Utils.getLocalBitmapUri(bitmap, context), message.shareTemplate);
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                Utils.share(context, null, message.shareTemplate);
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
        })
    }

    override fun onMessageCardClick(message: Message) {
        fragmentController.replaceFragment(MessageInfoFragment.getInstance(message), true);
    }

    /* private fun share(uri: Uri?, shareTemplate: String?) {
         progress_bar.visibility = View.GONE
         val shareIntent = Intent()
         shareIntent.setAction(Intent.ACTION_SEND)
         shareIntent.putExtra(Intent.EXTRA_TEXT, shareTemplate)
         shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
         shareIntent.setType("image*//*")
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(shareIntent, "send"))
    }


    fun getLocalBitmapUri(bmp: Bitmap?, context: Context): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png")
            val out = FileOutputStream(file)
            bmp?.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }*/
}