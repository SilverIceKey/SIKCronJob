# SIKCronJob

**SIKCronJob** 是一个基于 Kotlin 的定时任务调度框架，允许通过注解轻松定义和管理定时任务。它支持通过 AIDL 进行进程间通信，确保任务在 Android 系统中被可靠调度和执行。

## 功能特性

- 通过注解 `@CronJob` 简单定义定时任务。
- 支持任务执行的定时间隔和初始延迟配置。
- 使用 AIDL 实现进程间通信，确保任务调度的稳定性。
- 支持任务在主线程或后台线程中执行。
- 支持服务与应用生命周期的绑定管理。

## 项目结构

```css
SIKCronJob/
│
├── app/
│   ├── src/main/java/com/sik/cronjobsample/
│   │   └── MainActivity.kt
│
├── SIKCronJob/
│   ├── src/main/java/com/sik/cronjob/
│   │   ├── annotations/
│   │   │   └── CronJob.kt
│   │   ├── managers/
│   │   │   └── CronJobManager.kt
│   │   │   └── CronJobScanner.kt
│   │   ├── services/
│   │   │   └── TaskService.kt
│   │   ├── receivers/
│   │   │   └── CronJobCallback.kt
│   │   ├── tasks/
│   │       └── MyTask.kt
│   ├── src/main/aidl/com/sik/cronjob/
│       ├── ICronJobCallback.aidl
│       └── ITaskService.aidl
```

## 安装与使用

### 1. 添加依赖

在项目的setting.gradle或者root下的build.gradle中找到

```groovy
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

在app的build.gradle中进行依赖,版本：[![](https://jitpack.io/v/SilverIceKey/SIKCronJob.svg)](https://jitpack.io/#SilverIceKey/SIKCronJob)所有模块版本相同

```groovy
dependencies {
	implementation("com.github.SilverIceKey:SIKCronJob:Tag")
}
```

### 

### 2. 定义任务类

在你需要执行定时任务的类中，使用 `@CronJob` 注解来定义任务的间隔时间和初始延迟。例如，定义一个任务类：

```kotlin
package com.sik.cronjob.tasks

import com.sik.cronjob.annotations.CronJob

class MyTask {

    @CronJob(intervalMillis = 60000, initialDelay = 5000, runOnMainThread = true)
    fun performTask() {
        println("定时任务执行中...")
    }
}
```

### 3. 初始化 `CronJobManager` 并启动服务

在你的 `MainActivity` 或者 `Application` 类中初始化 `CronJobManager` 并绑定服务，同时扫描并注册任务：

```kotlin
package com.sik.cronjobsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sik.cronjob.managers.CronJobManager
import com.sik.cronjob.managers.CronJobScanner
import com.sik.cronjob.tasks.MyTask

class MainActivity : AppCompatActivity() {

    private lateinit var cronJobManager: CronJobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 CronJobManager
        cronJobManager = CronJobManager(this)

        // 绑定服务
        cronJobManager.bindService()

        // 注册并调度任务
        CronJobScanner.scanAndRegisterJobs(MyTask(), this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 解绑服务
        cronJobManager.unbindService()
    }
}
```

### 4. 编写服务与 AIDL 通信

确保 `TaskService.kt` 和 AIDL 接口文件位于正确的目录结构中。AIDL 文件在 `src/main/aidl` 下。`TaskService` 将负责管理和调度定时任务，并通过 AIDL 与主进程通信。

### 5. 配置 `AndroidManifest.xml`

确保在你的 `AndroidManifest.xml` 中注册了 `TaskService`，并根据需要配置服务为前台服务或后台服务：

```xml
<service
    android:name="com.sik.cronjob.services.TaskService"
    android:exported="false"
    android:process=":CronJobService"/>
```

### 6. 运行

启动应用，定时任务将按设置的间隔时间和初始延迟自动执行。

## 许可证

```
MIT License

Copyright (c) 2024 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

------

这样，你的项目已经被简洁地整理为一个可以轻松集成和使用的定时任务调度框架，并且使用了 **MIT** 许可证。

如果你需要进一步的修改或有任何问题，随时告诉我！