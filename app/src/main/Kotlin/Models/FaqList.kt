package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.core.UnObfuscable
import java.io.Serializable
import java.util.*

/**
 * Created by Shine on 21/07/16.
 */
class FaqList :UnObfuscable, Serializable {

    @SerializedName("results")
    var faqList: ArrayList<Faq>? = null;

    class Faq : Serializable {
        @SerializedName("question")
        var question: String? = "";

        @SerializedName("answer")
        var answer: String? = "";
    }
}