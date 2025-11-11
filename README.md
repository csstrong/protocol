recv RTU compatible(数据接收兼容版)

> 环境配置

本项目所需要的第三方插件在docker环境中启动。

- maven安装
maven使用的版本为3.8.8

- docker安装
docker在本地安装，可自行在网络上下载最新版本进行安装。

- rabbitmq安装
```text  
1.拉取镜像。
docker pull rabbitmq
# 创建容器
docker run -di --name rabbitmq -p 4369:4369 -p 5671:5671 -p 5672:5672 -p 15671:15671 -p 15672:15672 -p 25672:25672 rabbitmq
2.进入容器并开启管理功能。
# 进入容器
docker exec -it rabbitmq /bin/bash
# 开启 RabbitMQ 管理功能
rabbitmq-plugins enable rabbitmq_management
　　访问：http://192.168.10.10:15672/ 使用 guest 登录账号密码拉取镜像。
3.访问：http://192.168.10.10:15672/ 使用 guest 登录账号密码
```
- redis安装
6379:6379

- portanier安装
- mongodb安装
3.11.0

- spring-boot:2.5.13
- netty:4.1.73.Final

> 运行流程

接受数据->解析数据->存储数据\
1.往消息队列里面放入假数据\
2.平台监控rabbitmq队列里面的数据，并获取数据，并进行解析\
3.解析完后，将数据按指定格式存入数据库