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
    // 用于存储 jobId 和任务之间的映射，管理当前所有正在调度的任务
    private val jobIdMap = mutableMapOf<Int, Boolean>() // jobId -> 是否仍在运行

    private val serviceConnection = object : ServiceConnection {
        // 当服务绑定成功时调用
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            taskService = ITaskService.Stub.asInterface(service)
            taskService?.registerCallback(CronJobCallback()) // 注册回调
        }

        // 当服务断开连接时调用
        override fun onServiceDisconnected(name: ComponentName?) {
            taskService = null
        }
    }

    /**
     * 绑定到 TaskService 服务，并准备调度任务。
     */
    fun bindService() {
        val intent = Intent(context, TaskService::class.java)
        // 绑定服务，使用 BIND_AUTO_CREATE 标记确保服务启动
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * 解绑服务，并取消所有任务。
     */
    fun unbindService() {
        // 解绑时取消所有已经调度的任务
        jobIdMap.keys.forEach { jobId ->
            cancelJob(jobId) // 取消任务
        }
        context.unbindService(serviceConnection) // 解绑服务
    }

    /**
     * 调度定时任务。
     * @param jobId 任务唯一标识符
     * @param intervalMillis 执行间隔（毫秒）
     * @param initialDelay 初始延迟时间（毫秒）
     */
    fun scheduleJob(jobId: Int, intervalMillis: Long, initialDelay: Long) {
        taskService?.scheduleJob(jobId, intervalMillis, initialDelay)
        jobIdMap[jobId] = true // 将任务添加到 jobIdMap 中，表示任务正在运行
    }

    /**
     * 取消指定的任务。
     * @param jobId 任务唯一标识符
     */
    fun cancelJob(jobId: Int) {
        taskService?.cancelJob(jobId)
        jobIdMap.remove(jobId) // 从 jobIdMap 中移除该任务
    }
}