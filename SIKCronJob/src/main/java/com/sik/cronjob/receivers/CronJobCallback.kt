package com.sik.cronjob.receivers

import android.os.Handler
import android.os.Looper
import com.sik.cronjob.ICronJobCallback
import com.sik.cronjob.annotations.CronJob
import com.sik.cronjob.managers.CronJobScanner
import java.lang.reflect.Method

/**
 * 主进程的 AIDL 回调实现，接收到服务端通知时执行相应的任务。
 */
class CronJobCallback : ICronJobCallback.Stub() {

    override fun onJobTriggered(jobId: Int) {
        // 获取已注册的方法和目标对象
        val method = CronJobScanner.getMethodById(jobId)
        val targetObject = CronJobScanner.getTargetById(jobId)

        // 获取 @CronJob 注解的 runOnMainThread 参数
        val cronJob = method?.getAnnotation(CronJob::class.java)
        val runOnMainThread = cronJob?.runOnMainThread ?: true // 默认在主线程执行

        // 如果需要在主线程执行
        if (runOnMainThread) {
            // 使用 Handler 切换到主线程执行任务
            Handler(Looper.getMainLooper()).post {
                invokeMethod(method, targetObject)
            }
        } else {
            // 在当前线程直接执行
            invokeMethod(method, targetObject)
        }
    }

    private fun invokeMethod(method: Method?, targetObject: Any?) {
        try {
            method?.invoke(targetObject) // 通过反射调用无参数的 void 方法
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
