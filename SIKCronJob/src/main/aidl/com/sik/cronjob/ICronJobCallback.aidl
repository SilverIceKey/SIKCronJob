package com.sik.cronjob;

/**
 * AIDL 回调接口，用于 Service 向主进程通知定时任务的触发。
 */
interface ICronJobCallback {
    void onJobTriggered(int jobId);
}
