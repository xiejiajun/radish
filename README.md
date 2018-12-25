# radish

#### 项目介绍
分布式任务抢占及系统监控服务。

适用于中小微企业，将系统任务独立部署，统一管理。区别与传统的嵌入在系统中的任务， 可以很好的解耦任务服务。

具有以下优势：

1. 方便灵活的配置系统和强大的容错重试以及报警机制，可以确保任务的正常完成。目前已嵌入SendCloud邮件发送系统， 用户只需要简单的配置SendCloud的账号即可直接使用
2. 友好的管理控制台，能实时监控任务的进展。
3. 灵活的任务调度系统，可用配置多种任务类型（定时任务， 手动执行任务， 依赖链任务等等……）
4. 完善的客户端监控服务，可用帮助实时监控客户端的机器性能。可单独作为服务器监控系统使用。


#### 软件架构
![系统时序图](https://github.com/shensuoyao/radish/raw/master/doc/screenshots/任务系统时序图.jpg)
![系统架构图](https://github.com/shensuoyao/radish/raw/master/doc/screenshots/系统架构设计.jpg)


#### 安装教程

1. 下载source, 根据自己的Mysql环境和Redis环境修改配置文件, 并打包.
2. 初始化数据库
3. 配置好redis数据库
4. 启动scheduler服务端
5. 如有需要, 配置好网络环境, 在客户机上启动Agent客户端
6. 服务安装好后可以直接使用监控模块, 并内置支持Shell 和 Python脚本任务

#### Agent开发使用

1. 普通java项目
普通的java开发只需要在java项目中添加jar包的依赖，具体代码启动示例如下：
>public static void main(String[] args) {
int nettyPort = 8084;
  // 初始化agent监听程序
  ScriptHandler scriptHandler = new ScriptHandler();
  RadishAgent radishAgent = new RadishAgent(Collections.singletonList(scriptHandler));
  radishAgent.setShPath("/tmp/log/radish");
  radishAgent.setLogPath("/tmp/log/radish");
  radishAgent.setScheduingServer("http://127.0.0.1:8888/radish-scheduing");
  AgentInfo agentInfo = new AgentInfo();
  agentInfo.setAgentName("java-test");
  agentInfo.setAgentIp("127.0.0.1");
  agentInfo.setAgentPort(8083);
  agentInfo.setNetwork("netty");
  agentInfo.setNettyPort(nettyPort);
  radishAgent.setAgentInfo(agentInfo);
  radishAgent.start();
  // 由于是java启动，没有web容器，因此需要启动netty监听程序，用于server读取agent日志
  HandlerLogNettyServer.getInstance(nettyPort).start();
}
注：输出日志需要添加logback.xml配置文件，jar包最好引用radish-core-1.0.1-jar-with-dependencies.jar，这是把radish-core本身依赖的jar已经整体打包，不需要额外添加。
2. 基于spring boot应用开发
这是比较推荐的一种开发方式，可以分为以下4个步骤：
> 1. 将radish-core-1.0.1.jar和radish-spring-boot-starter-1.0.1.jar这2个jar包导入maven本地仓库中，mvn install:install-file -Dfile=本地jar包文件路径 -DpomFile=本地pom文件路径。
> 2. 新建spring boot项目，添加如下maven依赖：
> &lt;dependencies&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;dependency&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;spring-boot-starter-web&lt;/artifactId&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;2.1.1.RELEASE&lt;/version&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;/dependency&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;dependency&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;org.sam.shen&lt;/groupId&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;radish-spring-boot-starter&lt;/artifactId&gt;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;1.0.1&lt;/version&gt;
&nbsp;&nbsp;&nbsp;&nbsp; &lt;/dependency&gt;
&lt;/dependencies&gt;
> 3. application.yml配置文件示例如下：
server:
&nbsp;&nbsp;port: 8083
&nbsp;&nbsp;servlet:
&nbsp;&nbsp;&nbsp;&nbsp;context-path: /${spring.application.name}
spring:
&nbsp;&nbsp;application:
&nbsp;&nbsp;name: radish-agent
radish:
&nbsp;&nbsp;agent:
&nbsp;&nbsp;# agent服务器的ip地址
&nbsp;&nbsp;&nbsp;&nbsp;ip: 127.0.0.1
&nbsp;&nbsp;# 此端口同server.port
&nbsp;&nbsp;&nbsp;&nbsp;port: 8083
&nbsp;&nbsp;# agent服务器名称，自行定义
&nbsp;&nbsp;&nbsp;&nbsp;name: test
&nbsp;&nbsp;# 存放日志的路径
&nbsp;&nbsp;&nbsp;&nbsp;logpath: /tmp/log/radish
&nbsp;&nbsp;# 存放脚本的路径
&nbsp;&nbsp;&nbsp;&nbsp;shpath: /tmp/log/radish
scheduler:
&nbsp;&nbsp;# 连接任务调度中心的地址
&nbsp;&nbsp;server: http://192.168.140.146:8888/radish-scheduing
&nbsp;&nbsp;# 设置agent日志访问的模式，目前仅支持servlet和netty两种
&nbsp;&nbsp;log-view-mode: servlet
&nbsp;&nbsp;# 当log-view-mode为netty时需要设置如下参数，推荐使用servlet
&nbsp;&nbsp;log-view-netty:
&nbsp;&nbsp;port: 8084

#### 参与贡献

1. Fork 本项目
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request