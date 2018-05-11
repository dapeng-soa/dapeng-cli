命令行工具下载 http://pms.today36524.com.cn:8083/basic-services/dapeng-cli

使用方式:
> cd dist  
> java -jar cli.jar

支持脚本方式调用;eg:  java -jar cli.jar service -l  

初始化zk启动 java -Dsoa.zookeeper.host=10.10.10.45:2181 -jar cli.jar service -l

命令使用说明:    
    
    1. zk 命令使用说明[zookeeper   节点相关的操作]  
     1.1  zk -get [path]                             获得zk某个节点的数据
     1.2  zk -set [path] -d [data]                   设置zk某个节点的数据
     1.3  zk -nodes [path]                           获取zk节点下的子节点列表
     1.4  zk -route [serviceName] -d [data]          设置zk服务的路由配置
     1.5  zk -route [serviceName] -f [path+filename] 设置本zk服务的路由配置(路由配置从本地文件读取) 
     
    2. set 命令使用说明[设置系统参数 目前主要支持设置 invocationContext、 zookeeper的zkHost.注意:通过set 指令设置的值， 在当前命令行生命周期有效]  
     1.1  set                     查看设置的信息
     1.2  set -timeout [value]    设置invocationContext 超时时间
     1.3  set -callermid [value]  设置invocationContext Callermid
     1.4  set -calleeip [value]   设置invocationContext calleeip
     1.5  set -calleeport [value] 设置invocationContext calleeport
     1.6  set -callerip [value]   设置invocationContext callerip
     1.7  set -zkhost [value]     设置 zkhost
     1.8  set -callerfrom [value] 设置invocationContext callerfrom
     
    3. unset 命令使用说明[撤销 set 指令的赋值]  
     3.2  unset -timeout [value]    设置invocationContext 超时时间
     3.3  unset -callermid [value]  设置invocationContext Callermid
     3.4  unset -calleeip [value]   设置invocationContext calleeip
     3.5  unset -calleeport [value] 设置invocationContext calleeport
     3.6  unset -callerip [value]   设置invocationContext callerip
     3.7  unset -zkhost [value]     设置 zkhost
     3.8  unset -callerfrom [value] 设置invocationContext callerfrom
     
    4. metadata 命令使用说明[获取服务接口的元数据]  
      4.1 metadata -s [serviceName] -v [version]                     获取服务接口的元数据信息(结果直接打印在控制台)
      4.2 metadata -s [serviceName] -v [version] -f [path+fileName]  获取服务接口元数据信息并保存到指定路径
      
    5. json 命令使用说明[获取服务调用的json格式样例]  
      5.1 json -s [serviceName] -v [version] -m [method]                    获取服务调用的json格式样例在控制台打印;eg:  json -s com.today.api.order.service.OrderService -m queryOrderList -v 1.0.0
      5.2 json -s [serviceName] -v [version] -m [method] -f [path+filename] 获取服务调用的json格式样例,保存到指定文件
      
    6. service 命令使用说明[获取当前运行时实例的服务列表]  
      6.1 service -l    获取当前运行时实例的服务列表;eg: service -l
      
    7. method 命令使用说明[获取服务接口的方法列表]  
      7.1 method -s [serviceName:version]    获取服务接口的方法;eg: method -s com.today.api.order.service.OrderService:1.0.0
      
    8. request 命令使用说明[请求服务接口]  
      8.1 request -s [serviceName] -v [version] -m [method] -f [path+fileName]   请求服务接口
          -s serviceName   服务名
          -v version       版本号
          -m method        方法名
          -f fileName      请求的json格式文件    
      8.2 request -metadata [serviceName version] 请求服务接口的元数据;eg: request -metadata com.today.api.order.service.OrderService 1.0.0
      8.3 request -metadata [serviceName version] -f [path+fileName]请求服务接口的元数据,并保存到指定文件

    9. help 命令使用说明[通过  help cmd 可以查看命令使用指南]  
      9.1 help     查看所有指令的用法
      9.1 help cmd 查看某个指令的详细用法

    
