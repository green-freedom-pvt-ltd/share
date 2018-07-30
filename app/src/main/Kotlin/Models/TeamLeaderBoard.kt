package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.core.base.UnObfuscable
import com.sharesmile.share.core.config.Urls
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem
import com.sharesmile.share.utils.Utils
import java.io.Serializable
import java.util.*

/**
 * Created by Shine on 19/12/16.
 */
class TeamLeaderBoard : UnObfuscable, Serializable {

    @SerializedName("count")
    var count: Long = 0

    @SerializedName("next")
    var nextUrl: String = ""

    @SerializedName("previous")
    var previousUrl: String = ""

    @SerializedName("team_name")
    var teamName: String = ""

    @SerializedName("team_id")
    var teamId: Int? = 0

    @SerializedName("total_distance")
    var totalDistance: Float? = 0f

    @SerializedName("total_amount")
    var totalAmount: Float? = 0f

    @SerializedName("total_members")
    var totalMembers: Int? = 0

    @SerializedName("total_runs")
    var totalRuns: Int? = 0

    @SerializedName("team_logo")
    var teamLogo: String? = ""

    @SerializedName("results")
    var membersList: ArrayList<MemberDetails>? = null;

    class MemberDetails : Serializable {

        @SerializedName("user_id")
        var id: Int = 0

        @SerializedName("ranking")
        var ranking: Int = 0

        @SerializedName("first_name")
        var firstName: String? = null

        @SerializedName("last_name")
        var lastName: String? = null

        @SerializedName("social_thumb")
        var imageUrl: String? = null

        @SerializedName("profile_picture")
        var profilePictureUrl: String? = null

        @SerializedName("gender_user")
        var gender: String? = null

        @SerializedName("distance")
        var distance: Float = 0f

        @SerializedName("amount")
        var amount: Float = 0f

        fun convertToLeaderBoard(): BaseLeaderBoardItem {
            var board = BaseLeaderBoardItem()
            board.id = id.toLong()
            board.name = Utils.dedupName(firstName, lastName)
            if(profilePictureUrl!=null && profilePictureUrl!!.isNotEmpty())
            {
                board.image = Urls.getImpactProfileS3BucketUrl()+profilePictureUrl
            }else {
                board.image = imageUrl
            }
            board.distance = distance
            board.ranking = ranking
            board.amount = amount
            return board
        }

    }



}