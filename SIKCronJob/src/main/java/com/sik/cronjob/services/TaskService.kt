package com.sik.cronjob.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.sik.cronjob.ICronJobCallback
import com.sik.cronjob.ITaskService

/**
 * 服务进程，用于接收任务调度的请求并在定时器触发时通知主进程。
 */
class TaskService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var callback: ICronJobCallback? = null
    private var intervalMillis: Long = 0L

    private val binder = object : ITaskService.Stub() {
        override fun registerCallback(callback: ICronJobCallback) {
            this@TaskService.callback = callback // 绑定回调接口
        }

        override fun scheduleJob(jobId: Int, intervalMillis: Long, initialDelay: Long) {
            this@TaskService.intervalMillis = intervalMillis
            handler.postDelayed({ notifyMainProcess(jobId) }, initialDelay)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun notifyMainProcess(jobId: Int) {
        callback?.onJobTriggered(jobId)
        // 重新调度任务
        handler.postDelayed({ notifyMainProcess(jobId) }, intervalMillis)
    }
}
