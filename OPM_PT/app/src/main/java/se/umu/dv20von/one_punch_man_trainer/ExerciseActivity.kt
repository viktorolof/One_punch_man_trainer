package se.umu.dv20von.one_punch_man_trainer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import se.umu.dv20von.one_punch_man_trainer.databinding.ExerciseActivityBinding
import android.content.pm.PackageManager
import android.widget.ImageView


/**
 * The class for an Exercise. Depending on what choice the user made it can either be
 * running, push-ups, squats, or sit-ups. Makes use of the accelerometer to count amount
 * of reps.
 *
 * @author Viktor Olofsson dv20von@cs.umu.se
 * @since 2022-03-21
 */
class ExerciseActivity : AppCompatActivity() {
    /* Variables */
    private lateinit var binding: ExerciseActivityBinding
    private var exerciseType = -1
    private var count = 0
    private var previousMagnitude = 0.00
    private var previousY = 0.00
    private var lastMotion = System.currentTimeMillis()
    private val multiListener = MultiSensorListener()
    private var sameDate: Boolean =false
    private lateinit var sharedPref: SharedPreferences
    private var startPress = 0
    private var permissionOk = false

    /* Constants */
    private val RUNNING = 0
    private val PUSHUPS = 1
    private val SQUATS = 2
    private val SITUPS = 3

    /**
     * Overwritten function that is called whenever the Activity is created.
     *
     * @param savedInstanceState - A bundle used to restore a state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExerciseActivityBinding.inflate(layoutInflater)
        val view = binding.root
        /* Fetch some values before the view is created */
        sharedPref = this?.getSharedPreferences(
            getString(R.string.shared_file_key), Context.MODE_PRIVATE)
        exerciseType = intent.getIntExtra(getString(R.string.exercise_type), -1)
        sameDate = sharedPref.getBoolean(getString(R.string.same_date), false)
        initCount()
        initView()
        setContentView(view)

        var sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        var accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if(exerciseType != PUSHUPS) {
            doPermissionCheck()
        }

        val helpImage = findViewById<ImageView>(R.id.help_icon)
        helpImage.setOnClickListener { v ->
            var hp = HelpDialogFragment()
            hp.message = getHelpString()
            hp.show(supportFragmentManager, HelpDialogFragment.TAG)
        }

        binding.startButton.setOnClickListener { v ->
            if(permissionOk || exerciseType == PUSHUPS) {
                if(startPress == 0) {
                    // Activate the sensor and start the exercise
                    sensorManager.registerListener(multiListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
                    binding.startButton.backgroundTintList = getColorStateList(R.color.s_red)
                    binding.startButton.text = "Stop exercise"
                    startPress++

                } else {
                    // Deactivate the sensor and stop the exercise
                    sensorManager.unregisterListener(multiListener)
                    binding.startButton.backgroundTintList = getColorStateList(R.color.s_yellow)
                    binding.startButton.text = "Start exercise"
                    startPress--
                }
            }
        }
    }

    /**
     * Gets the string depending on exercise to be displayed in a DialogFragment.
     */
    private fun getHelpString(): String {
        var str = ""
        when(exerciseType) {
            RUNNING -> str = getString(R.string.running_guide)
            PUSHUPS -> str = getString(R.string.pushup_guide)
            SQUATS -> str = getString(R.string.squat_guide)
            SITUPS -> str = getString(R.string.situp_guide)
        }
        return str
    }

    /**
     * Initialises the amount of reps if the last tim the user exercised was on the same day.
     */
    private fun initCount() {
        if(sameDate) {
            getAmountOfReps()
        }
    }

    /**
     * Request permissions for using the Accelerometer.
     */
    private fun doPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, "ACTIVITY_RECOGNITION")
            != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 0)
        }
    }

    /**
     * Overwritten function that handles whenever the user declined the request for the
     * Accelerometer. Displays a DialogFragment explaining why it is needed.
     *
     * @param requestCode An int with the requestcode.
     * @param permissions The array of different permissions
     * @param grantResults An array that stores which permissions have been granted or denied.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        when (requestCode) {
            0 -> {
                if (!(grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Explain to the user why the accelerometer is needed
                    var hp = HelpDialogFragment()
                    hp.message = getString(R.string.permission_accelerometer)
                    hp.show(supportFragmentManager, HelpDialogFragment.TAG)
                } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionOk = true
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Initialises the different components depending on the exercise.
     */
    private fun initView() {
        if(exerciseType == RUNNING) {
            binding.trainingDescription.text = "Put the phone in your pocket and run 10 000 steps in order to complete this exercise."
            binding.exerciseImage.setImageResource(R.drawable.running)
            binding.counterTextview.text = "Steps: " + count
        }
        if(exerciseType == PUSHUPS) {
            binding.trainingDescription.text = "Put the phone on the ground and do 100 push-ups. One pushup is only counted if your nose touches the screen."
            binding.exerciseImage.setImageResource(R.drawable.pushups)
            binding.counterTextview.text = "Push-ups: " +count
        }
        if(exerciseType == SQUATS) {
            binding.trainingDescription.text = "Put the phone in your pocket and 100 squats to complete this exercise."
            binding.exerciseImage.setImageResource(R.drawable.squats)
            binding.counterTextview.text = "Squats: " +count

        }
        if(exerciseType == SITUPS) {
            binding.trainingDescription.text = "Hold the phone in your hand next to your head and do 100 sit-ups to complete this exercise."
            binding.exerciseImage.setImageResource(R.drawable.situps)
            binding.counterTextview.text = "Sit-ups: " + count
        }
    }

    /**
     * Class that acts a sensorlistener for three of the exercises, running, squats, and sit-ups.
     * The stepcounter for running is less sensitive than regular step counters in order to measure
     * running steps and not every walking step. Both the squat and the sit-up counters prevents
     * the user from cheating too much by only allowing one rep per ~1.5 seconds.
     *
     * @author Viktor Olofsson dv20von@cs.umu.se
     * @since 2022-03-21
     */
    private inner class MultiSensorListener: SensorEventListener {
        /**
         * Function is called whenever the accelerometer detects a change in motion.
         * @param p0 - A SensorEvent.
         */
        override fun onSensorChanged(p0: SensorEvent?) {
            if(p0!=null) {
                /* Get all the values from the SensorEvent */
                var x_acc = p0.values[0]
                var y_acc = p0.values[1]
                var z_acc = p0.values[2]

                /* Calculate the magnitude of the motion using the current values and the
                previous ones*/
                var magnitude = Math.sqrt((x_acc * x_acc + y_acc*y_acc + z_acc*z_acc).toDouble())
                var deltaMagnitude = magnitude - previousMagnitude
                previousMagnitude = magnitude
                var deltaY = y_acc - previousY
                previousY = y_acc.toDouble()
                var currentTime = System.currentTimeMillis()

                /* Depending on the exercise handle the data accordingly */
                if(exerciseType == RUNNING) {
                    runHandler(deltaMagnitude)
                }  else if(exerciseType == SQUATS) {
                    squatHandler(deltaY, currentTime)
                } else if(exerciseType == SITUPS) {
                    situpHandler(deltaMagnitude, currentTime)
                }
            }
        }

        /**
         * Checks if the motion was big enough and if the time was long enough to be a situp.
         *
         * @param deltaMagnitude - The magnitude of the motion.
         * @param currentTime - The time when the motion occurred.
         */
        private fun situpHandler(deltaMagnitude: Double, currentTime: Long) {
            if(deltaMagnitude > 1.5 && currentTime - lastMotion > 1500) {
                count++
                binding.counterTextview.text = "Sit-ups: " + count;
                lastMotion = currentTime
            }
        }

        /**
         * Checks if the motion was big enough and if the time was long enough to be a squat.
         *
         * @param deltaY - The magnitude of the motion in the y-axis.
         * @param currentTime - The time when the motion occurred.
         */
        private fun squatHandler(deltaY: Double, currentTime: Long) {
            if((deltaY > 2 || deltaY < -2) && currentTime - lastMotion > 1600) {
                count++
                binding.counterTextview.text = "Squats " + count

                lastMotion = currentTime
            }
        }

        /**
         * Checks if the motion was big enough to be a running step.
         *
         * @param deltaMagnitude - The magnitude of the motion.
         */
        private fun runHandler(deltaMagnitude: Double) {
            if(deltaMagnitude > 10) {
                count++;
                binding.counterTextview.text = "Steps: " + count
            }
        }

        // Pushups are not handled here

        /**
         * Overwritten function that does nothing.
         */
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    /**
     * Saves the amount of reps in the SharedPreferences file before activity is destroyed.
     */
    override fun onDestroy() {
        putAmountOfReps()
        super.onDestroy()
    }

    /**
     * Saves the amount of reps in the SharePreferences file.
     */
    private fun putAmountOfReps() {
        var completed : Int = if(count >= 100) 1 else 0

        with(sharedPref.edit()) {
            if (exerciseType == RUNNING) {
                putInt(getString(R.string.running_count), count)
                if(count >= 10000) {
                    putInt(getString(R.string.completed_running), 1)
                }
            } else if (exerciseType == PUSHUPS) {
                putInt(getString(R.string.pushup_count), count)
                putInt(getString(R.string.completed_pushups), completed)
            } else if (exerciseType == SQUATS) {
                putInt(getString(R.string.squat_count), count)
                putInt(getString(R.string.completed_squats), completed)
            } else if (exerciseType == SITUPS) {
                putInt(getString(R.string.situp_count), count)
                putInt(getString(R.string.completed_situps), completed)
            }
            apply()
        }
    }

    /**
     * Saves the amount of reps in the SharedPreferences when the activity goes into the
     * background, phone is locked etc.
     *
     * @param outState A Bundle to store values.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        putAmountOfReps()
        super.onSaveInstanceState(outState)
    }

    /**
     * Gets the amount of reps from the SharedPreferences file.
     */
    private fun getAmountOfReps() {
        if (exerciseType == RUNNING) {
            count = sharedPref.getInt(getString(R.string.running_count), 0)
        } else if (exerciseType == PUSHUPS) {
            count = sharedPref.getInt(getString(R.string.pushup_count), 0)
        } else if (exerciseType == SQUATS) {
            count = sharedPref.getInt(getString(R.string.squat_count), 0)
        } else if (exerciseType == SITUPS) {
            count = sharedPref.getInt(getString(R.string.situp_count), 0)
        }
    }

    /**
     * Re-initializes some values when activity is restored.
     * @param savedInstanceState A Bundle of values.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        getAmountOfReps()
        super.onRestoreInstanceState(savedInstanceState)
    }

    /**
     * Sets the touch event for the screen. Only if the user chose push-ups will the event
     * do something.
     *
     * @param event A MotionEvent.
     * @return a Boolean. (Value not used in this app)
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event!!.action == MotionEvent.ACTION_UP) {
            if(exerciseType == PUSHUPS && startPress == 1) {
                count++
                binding.counterTextview.text = "Push-ups: " + count
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Saves the amount of reps before going back to the TrainingActivity.
     */
    override fun onBackPressed() {
        putAmountOfReps()
        val trainingIntent : Intent = Intent(this, TrainingActivity::class.java)
        startActivity(trainingIntent)
        finish()
    }

}