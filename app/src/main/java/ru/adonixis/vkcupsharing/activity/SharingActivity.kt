package ru.adonixis.vkcupsharing.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.vk.api.sdk.VK
import kotlinx.android.synthetic.main.activity_sharing.*
import ru.adonixis.vkcupsharing.R
import ru.adonixis.vkcupsharing.util.Utils
import ru.adonixis.vkcupsharing.util.Utils.showSnackbar
import ru.adonixis.vkcupsharing.viewmodel.SharingViewModel
import java.io.*

class SharingActivity: AppCompatActivity() {

    private lateinit var comment: String
    private lateinit var photoUri: Uri
    private lateinit var viewModel: SharingViewModel
    private lateinit var mProgressDialog: ProgressDialog
    private val filePhoto: File? = null

    companion object {
        private const val TAG = "SharingActivity"
        private const val REQUEST_READ_EXTERNAL_STORAGE = 1
        private const val REQUEST_PICK_IMAGE = 2

        fun startFrom(context: Context) {
            val intent = Intent(context, SharingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sharing)

        viewModel = ViewModelProvider(this@SharingActivity).get(SharingViewModel::class.java)
        viewModel.getResultLiveData().observe(this, Observer {
            mProgressDialog.dismiss()
            showSuccessMessage(getString(R.string.message_wall_ok))
        })
        viewModel.getErrorMessageLiveData().observe(this, Observer {
            mProgressDialog.dismiss()
            showErrorMessage(it)
        })

        btnSelectPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@SharingActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestReadExtStorage()
            } else {
                pickPhoto()
            }
        }

        mProgressDialog = ProgressDialog(this, R.style.AppTheme_Light_Dialog)
        mProgressDialog.isIndeterminate = true
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage(getString(R.string.progress_message_sharing))
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    private fun requestReadExtStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@SharingActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            Snackbar.make(
                layoutRoot,
                R.string.message_rationale_permission,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.snackbar_action_grant) {
                    ActivityCompat.requestPermissions(
                        this@SharingActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_READ_EXTERNAL_STORAGE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this@SharingActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPhoto()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode == RESULT_OK && data != null && data.data != null) {
                val bottomSheetDialog = BottomSheetDialog(this@SharingActivity)
                bottomSheetDialog.setContentView(R.layout.dialog_sharing)
                bottomSheetDialog.setOnShowListener { dialog ->
                    Handler().postDelayed({
                        val d = dialog as BottomSheetDialog
                        val bottomSheet = d.findViewById<FrameLayout>(R.id.design_bottom_sheet)
                        val bottomSheetBehavior: BottomSheetBehavior<*> =
                            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }, 0)
                }
                bottomSheetDialog.show()

                photoUri = Uri.parse(Utils.getPath(this, data.data!!))

                val imagePhoto = bottomSheetDialog.findViewById<ImageView>(R.id.imagePhoto)
                imagePhoto?.setImageURI(photoUri)
                val icDismiss = bottomSheetDialog.findViewById<ImageView>(R.id.icDismiss)
                icDismiss?.setOnClickListener { bottomSheetDialog.hide() }
                val etComment = bottomSheetDialog.findViewById<EditText>(R.id.etComment)
                val btnSend = bottomSheetDialog.findViewById<Button>(R.id.btnSend)
                btnSend?.setOnClickListener {
                    bottomSheetDialog.hide()
                    comment = etComment?.text.toString()
                    viewModel.sharePost(comment, photoUri)
                    mProgressDialog.show()
                }
            }
        }
    }

    private fun showErrorMessage(errorMessage: String) {
        showSnackbar(
            layoutRoot, Snackbar.Callback(),
            ContextCompat.getColor(this, R.color.red),
            Color.WHITE,
            errorMessage,
            Color.WHITE,
            getString(R.string.snackbar_action_hide), null
        )
    }

    private fun showSuccessMessage(successMessage: String) {
        showSnackbar(
            layoutRoot, Snackbar.Callback(),
            ContextCompat.getColor(this, R.color.green),
            Color.WHITE,
            successMessage,
            Color.WHITE,
            getString(R.string.snackbar_action_hide), null
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        VK.logout()
        WelcomeActivity.startFrom(this)
        finish()
    }
}