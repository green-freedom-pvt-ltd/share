package adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sharesmile.share.core.application.MainApplication.getContext
import com.sharesmile.share.Message
import com.sharesmile.share.R
import com.sharesmile.share.analytics.events.AnalyticsEvent
import com.sharesmile.share.analytics.events.Event
import com.sharesmile.share.utils.DateUtil
import com.sharesmile.share.core.ShareImageLoader
import kotlinx.android.synthetic.main.item_message_center.view.*
import java.util.*

/**
 * Created by Shine on 26/08/16.
 */
class MessageCenterAdapter(listener: MessageInterface) : RecyclerView.Adapter<MessageCenterAdapter.MessageHolder>() {
    private var mListerner: MessageInterface

    init {
        mListerner = listener
    }

    private var mList: List<Message> = ArrayList<Message>();

    fun setData(messageList: List<Message>) {
        mList = messageList;
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder?.bindData(mList.get(position));
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        return MessageHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_message_center, parent, false));
    }

    override fun getItemCount(): Int {
        return mList.size;
    }

    inner class MessageHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        public fun bindData(data: Message) {

            itemView.date.text = DateUtil.getCustomFormattedDate(DateUtil.getFormattedDate(data.message_date, DateUtil.DEFAULT_DATE_FORMAT_DATE_ONLY), DateUtil.USER_FORMAT_DATE_DATE_ONLY);
            itemView.description.text = data.messageBrief

            ShareImageLoader.getInstance().loadImage(data.message_image, itemView.message_image,
                    ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder))

            if (!TextUtils.isEmpty(data.videoId)) {
                itemView.play_icon.visibility = View.VISIBLE
            } else {
                itemView.play_icon.visibility = View.GONE
            }
            itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    mListerner.onMessageCardClick(data);
                    AnalyticsEvent.create(Event.ON_CLICK_FEED_CARD)
                            .put("feed_card_id", data.id)
                            .buildAndDispatch()
                }

            })
            itemView.share.setOnClickListener(object : View.OnClickListener {

                override fun onClick(v: View?) {
                    mListerner.onShareMessageClick(data)
                    AnalyticsEvent.create(Event.ON_CLICK_FEED_CARD_SHARE)
                            .put("feed_card_id", data.id)
                            .buildAndDispatch()
                }
            })
        }

    }

    interface MessageInterface {
        fun onShareMessageClick(message: Message);
        fun onMessageCardClick(message: Message);

    }
}