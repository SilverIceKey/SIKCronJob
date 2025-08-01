package com.sik.cronjob.managers

import com.sik.cronjob.annotations.CronJob
import java.lang.reflect.Method
import java.util.UUID

/**
 * 扫描带有 @CronJob 注解的方法，并收集任务信息。
 */
object CronJobScanner {

    private val methodMap = mutableMapOf<Int, Method>()  // 保存任务 ID 与方法的映射
    private val targetMap = mutableMapOf<Int, Any>()     // 保存任务 ID 与目标对象的映射

    /**
     * 扫描对象的所有方法，并收集带有 @CronJob 注解的方法。
     * @param targetObject 需要扫描的对象
     */
    fun scanJobs(targetObject: Any): List<JobInfo> {
        val jobList = mutableListOf<JobInfo>()
        val clazz = targetObject::class.java
        val methods = clazz.declaredMethods

        for (method in methods) {
            method.getAnnotation(CronJob::class.java)?.let { cronJob ->
                method.isAccessible = true // 确保私有方法可访问

                // 去重：避免方法重复注册
                if (methodMap.containsValue(method)) return@let

                val jobId = generateUniqueJobId()
                val intervalMillis = cronJob.intervalMillis
                val initialDelay = cronJob.initialDelay

                methodMap[jobId] = method
                targetMap[jobId] = targetObject
                jobList.add(JobInfo(jobId, intervalMillis, initialDelay))
            }
        }
        return jobList
    }

    /**
     * 获取任务对应的方法。
     */
    fun getMethodById(jobId: Int): Method? = methodMap[jobId]

    /**
     * 获取任务对应的目标对象。
     */
    fun getTargetById(jobId: Int): Any? = targetMap[jobId]

    /**
     * 使用 UUID 生成唯一 jobId。
     */
    private fun generateUniqueJobId(): Int {
        return UUID.randomUUID().hashCode()
    }

    /**
     * 任务信息数据类。
     */
    data class JobInfo(val jobId: Int, val intervalMillis: Long, val initialDelay: Long)
}
