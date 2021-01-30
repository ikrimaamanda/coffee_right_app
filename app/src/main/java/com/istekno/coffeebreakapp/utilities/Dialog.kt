package com.istekno.coffeebreakapp.utilities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.istekno.coffeebreakapp.R
import com.istekno.coffeebreakapp.databinding.DialogUpdatingBinding
import java.util.logging.Handler

class Dialog {
    fun dialog(context: Context?, message: String, listAction: () -> Unit) {
        val dialog = AlertDialog.Builder(context).apply {
            setTitle("Notice")
            setMessage(message)
            setCancelable(false)
            setPositiveButton("Yes") { dialogInterface, i ->
                listAction()
            }
            setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
        }
        dialog.show()
    }

    fun dialogCancel(context: Context?, message: String, listAction: () -> Unit) {
        val dialog = AlertDialog.Builder(context).apply {
            setTitle("Notice")
            setMessage(message)
            setCancelable(false)
            setPositiveButton("OK") { _, _ ->
                listAction()
            }
        }
        dialog.show()
    }

    fun dialogCheckInternet(context: Context?, activity: Activity) {
        val dialog = AlertDialog.Builder(context).apply {
            setTitle("Network Info")
            setMessage("No internet connection\nCheck your internet connectivity and try again")
            setIcon(R.drawable.ic_no_internet)
            setCancelable(false)
            setPositiveButton("OK") { _, _ ->
                activity.finishAffinity()
            }
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun dialogUpdating(context: Context?, activity: Activity, action:() -> Unit) {
        val customView = DataBindingUtil.inflate<DialogUpdatingBinding>(activity.layoutInflater, R.layout.dialog_updating, null, false)

        val dialog = AlertDialog.Builder(context)
            .setView(customView.root)
            .setCancelable(false)
            .create()
        dialog.show()
        android.os.Handler.createAsync(Looper.getMainLooper()).postDelayed(
            {
                dialog.dismiss()
                action()
            }, 3000
        )
    }
}