package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.core.base.UnObfuscable
import java.io.Serializable
import java.util.*


/**
 * Created by Shine on 21/07/16.
 */
class CampaignList : UnObfuscable, Serializable {


    @SerializedName("count")
    var totalCount: Long = 0

    @SerializedName("next")
    var nextUrl: String = ""

    @SerializedName("previous")
    var previousUrl: String = ""

    @SerializedName("results")
    var campaignList: ArrayList<Campaign>? = null;

    class Campaign : Serializable, UnObfuscable {

        //"is_active": false,

        @SerializedName("campaign_id")
        var id: Long? = 0;

        @SerializedName("campaign_image")
        var imageUrl: String? = "";

        @SerializedName("campaign_name")
        var campaignName: String? = "";

        @SerializedName("campaign_title")
        var title: String? = "";

        /*@SerializedName("message_brief")
        var brief: String? = "";*/

        @SerializedName("campaign_description")
        var descritption: String? = "";

        @SerializedName("campaign_date")
        var date: String? = "";

        @SerializedName("partner")
        var partner: String? = "";

        @SerializedName("sponsor")
        var sponsor: String? = "";

        @SerializedName("button_text")
        var buttonText: String? = "Share";

        @SerializedName("campaign_share_template")
        var shareTemplate: String? = "";

        @SerializedName("is_always")
        var isAlways: Boolean? = false;

        @SerializedName("show_on_sign_up")
        var showOnSignUp: Boolean? = false;
    }

}