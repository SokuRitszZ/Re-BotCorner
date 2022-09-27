# BotCorner-简单AI博弈小站

> 前后端全栈项目
>
> Author: SokuRitszZ (Andrew Leung) （个人全栈）



## BotCorner 是什么？

一个仿照[北京大学Botzone](https://www.botzone.org.cn/)的传统博弈游戏对战平台，可以与其他人建立游戏对战。对战时可以自己手动操作，也可以编写一个Bot程序来帮助你做出决策，与其他人或者其他Bot角力，还可以保存对局录像回放分析。



主站：[https://app3495.acapp.acwing.com.cn/](https://app3495.acapp.acwing.com.cn/)

AcApp端：[https://www.acwing.com/file_system/file/content/whole/index/content/6745477/](https://www.acwing.com/file_system/file/content/whole/index/content/6745477/)



## 功能

- 游戏对战（人 vs 人 / AI vs 人 / AI vs AI / 自我测试）
- Bot 增删改查
- 对局交流



## 技术栈

### 前端

主要技术：

- Vue 3 前端框架
- Vite 2 项目管理
- Bootstrap 组件库
- Pinia 2 状态管理
- Vue-Router 路由管理

依赖库：

- Monaco Editor 代码编辑器
- animate.css 动画效果
- ajax 网络请求



### 后端

主要技术：

- SpringBoot 微服务 / Web框架
- MySQL 数据库服务
- Redis 非关系数据库服务
- Docker 代码运行沙箱
- Django 微服务 / Web框架

依赖服务：

- AcWing API 支持AcWing登录



## 目录结构

- re-botcorner-acapp acapp端
- re-botcorner-backend 后端
  - backend 主后端
  - matching-system 匹配系统微服务
  - running_system 代码运行微服务
- re-botcorner-frontend 前端
  - public 公共静态资源
  - src
    - assets 静态资源
    - components 自定义组件
    - routes 路由
    - script js脚本
      - games 前端游戏逻辑
    - store 全局状态管理
    - templateBotCode 代码模板
    - view 视图
      - viewsChild 子视图
    - App.vue 主页面
    - main.js 主文件
    - package.json 项目管理
    - vite.config.js vite配置