
package com.sik.cronjob.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.sik.cronjob.ICronJobCallback
import com.sik.cronjob.ITaskService

/**
 * TaskService：运行在远程进程的定时任务服务。
 * - 接收来自主进程的调度请求
 * - 使用 Handler 实现周期任务调度
 * - 通过 AIDL 回调方式通知主进程
 */
class TaskService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var callback: ICronJobCallback? = null

    /** 每个任务 ID 对应的间隔时间（毫秒） */
    private val intervalMap = mutableMapOf<Int, Long>()

    /** 每个任务 ID 对应的 Runnable 实例 */
    private val jobRunnableMap = mutableMapOf<Int, Runnable>()

    /** AIDL Binder 实现类 */
    private val binder = object : ITaskService.Stub() {

        /**
         * 注册回调接口，主进程通过 AIDL 注入回调实例
         */
        override fun registerCallback(callback: ICronJobCallback) {
            this@TaskService.callback = callback
        }

        /**
         * 安排一个新任务
         * @param jobId 唯一任务 ID
         * @param intervalMillis 任务重复间隔
         * @param initialDelay 首次执行延迟
         */
        override fun scheduleJob(jobId: Int, intervalMillis: Long, initialDelay: Long) {
            intervalMap[jobId] = intervalMillis
            val runnable = object : Runnable {
                override fun run() {
                    notifyMainProcess(jobId)
                    val nextDelay = intervalMap[jobId] ?: return
                    handler.postDelayed(this, nextDelay)
                }
            }
            jobRunnableMap[jobId] = runnable
            handler.postDelayed(runnable, initialDelay)
        }

        /**
         * 取消指定任务
         * @param jobId 要取消的任务 ID
         */
        override fun cancelJob(jobId: Int) {
            jobRunnableMap[jobId]?.let {
                handler.removeCallbacks(it)
                jobRunnableMap.remove(jobId)
                intervalMap.remove(jobId)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * 向主进程发送任务触发回调
     */
    private fun notifyMainProcess(jobId: Int) {
        // 异步调用，防止主进程耗时阻塞 Service 主线程
        Thread {
            try {
                callback?.onJobTriggered(jobId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
