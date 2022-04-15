package se.umu.dv20von.one_punch_man_trainer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_start.*
import se.umu.dv20von.one_punch_man_trainer.databinding.ActivityStartBinding

/**
 * The starting activity for the application. Displays some fun pictures to get the user interested.
 *
 * @author Viktor Olofsson dv20von@cs.umu.se
 * @since 2022-03-21
 */
class StartActivity : AppCompatActivity() {
    /* Varibles */
    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: ActivityStartBinding
    private var userFirstTime = 0

    /**
     * Overwritten function that creates the activity and the view.
     *
     * @param savedInstanceState - A Bundle with values for reconstructing a state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.allowEnterTransitionOverlap = true
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val helpImage = findViewById<ImageView>(R.id.help_icon)
        helpImage.setOnClickListener { v ->
            var hp = HelpDialogFragment()
            hp.message = getString(R.string.start_help)
            hp.show(supportFragmentManager, HelpDialogFragment.TAG)
        }

        sharedPref = this?.getSharedPreferences(
            getString(R.string.shared_file_key), Context.MODE_PRIVATE)

        userFirstTime = sharedPref.getInt(getString(R.string.first_time), 0)

        pictureChangeTimer()
    }

    /**
     * Sets the touch event for the screen. Starts a new Activity when user touches the screen.
     *
     * @param event A MotionEvent.
     * @return a Boolean. (Value not used in this app)
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (event.action == MotionEvent.ACTION_UP) {
            if(userFirstTime == 0) {
                with(sharedPref.edit()) {
                    putInt(getString(R.string.first_time), 1)
                    apply()
                }

                // Start the CharacterSelectActivity
                val characterIntent : Intent = Intent(this,
                                                CharacterSelectActivity::class.java)
                startActivity(characterIntent)
                finish()

            } else {
                // Start the TrainingActivity
                val trainingIntent : Intent = Intent(this,
                    TrainingActivity::class.java)
                startActivity(trainingIntent)
                finish()
            }
            true
        } else {
            false
        }
    }

    /**
     * Creates an infinite timer that changes the picture every 8 seconds.
     */
    private fun pictureChangeTimer() {
        var i = 0
        var listOfIds = listOf("saitama_4", "running","saitama", "pushups", "situps", "squats")
        object : CountDownTimer(48000, 8000) {

            override fun onTick(millisUntilFinished: Long) {
                var animationIn = AnimationUtils.loadAnimation(applicationContext, R.anim.fadein)
                binding.imageView3!!.animation = animationIn
                var imagestring = listOfIds[i % listOfIds.size]
                var context : Context = binding.imageView3!!.context
                var id = context.resources.getIdentifier(imagestring, "drawable",
                                                            context.packageName)
                binding.imageView3!!.setImageResource(id)

                i++
            }

            override fun onFinish() {
                pictureChangeTimer()
            }
        }.start()
    }
}