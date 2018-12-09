# exportITeyeBlog
* **文档日期**  
&emsp;&emsp;&emsp;&emsp;2018-12-09
* **项目描述**   
&emsp;&emsp;&emsp;&emsp;迁移iteye的博客，导出博文，生成Markdown文件。
* **项目由来**  
&emsp;&emsp;&emsp;&emsp;我的Blog一直保存在<https://www.iteye.com/>，写了好多年，主要是记录工作中的一些经验。虽然这些Blog的内容外人看着没什么价值，但是对于我来说却是一笔宝贵的财富。  
&emsp;&emsp;&emsp;&emsp;最近发现iteye时常不稳定，而且它在博客中插入的广告也倍增，有些功能根本404无法使用，所以很担心它未来出现什么问题我积累多年的blog的内容丢失，所以就写了一个基于Jsoup的小程序，导出自己的iteye的博文生成Markdown文件进行备份。
<br><br>
* **注意事项**  
    * application-dev.yml  
        iteyeblog.host: 配置iteye博客的地址，如：http://xxx.iteye.com  
        iteyeblog.cookies：登录iteye之后网站设置的cookie。  
        iteyeblog.blogsavepath：本地存放导出的blog生成Markdown文件的位置   
        iteyeblog.sleeptime: 休眠时间的随机部分  
        iteyeblog.basesleeptime: 休眠时间的基础部分
        
<pre>
1、cookies字符串说明:
可用使用自己的iteye的用户名密码登录系统,访问自己博文的时候可用在浏览器的控制台找到cookie的字符串。
举例：使用Chrome浏览器访问iteye中自己的博文，查看该URL，在这个URL的“Response-Headers”里面有一个字段“set-cookie”，将它的内容放在这里就可以使用。
2、解决"反高频爬取页码"的功能：
因为网站<a href="https://www.iteye.com" target="_blank">https://www.iteye.com</a>有"反高频爬取页码"的功能，所以需要降低访问网站的频率，所以这里会让程序随机的休眠一段时间，通过参数：iteyeblog.sleeptime和iteyeblog.basesleeptime实现。
</pre>
