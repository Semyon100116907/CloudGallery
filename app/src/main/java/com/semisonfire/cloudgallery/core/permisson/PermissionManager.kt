package com.semisonfire.cloudgallery.core.permisson

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import javax.inject.Inject

interface PermissionResultCallback {
    fun onPermissionGranted()
    fun onPermissionDenied(permissionList: Array<String>)
    fun onPermissionPermanentlyDenied(permission: String)
}

private data class PermissionEntity(
    val permission: String = "",
    val permanentlyDenied: Boolean = false
)

data class AlertButton(
    val title: String = "",
    val clickListener: (() -> Unit)? = null
) {
    fun isEmpty(): Boolean {
        return title.isEmpty() && clickListener == null
    }

    fun isNotEmpty(): Boolean = !isEmpty()
}

class PermissionManager @Inject constructor() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 10020
    }

    private var permissionResultCallback: PermissionResultCallback? = null

    fun onRequestPermissionsResult(
        context: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {

            val permissionResult = mutableListOf<PermissionEntity>()

            //Check permission results
            grantResults.forEachIndexed { i, result ->
                if (result == PackageManager.PERMISSION_DENIED) {
                    val permission = permissions[i]
                    val permanentlyDenied = isPermissionPremanentlyDenied(context, permission)
                    val permissionEntity = PermissionEntity(permission, permanentlyDenied)
                    permissionResult.add(permissionEntity)
                }
            }

            when {
                permissionResult.isEmpty() && grantResults.isNotEmpty() -> permissionResultCallback?.onPermissionGranted()
                else -> {
                    val deniedPermissions = mutableListOf<String>()
                    permissionResult.forEach {
                        if (!it.permanentlyDenied) {
                            deniedPermissions.add(it.permission)
                        } else {
                            permissionResultCallback?.onPermissionPermanentlyDenied(it.permission)
                            return
                        }
                    }
                    permissionResultCallback?.onPermissionDenied(deniedPermissions.toTypedArray())
                }
            }
        }
    }

    fun checkPermissions(
        context: Activity,
        permissionResultCallback: PermissionResultCallback,
        vararg permissions: String
    ) {
        if (permissions.isEmpty()) {
            return
        }
        this.permissionResultCallback = permissionResultCallback

        val isGranted = isGranted(permissions, context)

        if (!isGranted) {
            ActivityCompat.requestPermissions(context, permissions, PERMISSION_REQUEST_CODE)
        } else {
            permissionResultCallback.onPermissionGranted()
        }
    }

    fun isPermissionPremanentlyDenied(activity: Activity, permission: String): Boolean {
        return !shouldShowRationale(activity, permission)
    }

    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun permissionAlertDialog(
        context: Context,
        title: String = "",
        message: String = "",
        positive: AlertButton = AlertButton(),
        neutral: AlertButton = AlertButton(),
        negative: AlertButton = AlertButton()
    ) {
        val builder = AlertDialog.Builder(context)

        if (title.isNotEmpty()) {
            builder.setTitle(title)
        }
        if (message.isNotEmpty()) {
            builder.setMessage(message)
        }

        val dialogListener =
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                when (which) {
                    AlertDialog.BUTTON_POSITIVE -> positive.clickListener?.invoke()
                    AlertDialog.BUTTON_NEGATIVE -> negative.clickListener?.invoke()
                    AlertDialog.BUTTON_NEUTRAL -> neutral.clickListener?.invoke()
                }
                dialog.dismiss()
            }
        if (positive.isNotEmpty()) {
            builder.setPositiveButton(positive.title, dialogListener)
        }
        if (negative.isNotEmpty()) {
            builder.setNegativeButton(negative.title, dialogListener)
        }
        if (neutral.isNotEmpty()) {
            builder.setNeutralButton(neutral.title, dialogListener)
        }

        builder.create().show()
    }

    fun openApplicationSettings(context: Context) {
        val intent = Intent(
            ACTION_APPLICATION_DETAILS_SETTINGS,
            fromParts("package", context.packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(intent)
    }

    private fun checkPermissionGranted(context: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun isGranted(
        permissions: Array<out String>,
        context: Activity
    ): Boolean {
        var isGranted = true
        permissions.forEach { permission ->
            if (!checkPermissionGranted(context, permission)) {
                isGranted = false
                return@forEach
            }
        }
        return isGranted
    }
}