import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import com.example.olright.R

class SoundService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()

        // Start playing your sound
        mediaPlayer = MediaPlayer.create(this, R.raw.loudsound) // Ensure you have a sound file in res/raw
        mediaPlayer.isLooping = false // Set to true if you want it to loop
        mediaPlayer.start()

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}
