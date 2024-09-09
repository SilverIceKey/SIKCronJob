package com.sik.cronjob.managers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.sik.cronjob.ITaskService
import com.sik.cronjob.receivers.CronJobCallback
import com.sik.cronjob.services.TaskService

/**
 * 管理定时任务的类，负责绑定 TaskService 并通过 AIDL 通信启动任务。
 */
class CronJobManager(private val context: Context) {

    private var taskService: ITaskService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            taskService = ITaskService.Stub.asInterface(service)
            taskService?.registerCallback(CronJobCallback()) // 注册回调
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            taskService = null
        }
    }

    /**
     * 绑定服务并准备调度任务。
     */
    fun bindService() {
        val intent = Intent(context, TaskService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * 调度定时任务。
     */
    fun scheduleJob(jobId: Int, intervalMillis: Long, initialDelay: Long) {
        taskService?.scheduleJob(jobId, intervalMillis, initialDelay)
    }
}
