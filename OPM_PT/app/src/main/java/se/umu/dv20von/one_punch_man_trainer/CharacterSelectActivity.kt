package se.umu.dv20von.one_punch_man_trainer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import se.umu.dv20von.one_punch_man_trainer.databinding.CharacterSelectActivityBinding

/**
 * Activity for selecting gender and haircolor.
 *
 * @author Viktor Olofsson dv20von@cs.umu.se
 * @since 2022-03-21
 */
class CharacterSelectActivity : AppCompatActivity() {
    /* Variables */
    private lateinit var binding: CharacterSelectActivityBinding
    lateinit var sharedPref: SharedPreferences
    private var spinnerListener: MySpinnerListener = MySpinnerListener()
    private var currentSpinnerItem = 0
    private var maleOrFem = -1 // -1 is no choice, 0 is male, 1 is female
    private var oldHair = -1
    private var oldGender = -1
    private var fromTraining = false

    /* Constants */
    private val BLACK_HAIR = 0
    private val BROWN_HAIR = 1
    private val BLONDE_HAIR = 2
    private val ORANGE_HAIR = 3
    private val MALE_OR_FEM_KEY = "male or female choice"
    private val SPINNER_KEY = "current spinner choice"

    /**
     * Overwritten function that creates the activity and the view.
     *
     * @param savedInstanceState - A Bundle with values for reconstructing a state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CharacterSelectActivityBinding.inflate(layoutInflater)
        val view = binding.root
        oldHair = intent.getIntExtra(getString(R.string.haircolor_chosen), 0)
        oldGender = intent.getIntExtra(getString(R.string.male_or_fem), 0)
        fromTraining = intent.getBooleanExtra(getString(R.string.from_training), false)
        if(fromTraining) {
            binding.button.text = "Save Changes"
        }
        setContentView(view)
        binding.maleButton.background = getDrawable(R.drawable.black_m_zero)
        binding.femButton.background = getDrawable(R.drawable.black_w_zero)


        ArrayAdapter.createFromResource(this, R.array.spinnerItems,
            android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter
        }

        sharedPref = this?.getSharedPreferences(
            getString(R.string.shared_file_key), Context.MODE_PRIVATE)


        /* OnClickListeners */
        val helpImage = findViewById<ImageView>(R.id.help_icon)
        helpImage.setOnClickListener { v ->
            showHelpingText()
        }

        binding.maleButton.setOnClickListener { v ->
            maleOrFem = 0
            with(sharedPref.edit()) {
                putInt(getString(R.string.male_or_fem), 0)
                apply()
            }
           setSelected()
        }

        binding.femButton.setOnClickListener { v ->
            maleOrFem = 1
            with(sharedPref.edit()) {
                putInt(getString(R.string.male_or_fem), 1)
                apply()
            }
            setSelected()
        }

        binding.button.setOnClickListener { v ->
            if(maleOrFem != -1) {
                val trainingIntent : Intent = Intent(this,
                    TrainingActivity::class.java)
                startActivity(trainingIntent)
                finish()
            } else {
                val text = "You have to select a gender first."
                val duration = Toast.LENGTH_LONG
                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
            }
        }

        binding.spinner.onItemSelectedListener = spinnerListener

    }

    /**
     * Displays a DialogFragment that explains what to do in the activity.
     */
    private fun showHelpingText() {
        var hp = HelpDialogFragment()
        hp.message = getString(R.string.character_help)
        hp.show(supportFragmentManager, HelpDialogFragment.TAG)
    }

    /**
     * Sets the selcted gender and highlights that button.
     */
    fun setSelected() {
        if(maleOrFem == 0) {
            binding.maleButton.backgroundTintList = getColorStateList(R.color.s_yellow)
            binding.maleButton.alpha = 1.0f
            binding.femButton.backgroundTintList= getColorStateList(R.color.gray)
            binding.femButton.alpha = 0.5f
        } else if(maleOrFem == 1) {
            binding.maleButton.backgroundTintList = getColorStateList(R.color.gray)
            binding.maleButton.alpha = 0.5f
            binding.femButton.backgroundTintList = getColorStateList(R.color.s_yellow)
            binding.femButton.alpha = 1.0f
        }
    }

    /**
     * Sets the avatars passed to the function as arguments.
     *
     * @param maleimage An integer with the id for the picture of the male avatar.
     * @param femimage An integer with the id for the picture of the female avatar.
     */
    fun setAvatar(maleimage: Int, femimage: Int) {
        binding.maleButton.setImageResource(maleimage)
        binding.femButton.setImageResource(femimage)
    }

    /**
     * Saves the choice of male or female and the haircolor in a Bundle.
     *
     * @param outState The Bundle for storing values.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(MALE_OR_FEM_KEY, maleOrFem)
        outState.putInt(SPINNER_KEY, currentSpinnerItem)
        super.onSaveInstanceState(outState)
    }

    /**
     * Recreates some values that were saved in a Bundle.
     *
     * @param savedInstanceState The Bundle of values.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        maleOrFem = savedInstanceState.getInt(MALE_OR_FEM_KEY)
        currentSpinnerItem = savedInstanceState.getInt(SPINNER_KEY)
        setSelected()
        super.onRestoreInstanceState(savedInstanceState)
    }

    /**
     * Class for handling the SpinnerItems. When a user selects a haircolor the avatars update
     * their images and the choice is saved in the SharedPreferences file.
     *
     * @author Viktor Olofsson dv20von@cs.umu.se
     * @since 2022-03-21
     */
    private inner class MySpinnerListener: AdapterView.OnItemSelectedListener {
        /**
         * The function called whenever a spinner item is selcted.
         *
         * @param parent An adapterView (Not used in this implementation)
         * @param view A View (Not used in this implementation)
         * @param pos An integer with the position of the selected item.
         * @param id A Long (Not used in this implementation)
         */
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            when(pos) {
                BLACK_HAIR -> setAvatar(R.drawable.black_m_zero, R.drawable.black_w_zero)
                BROWN_HAIR -> setAvatar(R.drawable.brown_m_zero, R.drawable.brown_w_zero)
                BLONDE_HAIR -> setAvatar(R.drawable.blonde_m_zero, R.drawable.blonde_w_zero)
                ORANGE_HAIR -> setAvatar(R.drawable.orange_m_zero, R.drawable.orange_w_zero)
            }
            currentSpinnerItem = pos
            // In the next activity all data can be accessed from the sharedPref file
            with(sharedPref.edit()) {
                putInt(getString(R.string.haircolor_chosen), pos)
                apply()
            }
        }

        /**
         * Overwritten function that does nothing.
         */
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    /**
     * When user has clicked the back button they will be redirected to either the starting activity
     * or the training activity depending on which they came from.
     */
    override fun onBackPressed() {
        if(fromTraining) {
            with(sharedPref.edit()) {
                // If user clicked back reset to old changes
                putInt(getString(R.string.haircolor_chosen), oldHair)
                putInt(getString(R.string.male_or_fem), oldGender)
                apply()
            }
            val trainingIntent : Intent = Intent(this, TrainingActivity::class.java)
            startActivity(trainingIntent)
            finish()
        } else{
            // if we came from the startpage we have to set first time to 0 and go back to the start
            // screen
            with(sharedPref.edit()) {
                putInt(getString(R.string.first_time), 0)
                apply()
            }
            val startIntent : Intent = Intent(this, StartActivity::class.java)
            startActivity(startIntent)
            finish()
        }
    }
}