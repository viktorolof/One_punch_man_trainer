package se.umu.dv20von.one_punch_man_trainer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import se.umu.dv20von.one_punch_man_trainer.databinding.TrainingActivityBinding
import java.util.*


/**
 * The Activity where the user chooses exercises and can see their result.
 *
 * @author Viktor Olofsson dv20von@cs.umu.se
 * @since 2022-03-21
 */
class TrainingActivity : AppCompatActivity() {
    /* Variables*/
    private lateinit var binding: TrainingActivityBinding
    private lateinit var sharedPref: SharedPreferences
    // Male is 0 Female is 1
    private var maleOrFem = -1
    private var hairColor = 0
    private var hairPercent :String? = "zero"
    // Calendar is used for Dates since not all API Levels support the newer Java Date classes.
    private var currentDate = Calendar.getInstance()
    private var currentyear = currentDate.get(Calendar.YEAR)
    private var currentMonth = currentDate.get(Calendar.MONTH)
    private var currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
    private var sameDate = false
    private var lastYear = 0
    private var lastMonth = 0
    private var lastDay = 0
    private var completedChallenges: ArrayList<Int> = arrayListOf(0, 0, 0, 0)
    private var firstTimeTraining = 0

    /* Constants */
    private val BLACK_HAIR = 0
    private val BROWN_HAIR = 1
    private val BLONDE_HAIR = 2
    private val ORANGE_HAIR = 3
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
        binding = TrainingActivityBinding.inflate(layoutInflater)
        val view = binding.root
        sharedPref = this?.getSharedPreferences(
            getString(R.string.shared_file_key), Context.MODE_PRIVATE)
        initalizeValues()
        setContentView(view)

        // Explain to user how to use the app if it is their first time using.
        if(firstTimeTraining == 0) {
            showHelpingText()
            with(sharedPref.edit()) {
                putInt(getString(R.string.first_time_training), 1)
                putBoolean(getString(R.string.same_date), sameDate)
                apply()
            }
        }

        val helpImage = findViewById<ImageView>(R.id.help_icon)
        helpImage.setOnClickListener { v ->
            showHelpingText()
        }

        binding.running.setOnClickListener { v ->
            startExercise(RUNNING)
        }

        binding.pushups.setOnClickListener { v ->
            startExercise(PUSHUPS)
        }

        binding.squats.setOnClickListener { v ->
            startExercise(SQUATS)
        }

        binding.situps.setOnClickListener { v ->
            startExercise(SITUPS)
        }

        binding.imageView.setOnClickListener{ v ->
            // Start the character select screen
            val characterIntent : Intent = Intent(this,
                        CharacterSelectActivity::class.java).apply {
                putExtra(getString(R.string.haircolor_chosen), hairColor)
                putExtra(getString(R.string.male_or_fem), maleOrFem)
                putExtra(getString(R.string.from_training), true)
            }
            startActivity(characterIntent)
            finish()
        }
    }

    /**
     * Displays a DialogFragment that explains to the user how to use the app.
     */
    private fun showHelpingText() {
        var hp = HelpDialogFragment()
        hp.message = getString(R.string.help_dialog_text)
        hp.show(supportFragmentManager, HelpDialogFragment.TAG)
    }

    /**
     * Starts one of the exercises in a new Activity.
     */
    private fun startExercise(exercise : Int) {
        val intent : Intent = Intent(this, ExerciseActivity::class.java).apply {
            putExtra(getString(R.string.exercise_type), exercise)
        }
        startActivity(intent)
        finish()
    }

    /**
     * Initializes user values with data saved in a SharedPreferences file.
     */
    private fun initalizeValues() {
        // Get all data from SharedPreferences
        maleOrFem = sharedPref.getInt(getString(R.string.male_or_fem), 0) // defaults to male
        hairColor = sharedPref.getInt(getString(R.string.haircolor_chosen), 0) // defaults to black hair
        lastDay = sharedPref.getInt(getString(R.string.last_day), 0)
        lastMonth = sharedPref.getInt(getString(R.string.last_month), 0)
        lastYear = sharedPref.getInt(getString(R.string.last_year), 0)
        compareDate()

        if(!sameDate) {
            resetSomeValues()
        }
        calculateHairPercent()
        countCompletedExercises()

        firstTimeTraining = sharedPref.getInt(getString(R.string.first_time_training), 0)

        setCharacterImage()
    }

    /**
     * Calculates how much hair the user should have and updates the textview displaying the result.
     */
    private fun calculateHairPercent() {
        var total = 0
        completedChallenges[0] = sharedPref.getInt(getString(R.string.completed_running), 0)
        completedChallenges[1] = sharedPref.getInt(getString(R.string.completed_pushups), 0)
        completedChallenges[2] = sharedPref.getInt(getString(R.string.completed_squats), 0)
        completedChallenges[3] = sharedPref.getInt(getString(R.string.completed_situps), 0)
        for(i in 0..3) {
            total += completedChallenges[i]
        }

        when (total) {
            0 -> {
                hairPercent = "zero"
                binding.textView.text = "Todays result: 0% hair loss"
            }
            1 ->  {
                hairPercent = "twentyfive"
                binding.textView.text = "Todays result: 25% hair loss"
            }
            2 -> {
                hairPercent = "fifty"
                binding.textView.text = "Todays result: 50% hair loss"
            }
            3 -> {
                hairPercent = "seventyfive"
                binding.textView.text = "Todays result: 75% hair loss"
            }
            4 -> {
                hairPercent = "hundred"
                binding.textView.text = "Todays result: 100% hair loss"
            }
        }
    }

    /**
     * Resets some values in the SharedPreferences-file.
     */
    private fun resetSomeValues() {
        with(sharedPref.edit()) {
            // Set current date
            putInt(getString(R.string.last_year), currentyear)
            putInt(getString(R.string.last_month), currentMonth)
            putInt(getString(R.string.last_day), currentDay)

            // Reset training values
            putInt(getString(R.string.completed_pushups), 0)
            putInt(getString(R.string.completed_running), 0)
            putInt(getString(R.string.completed_situps), 0)
            putInt(getString(R.string.completed_squats), 0)
            putInt(getString(R.string.running_count), 0)
            putInt(getString(R.string.pushup_count), 0)
            putInt(getString(R.string.squat_count), 0)
            putInt(getString(R.string.situp_count), 0)

            apply()
        }
    }

    /**
     * Checks how many exercises are completed and saves the resulting hair percent in the
     * SharedPreferences file.
     */
    private fun countCompletedExercises() {
        var total = 0
        if(completedChallenges[0] != 0) {
            binding.running.backgroundTintList = getColorStateList(R.color.green)
            total++
        }
        if(completedChallenges[1] != 0) {
            binding.pushups.backgroundTintList = getColorStateList(R.color.green)
            total++
        }
        if(completedChallenges[2] != 0) {
            binding.squats.backgroundTintList = getColorStateList(R.color.green)
            total++
        }
        if(completedChallenges[3] != 0) {
            binding.situps.backgroundTintList = getColorStateList(R.color.green)
            total++
        }

        when(total) {
            0 -> putHairPercent("zero")
            1 -> putHairPercent("twentyfive")
            2 -> putHairPercent("fifty")
            3 -> putHairPercent("seventyfive")
            4 -> putHairPercent("hundred")
        }
    }

    /**
     * Sets the amount of hair in he SharedPreferences file.
     */
    private fun putHairPercent(hair: String) {
        with(sharedPref.edit()) {
            putString(getString(R.string.hair_percent), hair)
        }
    }

    /**
     * Sets the correct character image depending on the choices of the user.
     */
    private fun setCharacterImage() {
        var imagestring = ""
        when(hairColor) {
            BLACK_HAIR -> imagestring += "black_"
            BROWN_HAIR -> imagestring += "brown_"
            BLONDE_HAIR -> imagestring += "blonde_"
            ORANGE_HAIR -> imagestring += "orange_"
        }

        if(maleOrFem == 0) {
            imagestring += "m_"
        } else {
            imagestring += "w_"
        }

        if(hairPercent == "hundred") {
            if(maleOrFem == 0) {
                imagestring = "m_hundred"
            } else {
                imagestring = "w_hundred"
            }
        } else if(hairPercent == null) {
            imagestring += "zero"
        } else {
            imagestring += hairPercent
        }


        var context : Context = binding.imageView.context
        var id = context.resources.getIdentifier(imagestring, "drawable", context.packageName)
        binding.imageView.setImageResource(id)
    }

    /**
     * Sets a boolean to true or false depending on the last date the user used the app.
     */
    private fun compareDate() {
        // TODO resetta massa värden i sharedprefs om det är en ny dag
        sameDate = currentDay == lastDay && currentMonth == lastMonth && currentyear == lastYear
        with(sharedPref.edit()) {
            putBoolean(getString(R.string.same_date), sameDate)
            apply()
        }
    }
}