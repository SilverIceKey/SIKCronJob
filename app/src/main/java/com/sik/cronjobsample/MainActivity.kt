package com.sik.cronjobsample

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sik.cronjob.managers.CronJobManager
import com.sik.cronjob.managers.CronJobScanner
import com.sik.cronjobsample.tasks.MyTask

class MainActivity : AppCompatActivity() {
    private lateinit var cronJobManager: CronJobManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 初始化 CronJobManager
        cronJobManager = CronJobManager.getInstance(this)

        // 扫描并收集任务信息
        val jobs = CronJobScanner.scanJobs(MyTask())

        // 注册任务
        cronJobManager.registerJobs(jobs)

        Log.i("Main","注册完成")
    }

    override fun onResume() {
        super.onResume()
        // 绑定服务
        cronJobManager.bindService()
    }

    override fun onPause() {
        super.onPause()
        //解绑服务
        cronJobManager.unbindService()
    }
}