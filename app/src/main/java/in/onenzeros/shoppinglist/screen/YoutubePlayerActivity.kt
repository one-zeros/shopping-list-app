package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.utils.DeveloperKey
import android.os.Bundle
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.activity_youtube.*

internal class YoutubePlayerActivity : YouTubeBaseActivity() {

    private var onInitializedListener: YouTubePlayer.OnInitializedListener? = null
    val videoId = "8lfitdnMaEs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube)
        onInitializedListener = object : YouTubePlayer.OnInitializedListener {

            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider,
                youTubePlayer: YouTubePlayer,
                wasRestored: Boolean) {
                if (!wasRestored) {
                    youTubePlayer.cueVideo( videoId)
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
                    youTubePlayer.play()
                }
                youTubePlayer.loadVideo(videoId) // where video.id is a String of a Youtube video ID

            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider,
                youTubeInitializationResult: YouTubeInitializationResult) {
            }
        }
        toolbar?.setNavigationOnClickListener {  finish() }
        youtube_player?.initialize(DeveloperKey.DEVELOPER_KEY, onInitializedListener)
    }
}