# **重要知会** #
  * Client端的Java的实现不再更新，只有Go实现gsnova会被更新；
  * Client端的Go实现目前在github上，[gsnova@github](https://github.com/yinqiwen/gsnova)
  * 服务端针对`Google AppEngine`的实现在这里 [snova-gae@github](https://github.com/yinqiwen/snova-gae)，正常情况下也只会更新Go实现
  * 服务端针对一般PaaS的实现在这里， [snova-c4@github](https://github.com/yinqiwen/snova-c4)，正常情况下也只会更新NodeJS实现
  * 由于工作以及业余开源重心在这个NoSQL项目[Ardb](https://github.com/yinqiwen/ardb)上（欢迎围观），Snova相关项目不会非常活跃，bug修复也可能无法及时修复，希望大家谅解；此外非常欢迎有兴趣的朋友参与开发接手。
  * 此项目另外有很多优秀的开源项目替代， 比如[goagent](https://code.google.com/p/goagent/)等，若本项目无法满足需求，可以尝试寻找一些优秀的开源实现。


# **关于** #
  * Snova(Java)是一个通用web proxy实现，包括server端和client端，目前包含基于`Google AppEngine`平台的GAE实现，支持`Heroku/CloundFoundry/OpenShift/Appfog/Dotcloud/Modulus/Jelastic`等Java/NodeJS平台的C4实现，以及增强的PAC实现SPAC（Special Proxy  Auto Config）。源码在`GoogleCode`上维护。[snova@GoogleCode](http://code.google.com/p/snova/source/checkout)
  * GSnova(Go)是几乎和snova功能一致的Go语言client实现，server端与snova通用；包括GAE/C4支持，SPAC（Special Proxy  Auto Config）实现，以及额外的SSH支持。源码放到了`GitHub`上维护. [gsnova@github](https://github.com/yinqiwen/gsnova)
  * 基于`Google AppEngine`平台服务端有Java和Go两种实现，源码放到了`GitHub`上维护. [snova-gae@github](https://github.com/yinqiwen/snova-gae)
  * 其它PaaS服务端(C4)有Java和NodeJS两种实现，源码放到了`GitHub`上维护. [snova-c4@github](https://github.com/yinqiwen/snova-c4)

# **安装部署** #
  * 安装运行依赖
    * Snova(Java) 依赖JRE 1.6+
    * GSnova(Go)为系统原生可执行文件，无任何依赖
    * 启动后默认绑定在127.0.0.1:48100上接受代理请求（默认有spac支持）
  * `Google Appengine平台`
    * 参考[GAEInstallation](http://code.google.com/p/snova/wiki/GAEInstallation)安装配置部署GAE到`Google Appengine`
    * GSnova(Go)启动后同时默认127.0.0.1:48101上接受代理请求（无spac支持）
  * `Java PaaS平台`(可选)
    * 参考[JavaPaaSInstallation](http://code.google.com/p/snova/wiki/JavaPaaSInstallation)安装配置部署C4到`Java PaaS`上。目前支持`Heroku/CloundFoundry/OpenShift/Appfog/Dotcloud/Jelastic`等
    * GSnova(Go)启动后同时默认127.0.0.1:48102上接受代理请求（无spac支持）
  * `Node.js PaaS平台`(可选)
    * 参考[NodeJsPaaSInstallation](http://code.google.com/p/snova/wiki/NodeJsPaaSInstallation)安装配置部署C4到`NodeJS PaaS`上。。目前支持`Heroku/CloundFoundry/OpenShift/Appfog/Dotcloud/Modulus`等
    * 此链接为较全的支持NodeJS的[PaaS Providers](https://github.com/joyent/node/wiki/Node-Hosting)，一般都可部署NodeJS版本
    * Snova/GSnova启动后同时默认127.0.0.1:48102上接受代理请求（无spac支持）
  * VPS(可选)
    * C4的服务端也可部署到VPS上，参考[C4VPSInstallation](http://code.google.com/p/snova/wiki/C4VPSInstallation)安装配置部署
  * SSH(可选)
    * 仅GSnova(Go)支持，暂无文档参考，请参考配置文件中注释帮助
    * GSnova(Go)启动后同时默认127.0.0.1:48103上接受代理请求（无spac支持）
  * SPAC(可选)
    * Snova(Java)用户请参考[SpecialProxyAutoConfig](http://code.google.com/p/snova/wiki/SpecialProxyAutoConfig)配置SPAC
    * GSnova(Go)用户请参考[SpecialProxyAutoConfigOnGSnova](http://code.google.com/p/snova/wiki/SpecialProxyAutoConfigOnGSnova)配置SPAC

# **[常见问题FAQ](http://code.google.com/p/snova/wiki/FAQ)** #

# **`GAE AppId`共享** #
  * 在GSnova(Go)的Web界面中提供有一个共享appid的功能，任何人可以据此共享自己的appid。访问[链接](http://127.0.0.1:48100/share.html)
  * 当Snova/GSnova的用户由于某些原因（如不知道怎么部署server侧）没有配置自己的appid时，默认情况下，客户端会从服务器上随机获取一个共享appid，然后用匿名用户方式连接`<shareappid>.appspot.com`

# **注意** #
  * Go版本GAE服务端与Java版本GAE服务端功能完全一致，区别在于Go版本的性能较Java版本为好，粗略观察差距大约在30%以上，另外Go实例启动时间很短（<500ms）,而Java实例的启动时间较长（>4s）
  * GSnova(Go)无GUI界面，只提供简单的本地Web界面支持。Snova(Java)提供GUI支持。
  * GSnova(Go)通过本地web界面提供share/unshare appid功能