[toc]

> 对象存储介绍:对象存储服务是云厂商提供的一种海量、安全、低成本、高可靠的云存储服务，适合存放任意类型的文件。容量和处理能力弹性扩展，多种存储类型供选择，全面优化存储成本。方便业务在任何时间、任何地点、任何互联网设备上进行上传和下载数据。



### 简介

   **Lucian（中文名卢锡安）是一款开源的跨平台网站管理工具**

不同于市面上其他远控工具，虽然支持各项协议的远控如dns、https、tcp、smtp层出不穷，但是一、各种对Cobra Strike 、冰蝎、恶意dns的流量检测技术逐步完善，从协议的隐蔽性和对抗性来说还远远不够；二、云环境下的渗透测试的目标环境会设置严格的安全组或者在vpc内，不能对外出入流量；三、出入的流量均会经过云安全厂商的检测，存在暴露风险。这款新工具的特点在于：

1. 基础http协议基于云厂家内部的对象存储，绕过对外连接的安全检测机制
2. 对象存储支持全球网络，速度快，在使用内部的endpoint时可以无对外流量产生
3. 使用云bucket作为数据和命令的跳板作为中转，无需cc服务器，保护安全测试人员

​    适配国内外主流云厂家的应用场景，已经实现的功能有：

1. 支持windows、linux、mac系统，只要系统上有jdk1.6到jdk12均可运行
2. 支持多种对象存储协议：亚马逊S3\阿里云OSS\腾讯COS
3. 内存编译内存运行，无payload文件落地
4. 全链路https、数据通过Head和put协议走header包发送，隐蔽性高
5. 支持java调用jni方式执行shell（慎用，不兼容的情况下会导致jvm崩溃从而掉线）
6. 上线主机数量无限制，后台支持操作命令详细记录

### 使用介绍：

1. 安全测试工程师在靶机上编译和运行加载器SennaLoader，当然也可以直接执行java命令运行已编译好的class或者jar文件。

   ![执行运行加载器](https://tva1.sinaimg.cn/large/007S8ZIlly1gjh7f3kmjnj30vw09ogow.jpg)

2. 加载器从远端下载payload文件Lucian，进行实时内存编译和内存运行（这时候远端的payload文件可以销毁了~）

![lucian文件无文件后缀要求，内容为已经正确配置对象存储上线地址](https://tva1.sinaimg.cn/large/007S8ZIlly1gjh761gng7j31tp0u0x3i.jpg)

3. 自动化执行随机间隔心跳和响应命令的功能，启动上线

   ![在对象存储侧显示已经成功上线](https://tva1.sinaimg.cn/large/007S8ZIlly1gjh3ymsqc5j31mw07q0uc.jpg)

4. 安全测试工程师配置相应的对象存储地址

   ![已经正确配置对应的对象存储上线地址](https://tva1.sinaimg.cn/large/007S8ZIlly1gjh42huilqj30wv0u0ai2.jpg)

5. 从远端读取上线主机列表，下发命令、Lucian获取到要执行的命令，执行上报结果

![image-20201007234031698](https://tva1.sinaimg.cn/large/007S8ZIlly1gjh7514k01j31y40kkndn.jpg)

6. 安全测试完成，执行-k命令，进行卸载删除,程序自毁

   ![-k指定需要删除的主机id](https://tva1.sinaimg.cn/large/007S8ZIlly1gjh48csapfj31bw0c8myk.jpg)

   ![java和class文件均已删除](https://tva1.sinaimg.cn/large/007S8ZIlly1gjh6g9t1wqj30um0aggpn.jpg)