package se.umu.dv20von.one_punch_man_trainer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * Class for displaying a DialogFragment that guides the user on how to use the app.
 *
 * @author Viktor Olofsson dv20von@cs.umu.se
 * @since 2022-03-21
 */
class HelpDialogFragment: DialogFragment() {
    var message: String? = ""
    /**
     * Overwritten function that creates a DialogFragment.
     *
     * @param savedInstanceState A bundle.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(if(message!="") message else {getStringFromBundle(savedInstanceState)})
            .setPositiveButton(getString(R.string.ok)) { _,_ ->  }
            .create()

    /**
     * Saves a string from the Bundle in the attribute "message" and returns it.
     * @param savedInstanceState The bundle wit the string.
     * @return A string with the message.
     */
    private fun getStringFromBundle(savedInstanceState: Bundle?): String? {
        message = savedInstanceState!!.getString(KEY)
        return message
    }

    /**
     * A companion object.
     */
    companion object {
        const val TAG = "HelpDialog"
        const val KEY = "My dialog key"
    }

    /**
     * Saves the the message string in order to recreate the dialog at a later stage ( eg when
     * rotating phone)
     *
     * @param outState - The bundle where the string is stored.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY, message)
        super.onSaveInstanceState(outState)
    }

}