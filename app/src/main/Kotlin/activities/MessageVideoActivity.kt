package activities


import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.gson.Gson
import com.sharesmile.share.Message
import com.sharesmile.share.R
import kotlinx.android.synthetic.main.activity_message_video.*

class MessageVideoActivity : YouTubeBaseActivity() ,View.OnClickListener{



    private lateinit var message: Message

    companion object {
        val BUNDLE_MESSAGE_OBJECT = "bundle_message_object"
    }

    @BindView(R.id.video_player)
    lateinit var mPlayer: YouTubePlayerView;

    @BindView(R.id.close)
    public  var mClose :ImageView?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message = Gson().fromJson<Message>(intent.getStringExtra(BUNDLE_MESSAGE_OBJECT), Message::class.java)

        setContentView(R.layout.activity_message_video)
        ButterKnife.bind(this@MessageVideoActivity);
        mPlayer = findViewById(R.id.video_player) as YouTubePlayerView;

        val video = message.getVideoId()

        if (!TextUtils.isEmpty(video)) {
            mPlayer.initialize(getString(R.string.google_app_id), object : YouTubePlayer.OnInitializedListener {
                override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, b: Boolean) {
                    youTubePlayer.loadVideo(message.videoId)
                    youTubePlayer.play()
                }

                override fun onInitializationFailure(provider: YouTubePlayer.Provider, youTubeInitializationResult: YouTubeInitializationResult) {
                    Toast.makeText(this@MessageVideoActivity, "failed", Toast.LENGTH_SHORT).show()
                }
            })
        }

        mClose =findViewById(R.id.close) as ImageView
        mClose?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.id){
            (R.id.close) -> {
                finish();
            }
        }
    }
}
