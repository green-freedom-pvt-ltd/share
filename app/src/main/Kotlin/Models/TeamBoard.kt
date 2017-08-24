package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.LeaderBoard
import com.sharesmile.share.core.UnObfuscable
import java.io.Serializable
import java.util.*

/**
 * Created by Shine on 19/12/16.
 */
class TeamBoard : UnObfuscable, Serializable {

    @SerializedName("count")
    var totalMessageCount: Long = 0

    @SerializedName("next")
    var nextUrl: String = ""

    @SerializedName("previous")
    var previousUrl: String = ""

    @SerializedName("impactleague_is_active")
    var isLeagueActive: Boolean = false

    @SerializedName("impactleague_start_date")
    var leagueStartDateEpoch: Long = 0

    @SerializedName("duration")
    var durationInDays: Int = 0

    @SerializedName("results")
    var teamList: ArrayList<Team>? = null;

    public class Team : Serializable {

        @SerializedName("id")
        var id: Long? = 0;

        @SerializedName("team_name")
        var teamName: String? = "";

        @SerializedName("impactleague")
        var leagueName: String? = "";

        @SerializedName("team_captain")
        var teamCaptain: String? = "";

        @SerializedName("team_captain_email_id")
        var teamCaptainEmailId: String? = "";

        @SerializedName("total_distance")
        private var total_distance: TotalDistance? = null;

        @SerializedName("impactleague_banner")
        public var banner: String? = null;

        public fun convertToLeaderBoard(): LeaderBoard {
            var board = LeaderBoard();
            board.id = id;
            board.first_name = teamName;
            board.last_week_distance = total_distance?.totalDistance;
            return board;
        }

        private class TotalDistance : Serializable {
            @SerializedName("total_distance")
            public val totalDistance: Float? = 0f;
        }
    }

}