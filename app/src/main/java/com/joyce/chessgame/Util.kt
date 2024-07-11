package com.joyce.chessgame

import android.app.Activity
import android.content.Context
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Util {

    fun getString(id: Int): String{
        return MyApplication.instance.getContext().getString(id)
    }

    fun getCurrentTime():String{
        val sdf = SimpleDateFormat("MM/dd hh:mm", Locale.ROOT)
        return sdf.format(Date(System.currentTimeMillis()))
    }

    fun hideEmail(host: String): String {
        val atIndex = host.indexOf('@')
        if (atIndex == -1) {
            return "Invalid email format"
        }

        val namePart = host.substring(0, atIndex)

        val visiblePart = if (namePart.length <= 4) {
            namePart
        } else {
            namePart.take(4)
        }

        return "$visiblePart*****"
    }

    fun getContext(): Context {
        return MyApplication.instance.applicationContext
    }

    fun showAlertDialog(context: Context, title: String, msg: String){
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.confirm), null)
            .show()
    }

    fun showAlertDialogWithNegative(context: Context, title: String?, msg: String?, haveNo: Boolean, positiveContent: String, negativeContent: String, callback: (Boolean) -> Unit) {
        val alert = android.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(positiveContent) { dialog, _ ->
                callback(true)
                dialog.dismiss()
            }

        if (haveNo){
            alert.setNegativeButton(negativeContent) { dialog, _ ->
                callback(false)
                dialog.dismiss()
            }
        }
        alert.create().show()
    }

    fun hideKeyBoard(activity: Activity){
        val view = activity.findViewById<View>(android.R.id.content)
        if (view != null){
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**EditText字數限制*/
    fun editTextMaxLength(maxLength: Int): Array<InputFilter>{
        return arrayOf(InputFilter.LengthFilter(maxLength))
    }

}