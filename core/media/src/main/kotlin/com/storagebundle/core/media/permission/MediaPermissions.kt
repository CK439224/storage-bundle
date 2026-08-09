package com.storagebundle.core.media.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Resolves which storage permission this device actually needs.
 *
 * The permission split is a platform requirement, not a preference: `READ_MEDIA_IMAGES`
 * exists only from API 33, and `READ_EXTERNAL_STORAGE` is capped at API 32 in the manifest.
 * Getting this wrong is silent — the app would simply find nothing — so the mapping lives in
 * one place with tests rather than being repeated at each call site.
 *
 * Note that the Photo Picker is deliberately *not* used: both media features need
 * whole-library visibility to audit it, which a per-selection picker cannot provide
 * (PLAN.md §5.1, §5.2).
 */
object MediaPermissions {

    /**
     * The runtime permission required to enumerate images on this device.
     */
    val requiredImagePermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            @Suppress("DEPRECATION")
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    /**
     * The permission needed to post drift alerts, or `null` below API 33 where notifications
     * need no runtime grant.
     */
    val notificationPermission: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

    /** Returns whether the app can currently read the image library. */
    fun hasImageAccess(context: Context): Boolean =
        isGranted(context, requiredImagePermission)

    /** Returns whether the app may post notifications. */
    fun hasNotificationAccess(context: Context): Boolean {
        val permission = notificationPermission ?: return true
        return isGranted(context, permission)
    }

    private fun isGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
