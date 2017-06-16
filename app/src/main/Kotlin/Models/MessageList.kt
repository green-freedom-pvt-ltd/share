package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.MainApplication
import com.sharesmile.share.Message
import com.sharesmile.share.core.UnObfuscable
import com.squareup.picasso.Picasso
import java.io.Serializable
import java.util.*


/**
 * Created by Shine on 21/07/16.
 */
class MessageList : UnObfuscable, Serializable, Iterable<Message> {


    @SerializedName("count")
    var totalMessageCount: Long = 0

    @SerializedName("next")
    var nextUrl: String = ""

    @SerializedName("previous")
    var previousUrl: String = ""

    @SerializedName("results")
    var messageList: ArrayList<Message>? = null;

    class Message : Serializable {

        @SerializedName("message_center_id")
        var id: Long? = 0;

        @SerializedName("message_image")
        var imageUrl: String? = "";

        @SerializedName("message_title")
        var title: String? = "";

        @SerializedName("message_brief")
        var brief: String? = "";

        @SerializedName("message_description")
        var descritption: String? = "";

        @SerializedName("message_date")
        var messageDate: String? = "";


        @SerializedName("message_share_template")
        var shareTemplate: String? = "";

        @SerializedName("message_video")
        var videoId: String? = "";
    }


    override fun iterator(): Iterator<com.sharesmile.share.Message> {
        return ArrayListIterator();
    }


    inner class ArrayListIterator : Iterator<com.sharesmile.share.Message> {
        var remaining = messageList?.size;
        var removalIndex = -1;

        override fun hasNext(): Boolean {
            return remaining != 0
        }

        override fun next(): com.sharesmile.share.Message {
            val rem = remaining
            if (rem == 0) {
                throw NoSuchElementException()
            }
            remaining = rem?.minus(1)
            removalIndex = messageList?.size!!.minus(rem!!)
            val message = messageList?.get(remaining!!.toInt())
            return getMessageData(message!!)
        }

    }

    private fun getMessageData(data: Message): com.sharesmile.share.Message {

        val message = com.sharesmile.share.Message(data.id)
        message.is_read = false
        message.message_image = data.imageUrl
        message.message_title = data.title
        message.message_description = data.descritption
        message.message_date = data.messageDate
        message.shareTemplate = data.shareTemplate
        message.messageBrief=data.brief
        message.videoId=data.videoId

        return message
    }
}