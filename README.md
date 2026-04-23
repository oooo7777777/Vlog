# 日志系统框架

VLog 是一个基于mmap， 高性能，高可用的，无丢失的，简单易用的日志系统框架。
sdcard/Android/data/
## 特性介绍  

| 特性|简介|
| ------ | ------ |
|自定义日志保存路径 |默认保存在Android/data/com.xxxx.xxxx/files/log中|
|自定义日志缓存路径|默认保存在Android/data/com.xxxx.xxxx/files/cache中|
|自定义日志最大容量|默认70M，超过最大容量就不再写入|
|日志按天保存，自定义日志保存天数|默认保存最近7天的日志，超过七天的日志将会被删除|
|自定义日志加密方式|默认不加密，可以实现LogEncrypt接口，实现自定义的加密方式|
|日志记录通用信息|在日志系统初始化的时候，会一并记录App版本，手机型号等信息，网络切换的时候还会记录网络类型|
|日志格式为CSV格式，方便后端解析展示|写日志的时候，会同时写入当前时间，线程ID，线程的名字，Log等级，以及当前所在类和方法的名字|
|支持控制台打印日志信息|在写入日志到本地的同时，也会在控制台输出相应的日志信息|
|全新优化的日志展示体验，更清晰直观|
|无需连接设备即可实时查看日志|
|支持通知栏快速进入日志页|
|支持按关键字和日志等级筛选|
|不同日志等级使用不同颜色区分，信息识别更直观|
|列表支持长按复制，点击可查看日志详情|
|详情页支持完整内容查看、复制和分享|
|分享能力优化为文本文件导出，便于外部打开和转发|
|支持一键快速切换到列表顶部或底部，长列表浏览更方便|

### 优美的日志输出
![img1](./app/src/main/res/drawable/img1.png)

### 无需连接设备即可实时查看日志
![img2](./app/src/main/res/drawable/img2.png)
![img3](./app/src/main/res/drawable/img3.png)
![img4](./app/src/main/res/drawable/img4.png)
![img5](./app/src/main/res/drawable/img5.png)

### 历史日志查看与回溯
![img6](./app/src/main/res/drawable/img6.png)


## 添加依赖

1. 在项目根目录的build.gradle 中添加:

   ```groovy
   	allprojects {
   		repositories {
   			...
   			maven { url 'https://jitpack.io' }
   		}
   	}
   ```

2. 添加依赖

   ```groovy
   dependencies {
   	        implementation 'com.github.oooo7777777:Vlog:2.0.9'
   	}
   ```

## 初始化
```
//初始化日志系统
 VLog.init(VLogConfig(this, BuildConfig.DEBUG, true))
```

## 开启通知栏日志入口
添加通知栏权限
```
uses-permission android:name="android.permission.POST_NOTIFICATIONS"
```

```java
VLog.init(
        new VLogConfig(this)
                .setEnableLogInspector(true)
);
```

如果需要在运行时动态开启或关闭通知栏日志入口，可以直接调用：

```java
VLog.setLogInspectorEnabled(true);  // 开启
VLog.setLogInspectorEnabled(false); // 关闭
```

## 打印日志(推荐使用kotlin方法)

```
kotlin
"hello VLog".logI()

java
LogExtKt.log("hello VLog");
```

## 立即写入到文件，在上传日志的时候调用

```
VLog.flush();
```

