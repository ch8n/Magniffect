package ch8n.project.magniffect.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog


object PromptUtils {

    fun alertDialog(context: Context, @StyleRes style: Int, dialogBuilder: AlertDialog.Builder.() -> Unit): Dialog {
        val builder = AlertDialog.Builder(context, style).also {
            it.setCancelable(false)
            it.dialogBuilder()
        }
        return builder.create()
    }

    fun AlertDialog.Builder.negativeButton(text: String = "Dismiss", handleClick: (dialogInterface: DialogInterface) -> Unit = {}) {
        this.setPositiveButton(text) { dialogInterface, which -> handleClick(dialogInterface) }
    }

    fun AlertDialog.Builder.positiveButton(text: String = "Continue", handleClick: (dialogInterface: DialogInterface) -> Unit = {}) {
        this.setNegativeButton(text) { dialogInterface, which -> handleClick(dialogInterface) }
    }

    fun toast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}