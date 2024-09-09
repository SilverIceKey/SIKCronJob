package com.sik.cronjob.managers

import android.content.Context
import android.content.Intent
import com.sik.cronjob.ITaskService
import com.sik.cronjob.annotations.CronJob
import com.sik.cronjob.services.TaskService
import java.lang.reflect.Method
import java.util.UUID

/**
 * 扫描带有 @CronJob 注解的方法，并将它们注册为定时任务。
 */
object CronJobScanner {

    private val methodMap = mutableMapOf<Int, Method>()  // 保存任务 ID 与方法的映射
    private val targetMap = mutableMapOf<Int, Any>()  // 保存任务 ID 与目标对象的映射
    private var intervalMillis: Long = 0L
    private var initialDelay: Long = 0L
    private var jobId: Int = 0

    /**
     * 扫描对象的所有方法，并注册带有 @CronJob 注解的方法。
     */
    fun scanAndRegisterJobs(targetObject: Any, context: Context) {
        val clazz = targetObject::class.java
        val methods = clazz.declaredMethods

        for (method in methods) {
            method.getAnnotation(CronJob::class.java)?.let { cronJob ->
                jobId = generateUniqueJobId() // 使用 UUID 生成 jobId
                intervalMillis = cronJob.intervalMillis
                initialDelay = cronJob.initialDelay
                methodMap[jobId] = method
                targetMap[jobId] = targetObject
                startServiceProcess(context)
            }
        }
    }

    private fun startServiceProcess(context: Context) {
        val intent = Intent(context, TaskService::class.java).apply {
            putExtra("jobId", jobId)
            putExtra("intervalMillis", intervalMillis)
            putExtra("initialDelay", initialDelay)
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName?, service: android.os.IBinder?) {
            val taskService = ITaskService.Stub.asInterface(service)
            taskService?.scheduleJob(jobId, intervalMillis, initialDelay)
        }

        override fun onServiceDisconnected(name: android.content.ComponentName?) {
            // 处理服务断开连接
        }
    }

    fun getMethodById(jobId: Int): Method? = methodMap[jobId]
    fun getTargetById(jobId: Int): Any? = targetMap[jobId]

    // 使用 UUID 生成唯一 jobId
    private fun generateUniqueJobId(): Int {
        return UUID.randomUUID().hashCode()
    }
}
