# FinalSpeed
FinalSpeed是高速双边加速软件,可加速所有基于tcp协议的网络服务,在高丢包和高延迟环境下,仍可达到90%的物理带宽利用率,即使高峰时段也能轻松跑满带宽.

### 安装教程
[客户端安装说明](https://www.91yun.org/archives/615)
<br />
[服务端安装说明](http://www.91yun.org/archives/2775)

### 说明


finalspeed作者开始卖收费版了，所以停止了免费版的更新，并且删除了所有代码。不过还好我fork了一份。。作为Openvz的救星，还是有不少人对finalspeed有需求的。所以我就做了这个一键安装包。

这个一键安装包完全重写了作者原来的安装代码，启动，停止代码。并加入了服务，可以使用 service finalspeed star | stop 来控制，加入了开机启动启动。总之，你需要做的就是真正的一键。一键安装包安装的服务器端版本为1.2.需要1.0的自行到github下载。欢迎大家测试，有问题及时留言给我。

github地址：https://github.com/91yun/finalspeed

github里也有服务器端和客户端的文件。大家有需求可以自行去下载。


### 一键安装代码：
```
wget -N --no-check-certificate https://raw.githubusercontent.com/91yun/finalspeed/master/install_fs.sh && bash install_fs.sh
```
###一键卸载代码
```
wget -N --no-check-certificate https://raw.githubusercontent.com/91yun/finalspeed/master/install_fs.sh && bash install_fs.sh uninstall
```
### finalspeed操作命令

启动： /etc/init.d/finalspeed start

停止命令：/etc/init.d/finalspeed stop

状态命令（查看日志）：/etc/init.d/finalspeed status

### finalspeed安装路径

安装路径： /fs/

日志路径：/fs/server.log

