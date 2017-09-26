package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.core.UnObfuscable
import com.sharesmile.share.rfac.models.BaseLeaderBoardItem
import java.io.Serializable
import java.util.*

/**
 * Created by Shine on 19/12/16.
 */
class LeagueBoard : UnObfuscable, Serializable {

    @SerializedName("count")
    var totalMessageCount: Long = 0

    @SerializedName("next")
    var nextUrl: String = ""

    @SerializedName("previous")
    var previousUrl: String = ""

    @SerializedName("impactleague_is_active")
    var isLeagueActive: Boolean = false

    @SerializedName("impactleague_name")
    var leagueName: String = ""

    @SerializedName("impactleague_start_date")
    var leagueStartDateEpoch: Long = 0

    @SerializedName("impactleague_end_date")
    var leagueEndDateEpoch: Long = 0

    @SerializedName("duration")
    var durationInDays: Int = 0

    @SerializedName("impactleague_banner")
    var leagueBanner: String = ""

    @SerializedName("impactleague_logo")
    var leagueLogo: String = ""

    @SerializedName("total_members")
    var totalMembers: Int = 0

    @SerializedName("total_runs")
    var totalRuns: Int = 0

    @SerializedName("total_distance")
    var totalDistance: Float = 0f

    @SerializedName("total_amount")
    var totalImpact: Float = 0f

    @SerializedName("show_team_logos")
    var showTeamLogos: Boolean = false

    @SerializedName("results")
    var teamList: ArrayList<Team>? = null

    class Team : Serializable {

        @SerializedName("team_id")
        var id: Long? = 0

        @SerializedName("team_name")
        var teamName: String? = ""

        @SerializedName("team_captain")
        var teamCaptain: String? = ""

        @SerializedName("team_captain_email_id")
        var teamCaptainEmailId: String? = ""

        @SerializedName("team_logo")
        var teamLogo: String? = ""

        @SerializedName("distance")
        private var distance: Float = 0f

        @SerializedName("amount")
        private var amount: Float = 0f

        @SerializedName("ranking")
        private var ranking: Int = 0

        fun convertToLeaderBoard(): BaseLeaderBoardItem {
            var board = BaseLeaderBoardItem()
            board.id = id as Long
            board.name = teamName
            board.image = teamLogo
            board.distance = distance
            board.ranking = ranking
            board.amount = amount
            return board
        }
    }

}