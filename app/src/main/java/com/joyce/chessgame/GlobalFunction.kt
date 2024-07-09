package com.joyce.chessgame

import android.app.Activity
import android.content.Context
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import kotlin.math.max

object GlobalFunction {

    fun getContext(): Context{
        return MyApplication.instance.applicationContext
    }

    fun showAlertDialog(context: Context, title: String, msg: String){
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.confirm.getStringValue(), null)
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

    fun Int.getStringValue(): String{
        return getContext().getString(this)
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