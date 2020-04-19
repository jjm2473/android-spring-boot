## Running SpringBoot in Android
## 在 Android 中运行 SpringBoot
这个项目用于在 Android 中启动 SpringBoot 服务，也就是把 Android 设备当成服务器，
已在Android 9模拟器测试过

![浏览器截图](Screenshot_1587307289.png)


### TODO:
1. 扩展 [AndroidConfigurationClassPostProcessor.java](app/src/main/java/org/springframework/context/annotation/AndroidConfigurationClassPostProcessor.java) 以支持CGLIB代理 [<sup>1</sup>](https://github.com/zhangke3016/MethodInterceptProxy)
2. 尚未测试模板引擎


PS：spring官方的 `SpringBoot for android` 是在服务器部署RESTful接口给Android调用
