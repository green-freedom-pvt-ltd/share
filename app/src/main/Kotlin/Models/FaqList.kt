package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.core.base.UnObfuscable
import com.sharesmile.share.rfac.models.Qna
import java.io.Serializable
import java.util.*

/**
 * Created by Shine on 21/07/16.
 */
class FaqList : UnObfuscable, Serializable {

    @SerializedName("results")
    var faqList: ArrayList<Qna>? = null;

}