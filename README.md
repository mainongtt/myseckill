# myseckill 秒杀项目
## 代码结构
![image](https://user-images.githubusercontent.com/52461848/167248100-36d89c60-0da9-4ff2-bc5c-40dbde376ec2.png)    
1.  common包：存放一些常用工具类
2.  component包：自己注入的一些Bean    
3.  configuration包：存放一些配置类
4.  controller包：控制类  
5.  service包：业务层 
6.  dao 包: 数据库层  
7.  entity包：实体层
8.  rocket包：RocketMQ的一些代码   
## 运行环境
详见：环境搭建.pdf
1.  安装JDK，配置环境变量；    
2.  安装Maven，配置镜像服务器；
3.  配置数据库
## 功能
1.用户功能.md    
2.商品功能.md    
3.订单功能.md
## 项目部署
### 1.redis安装
yum list redis*     
yum install redis.x86_64     
vim /etc/redis.conf     
:set nu      
启动:redis-server /etc/redis.conf      
redis-cli -a nowcoder123     
whereis redis-cli     
查看进程号： ps -ef|grep redis     
cp /usr/bin/redis-cli /usr/local/bin/    
### 2.RockedMq安装
安装    
wget https://mirror-hk.koddos.net/apache/rocketmq/4.8.0/rocketmq-all-4.8.0-bin-release.zip    
unzip rocketmq-all-4.8.0-bin-release.zip    
chmod -R 777 rocketmq-all-4.8.0-bin-release    
配置    
cd /root/rocketmq-all-4.8.0-bin-release    
# ./bin/runserver.sh (82)    
-server Xms256m Xmx256m Xmn128m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m    
# ./bin/runbroker.sh (67)    
-server Xms256m Xmx256m Xmn128m    
# ./conf/broker.conf (追加)    
brokerIP1 = 139.9.119.64    
autoCreateTopicEnable = true    

启动     
# namesrv     
nohup sh ./bin/mqnamesrv -n localhost:9876 &    
tail -f /root/logs/rocketmqlogs/namesrv.log    
# broker     
nohup sh ./bin/mqbroker -n localhost:9876 -c ./conf/broker.conf &     
tail -f /root/logs/rocketmqlogs/broker.log    

测试    
export NAMESRV_ADDR=localhost:9876    
sh bin/tools.sh org.apache.rocketmq.example.quickstart.Producer    
sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer    

关闭     
sh ./bin/mqshutdown broker    
sh ./bin/mqshutdown namesrv    
