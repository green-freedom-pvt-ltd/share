package Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ankitmaheshwari on 1/19/18.
 */

public class FeedLatestArticleResponse {

    @SerializedName("creation_time")
    private long creationEpochSecs;

    public long getCreationEpochSecs() {
        return creationEpochSecs;
    }
}
