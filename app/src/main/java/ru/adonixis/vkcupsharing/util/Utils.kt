package ru.adonixis.vkcupsharing.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar

object Utils {
    @JvmStatic
    fun convertDpToPixel(context: Context, dp: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(context: Context, px: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun showSnackbar(view: View,
                     callback: Snackbar.Callback?,
                     @ColorInt backgroundColor: Int,
                     @ColorInt textColor: Int,
                     text: String,
                     @ColorInt actionTextColor: Int,
                     actionText: String,
                     onClickListener: View.OnClickListener?) {
        var onClickListener = onClickListener
        if (onClickListener == null) {
            onClickListener = View.OnClickListener { }
        }
        val snackbar = Snackbar
            .make(view, text, Snackbar.LENGTH_LONG)
            .addCallback(callback!!)
            .setActionTextColor(actionTextColor)
            .setAction(actionText, onClickListener)
        val sbView = snackbar.view
        sbView.setBackgroundColor(backgroundColor)
        val textView = sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(textColor)
        snackbar.show()
    }

    fun getPath(context: Context, uri: Uri): String {
        if (uri.scheme == "file") {
            if (uri.path != null) return uri.path!!
            return ""
        }
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, proj, null, null, null)
        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return "file://" + cursor.getString(columnIndex)
    }
}