package com.sik.cronjob.receivers

import android.os.Handler
import android.os.Looper
import com.sik.cronjob.ICronJobCallback
import com.sik.cronjob.annotations.CronJob
import com.sik.cronjob.managers.CronJobScanner
import java.lang.reflect.Method

/**
 * CronJobCallback 是主进程中的 AIDL 回调实现。
 * 当服务进程触发某个定时任务时，会调用此回调中的 onJobTriggered 方法，
 * 进而根据 jobId 查找对应的目标方法并执行它。
 */
class CronJobCallback : ICronJobCallback.Stub() {

    /**
     * 服务进程触发任务时调用此方法
     * @param jobId 任务的唯一标识符
     */
    override fun onJobTriggered(jobId: Int) {
        // 1. 根据 jobId 查找注册的 method 和其绑定的 target 对象（执行者）
        val method = CronJobScanner.getMethodById(jobId)
        val targetObject = CronJobScanner.getTargetById(jobId)

        // 2. 读取方法上的 @CronJob 注解，判断是否要求主线程运行
        val cronJob = method?.getAnnotation(CronJob::class.java)
        val runOnMainThread = cronJob?.runOnMainThread ?: true // 默认主线程执行

        // 3. 根据注解配置切换线程执行方法
        if (runOnMainThread) {
            // 主线程执行（如需操作 UI 等）
            Handler(Looper.getMainLooper()).post {
                invokeMethod(method, targetObject)
            }
        } else {
            // 子线程或当前线程直接执行（适用于后台逻辑）
            invokeMethod(method, targetObject)
        }
    }

    /**
     * 封装反射调用方法逻辑
     * @param method 被调用的方法
     * @param targetObject 方法绑定的目标对象
     */
    private fun invokeMethod(method: Method?, targetObject: Any?) {
        try {
            method?.invoke(targetObject) // 执行无参数的 void 方法
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: 可以考虑上报异常或日志记录
        }
    }
}
