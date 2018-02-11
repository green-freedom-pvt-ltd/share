package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.core.base.UnObfuscable
import java.io.Serializable

/**
 * Created by Shine on 22/07/16.
 */
class FaqFeedBackRequest(ques: String) : UnObfuscable, Serializable {

    @SerializedName("question")
    lateinit var question: String;
    @SerializedName("user_id")
    var user_id: Int = 0;

    init {
        question = ques
    }

    constructor(ques: String, id: Int) : this(ques) {
        user_id = id;
    }

}
