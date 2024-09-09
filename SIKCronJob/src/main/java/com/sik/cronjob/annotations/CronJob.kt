package com.sik.cronjob.annotations

/**
 * 注解类，用于标记定时任务的方法。
 * @param intervalMillis 每个任务的执行间隔（以毫秒为单位）
 * @param initialDelay 任务初始延迟时间（以毫秒为单位）
 * @param runOnMainThread 是否在主线程运行任务
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CronJob(
    val intervalMillis: Long = 0,
    val initialDelay: Long = 0,
    val runOnMainThread: Boolean = true
)
