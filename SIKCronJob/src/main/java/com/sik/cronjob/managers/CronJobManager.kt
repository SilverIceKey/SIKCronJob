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
 * 管理定时任务的类，负责绑定 TaskService 并通过 AIDL 通信自动启动任务。
 */
class CronJobManager private constructor(private val context: Context) {

    private var taskService: ITaskService? = null
    private val jobIdMap = mutableMapOf<Int, CronJobScanner.JobInfo>() // jobId -> JobInfo
    private val pendingJobs = mutableListOf<CronJobScanner.JobInfo>() // 服务未连接时的待调度任务
    private val cronJobCallback = CronJobCallback() // 单例回调，避免重复注册

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            taskService = ITaskService.Stub.asInterface(service)
            taskService?.registerCallback(cronJobCallback) // 注册回调

            synchronized(pendingJobs) {
                pendingJobs.forEach { job ->
                    taskService?.scheduleJob(job.jobId, job.intervalMillis, job.initialDelay)
                    jobIdMap[job.jobId] = job
                }
                pendingJobs.clear()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            taskService = null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CronJobManager? = null

        fun getInstance(context: Context): CronJobManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CronJobManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * 绑定到 TaskService 服务，并准备调度任务。
     */
    fun bindService() {
        val intent = Intent(context, TaskService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * 解绑服务，并取消所有任务。
     */
    fun unbindService() {
        jobIdMap.keys.forEach { jobId ->
            cancelJob(jobId)
        }
        jobIdMap.clear()
        synchronized(pendingJobs) {
            pendingJobs.clear()
        }
        context.unbindService(serviceConnection)
    }

    /**
     * 注册并调度多个任务。
     * @param jobs 任务列表
     */
    fun registerJobs(jobs: List<CronJobScanner.JobInfo>) {
        jobs.forEach { job ->
            if (!jobIdMap.containsKey(job.jobId)) {
                scheduleJob(job)
            }
        }
    }

    /**
     * 调度定时任务。
     * @param job 任务信息
     */
    private fun scheduleJob(job: CronJobScanner.JobInfo) {
        if (taskService != null) {
            taskService?.scheduleJob(job.jobId, job.intervalMillis, job.initialDelay)
            jobIdMap[job.jobId] = job
        } else {
            synchronized(pendingJobs) {
                pendingJobs.add(job)
            }
        }
    }

    /**
     * 取消指定的任务。
     * @param jobId 任务唯一标识符
     */
    private fun cancelJob(jobId: Int) {
        taskService?.cancelJob(jobId)
    }
}
