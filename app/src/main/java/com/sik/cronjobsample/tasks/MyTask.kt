package com.sik.cronjobsample.tasks

import android.util.Log
import com.sik.cronjob.annotations.CronJob

class MyTask {
    @CronJob(
        intervalMillis = 5000,
        initialDelay = 5000,
        runOnMainThread = true
    ) // 每 60 秒执行一次，延迟 5 秒开始
    fun performTask() {
        // 你的任务逻辑
        Log.i("Task","定时任务执行中1111...")
    }
}