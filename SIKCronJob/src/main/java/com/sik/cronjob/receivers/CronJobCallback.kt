package com.sik.cronjob.receivers

import com.sik.cronjob.ICronJobCallback
import com.sik.cronjob.managers.CronJobScanner

/**
 * 主进程的 AIDL 回调实现，接收到服务端通知时执行相应的任务。
 */
class CronJobCallback : ICronJobCallback.Stub() {
    override fun onJobTriggered(jobId: Int) {
        // 获取已注册的方法并执行
        val method = CronJobScanner.getMethodById(jobId)
        val targetObject = CronJobScanner.getTargetById(jobId)

        method?.let {
            try {
                it.invoke(targetObject) // 通过反射调用无参数的 void 方法
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
