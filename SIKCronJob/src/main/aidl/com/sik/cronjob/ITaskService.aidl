package com.sik.cronjob;

import com.sik.cronjob.ICronJobCallback;

/**
 * AIDL 服务接口，用于在主进程和服务进程之间通信，
 * 注册任务回调和调度任务。
 */
interface ITaskService {
    // 注册回调，用于主进程传递回调
    void registerCallback(in ICronJobCallback callback);

    // 启动任务调度
    void scheduleJob(int jobId, long intervalMillis, long initialDelay);
}
