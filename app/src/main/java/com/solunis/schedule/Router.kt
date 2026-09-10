package com.solunis.schedule

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.solunis.schedule.ui.camera.AiCameraActivity
import com.solunis.schedule.ui.gallery.GalleryImportActivity
import com.solunis.schedule.ui.manage.CourseManageActivity

object Router {

    fun dispatch(context: Context) {
        goHome(context)
    }

    fun goHome(context: Context) {
        context.startActivity(Intent(context, HomeActivity::class.java))
        finishIfActivity(context)
    }

    fun goManage(context: Context) {
        context.startActivity(Intent(context, CourseManageActivity::class.java))
    }

    fun goCamera(context: Context) {
        context.startActivity(Intent(context, AiCameraActivity::class.java))
    }

    fun goGallery(context: Context) {
        context.startActivity(Intent(context, GalleryImportActivity::class.java))
    }

    private fun finishIfActivity(context: Context) {
        if (context is Activity) {
            context.finish()
        }
    }
}
