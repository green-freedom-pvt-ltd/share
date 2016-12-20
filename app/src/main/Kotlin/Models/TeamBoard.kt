package Models

import com.google.gson.annotations.SerializedName
import com.sharesmile.share.LeaderBoard
import com.sharesmile.share.core.UnObfuscable
import org.json.JSONObject
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

    @SerializedName("results")
    var teamList: ArrayList<Team>? = null;

   public class Team : Serializable {

        /* "": 1,
         "impactleague": "Genpact Impact League",
         "team_name": "Registered Team",
         "team_code": "R3455FG",
         "total_amount": {
             "total_amount": null
         },
         "total_distance": {
             "total_distance": null
         },
         "team_captain": "Piyush Nagle",
         "team_captain_email_id": "piyushiit.cse@gmail.com"
     },*/
        @SerializedName("id")
        var id: Long? = 0;

        @SerializedName("team_name")
        var teamName: String? = "";

        /*@SerializedName("total_amount")
        var totalAmount: JSONObject? = null;
*/
        public fun convertToLeaderBoard() : LeaderBoard {

            var board= LeaderBoard();
            board.id=id;
            board.first_name=teamName;
           // board.last_week_distance=totalAmount?.getDouble("total_amount") as Float ;
            return  board;
        }
    }

}