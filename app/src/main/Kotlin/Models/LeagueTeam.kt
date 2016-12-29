package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.core.UnObfuscable
import java.io.Serializable
import java.util.*

/**
 * Created by Shine on 21/07/16.
 */
class LeagueTeam : UnObfuscable, Serializable {

    @SerializedName("id")
    var id: Int? = null;

    @SerializedName("user")
    var user: Int? = null;

    @SerializedName("company")
    var company: Int? = null;

    @SerializedName("team")
    var team: String? = null;

    @SerializedName("city")
    var city: String? = null;

    @SerializedName("department")
    var department: String? = null;

    @SerializedName("impactleague_banner")
    var banner:String?=null;


    @SerializedName("company_attribute")
    var companyDetails: ArrayList<Company>? = null;

    class Company : UnObfuscable, Serializable {

        @SerializedName("id")
        var id: Int? = null;

        @SerializedName("company")
        var company: Int? = null;

        @SerializedName("city")
        var city: String? = null;

        @SerializedName("department")
        var department: String? = null;
    }
}