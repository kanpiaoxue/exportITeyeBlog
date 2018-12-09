package org.kanpiaoxue.exportiteyeblog.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kanpiaoxue.exportiteyeblog.bean.ITeyeBlogArticle;
import org.kanpiaoxue.exportiteyeblog.service.BlogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

/**
 * BlogServiceImpl.java
 * 
 * @author kanpiaoxue
 * @version 1.0
 * @Create Time 2018/12/05 18:52:02
 * @Description: 博客导出实现类
 *               CSS 选择器参考手册:
 *               http://www.w3school.com.cn/cssref/css_selectors.ASP
 */
@Service
public class BlogServiceImpl implements BlogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogServiceImpl.class);
    private static final int KEY_VALUE_SIZE = 2;

    private static final int NORMAL_EXIST_CODE = 0;
    private static final int ERROR_EXIST_CODE = -1;

    private static final Pattern TIME_PART_PATTERN = Pattern.compile("^(\\d+?)\\s(.*)$");
    private static final DateTimeFormatter YYYY_MM_DD_DATE_TIME_FORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final String[] searchList;
    private static final String[] replacementList;
    private static final Map<String, String> specialCharactorMap = Maps.newHashMap();
    static {
        specialCharactorMap.put("？", "");
        specialCharactorMap.put("?", "");
        specialCharactorMap.put("：", "_");
        specialCharactorMap.put(":", "_");
        specialCharactorMap.put("“", "");
        specialCharactorMap.put("”", "");
        specialCharactorMap.put("\"", "");
        specialCharactorMap.put("！", "");
        specialCharactorMap.put("￥", "");
        specialCharactorMap.put("……", "");
        specialCharactorMap.put("；", "");
        specialCharactorMap.put("（", "(");
        specialCharactorMap.put("）", ")");
        specialCharactorMap.put("，", "_");
        specialCharactorMap.put("。", "");
        specialCharactorMap.put("、", "_");
        specialCharactorMap.put("/", "_");
        specialCharactorMap.put("\\", "_");
        specialCharactorMap.put(" ", "_");
        specialCharactorMap.put("__", "_");
        List<String> searchs = Lists.newArrayList();
        List<String> replacements = Lists.newArrayList();
        specialCharactorMap.entrySet().forEach(entry -> {
            searchs.add(entry.getKey());
            replacements.add(entry.getValue());
        });
        searchList = searchs.toArray(new String[] {});
        replacementList = replacements.toArray(new String[] {});
    }

    @Value("${iteyeblog.host}")
    private String host;
    @Value("${iteyeblog.useragent:\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36\"}")
    private String userAgent;
    @Value("${iteyeblog.blogsavepath}")
    private String blogSavePath;

    @Value("${iteyeblog.startpagenumber:1}")
    private Integer startPageNumber;
    @Value("${iteyeblog.endpagenumber: #{T(java.lang.Integer).MAX_VALUE}}")
    private Integer endPageNumber;

    /**
     * sleepTime 和 baseSleepTime 两个时间经过测试是可用的。
     * 如果系统爬取博客文章被iteye阻拦，可用适当的扩大调整这个数字。
     * 这2个数字越大，爬取博客的速度越慢。
     */
    @Value("${iteyeblog.sleeptime:20}")
    private Integer sleepTime;

    @Value("${iteyeblog.basesleeptime:30}")
    private Long baseSleepTime;

    /**
     * cookie的字符串得到的方法说明：
     * 可用使用自己的iteye的用户名密码登录系统，访问自己博文的时候可用在浏览器的控制台找到cookie的字符串。
     * 举例：使用Chrome浏览器访问iteye中自己的博文，查看该URL，在这个URL的“Response-Headers”里面有一个字段“set-cookie”，将它的内容放在这里就可以使用。
     */
    @Value("${iteyeblog.cookies}")
    private String cookiesString;

    /*
     * (non-Javadoc)
     * @see org.kanpiaoxue.exportiteyeblog.service.BlogService#exportBlogs()
     */
    @Override
    @PostConstruct
    public void exportBlogs() throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        LOGGER.info("start to exportIteyeBlogs.");
        Map<String, String> cookies = createCookies();
        List<ITeyeBlogArticle> count = Lists.newArrayList();
        IntStream.rangeClosed(startPageNumber, endPageNumber).forEach(pageNum -> {
            try {
                // 根据分页的页码分析页码中的博客的标题、URL地址和发布时间
                List<ITeyeBlogArticle> articles = pagingURLs(pageNum, cookies);
                tryExistSystem(articles);
                // 跳过已经存在的博客文章，不重复导出
                List<ITeyeBlogArticle> distinctArticles = filterExistedFile(articles);
                // 导出博客文章
                exportBlogs(distinctArticles, cookies);
                count.addAll(articles);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                if (e instanceof org.jsoup.HttpStatusException) {
                    LOGGER.error("{} prevent crawling the page. System will exit with errorCode:{}", host,
                            ERROR_EXIST_CODE);
                    System.exit(ERROR_EXIST_CODE);
                }
            }
        });
        LOGGER.info("finish exportIteyeBlogs. blog articles count:{}. it consumes {}", count.size(), sw);
    }

    /**
     * @param article
     * @return
     * @author kanpiaoxue
     *         Create Time 2018年12月8日 下午10:01:53
     *         Description : 创建博客正文
     */
    private String buildBlogContent(ITeyeBlogArticle article) {
        StringBuilder builder = new StringBuilder();
        // 博客标题
        builder.append("#").append(article.getBlogTitle()).append("\n");
        // 博客的发布时间
        builder.append("###发表时间：")
                .append(article.getPublicationTime().toString(YYYY_MM_DD_DATE_TIME_FORMATTER)).append("\n");
        // 博客的分类
        builder.append("###分类：").append(article.getBlogCategorys()).append("\n");
        // 博客的iteye的原始地址
        builder.append("###iteye原始地址：<a href=\"").append(article.getOriginalUrl())
                .append("\" target=\"_blank\">").append(article.getOriginalUrl()).append("</a>\n");
        builder.append("\n");
        // 切割博客正文
        builder.append("---");
        builder.append("\n\n");
        // 写入博文
        builder.append(article.getBlogBody());
        return builder.toString();
    }

    /**
     * @param blogContentEditorBodyDoc
     * @return
     * @author kanpiaoxue
     *         Create Time 2018年12月8日 下午9:46:23
     *         Description : 构建历史非常久远的blog的博文。之前的博文的HTML布局和现在的博文的HTML布局不一致。
     */
    private String buildOldBlogContent(Document blogContentEditorBodyDoc) {
        return blogContentEditorBodyDoc.body().children().stream().map(Element::outerHtml)
                .collect(Collectors.joining("\n"));
    }

    private Map<String, String> createCookies() {
        LOGGER.trace("start to createCookies with cookiesString:{}", cookiesString);
        Preconditions.checkArgument(StringUtils.isNotBlank(cookiesString), "cookiesString is null or empty!");
        List<String> cookieList =
                Splitter.on(";").trimResults().omitEmptyStrings().splitToList(cookiesString);
        Map<String, String> cookies = Maps.newHashMap();
        for (String cookieKeyValue : cookieList) {
            List<String> kv = Splitter.on("=").trimResults().omitEmptyStrings().splitToList(cookieKeyValue);
            if (CollectionUtils.isNotEmpty(kv) && kv.size() == KEY_VALUE_SIZE) {
                cookies.put(kv.get(0), kv.get(1));
            }
        }
        LOGGER.trace("finish createCookies with cookiesString:{}. cookies:{}", cookiesString, cookies);
        return cookies;
    }

    private Document createHTMLDocument(String url, Map<String, String> cookies) throws IOException {
        int timeoutMillis = (baseSleepTime.intValue() + sleepTime.intValue()) * 10 * 1000;
        return Jsoup.connect(url).timeout(timeoutMillis).cookies(cookies).userAgent(userAgent).get();
    }

    private void createMackDownDocumentFile(ITeyeBlogArticle article) throws IOException {
        // 创建博客正文
        String blogContent = buildBlogContent(article);

        File file = new File(article.getMarkdownFileName());
        if (!file.getParentFile().exists()) {
            // 创建目录结构
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        Files.asCharSink(file, Charsets.UTF_8, FileWriteMode.APPEND).write(blogContent);
    }

    private String createMarkdownFileName(ITeyeBlogArticle article) {
        String title = processSpecilCharacters(article.getBlogTitle());
        String publicationTimeString = article.getPublicationTime().toString(YYYY_MM_DD_DATE_TIME_FORMATTER);
        /*
         * jekyll:文件格式很重要，必须要符合: YEAR-MONTH-DAY-title.MARKUP
         * 2007-10-29-why-every-programmer-should-play-nethack.md
         */
        StringBuilder builder = new StringBuilder(blogSavePath);
        builder.append(File.separator);
        builder.append(publicationTimeString);
        builder.append(File.separator);
        builder.append(publicationTimeString);
        builder.append("-");
        builder.append(title);
        builder.append(".md");
        String fileName = builder.toString();
        LOGGER.debug("create originFileName:{}", fileName);
        return fileName;
    }

    private DateTime createPublicationTime(String dateText) {
        dateText = dateText.trim();
        Matcher timePartMatcher = TIME_PART_PATTERN.matcher(dateText);
        DateTime now = DateTime.now();
        DateTime publicationTime = null;
        if (timePartMatcher.matches()) {
            String timePart = timePartMatcher.group(2).trim();
            if ("小时前".equals(timePart)) {
                publicationTime = now.hourOfDay().addToCopy(0 - Integer.valueOf(timePartMatcher.group(1)));
            } else if ("分钟前".equals(timePart)) {
                publicationTime = now.minuteOfDay().addToCopy(0 - Integer.valueOf(timePartMatcher.group(1)));
            }
        } else if ("刚刚".equals(dateText)) {
            publicationTime = now;
        } else if ("昨天".equals(dateText)) {
            publicationTime = now.dayOfYear().addToCopy(0 - 1);
        } else if ("前天".equals(dateText)) {
            publicationTime = now.dayOfYear().addToCopy(0 - 2);
        } else {
            publicationTime = DateTime.parse(dateText, YYYY_MM_DD_DATE_TIME_FORMATTER);
        }
        return publicationTime;

    }

    private void exportBlogs(List<ITeyeBlogArticle> articles, Map<String, String> cookies) {
        articles.stream().forEach(article -> {
            LOGGER.info("start to exportBlog:{}", article);
            try {
                // 解析博客正文
                ITeyeBlogArticle newArticle = parsePage(article.clone(), cookies);
                LOGGER.info("newArticle:{}", newArticle);
                // 创建博客的markdown的文件
                createMackDownDocumentFile(newArticle);
                // 开始休眠，规避网站的反作弊功能
                sleepTime();
            } catch (Exception e) {
                LOGGER.error(String.format("newArticle:%s,msg:%s", article, e.getMessage()), e);
            }
        });
    }

    private List<ITeyeBlogArticle> filterExistedFile(List<ITeyeBlogArticle> articles) {
        return articles.stream().filter(article -> {
            // 过滤掉已经导出的存在的博客文件
            boolean exist = new File(article.getMarkdownFileName()).exists();
            if (exist) {
                LOGGER.info("{} had already existed. it will be skipped!", article.getMarkdownFileName());
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * @param pageNum
     *            当前分页的页码
     * @param cookies
     * @return 当前分页中的blog的信息：URL地址、blog的标题、markdown的文件地址
     * @throws Exception
     * @author kanpiaoxue
     *         Create Time 2018年12月8日 下午8:30:07
     *         Description : 得到当前分页的blog的信息
     */
    private List<ITeyeBlogArticle> pagingURLs(int pageNum, Map<String, String> cookies) throws Exception {
        String url = String.format("%s/admin/blogs?page=%s", host, pageNum);
        LOGGER.info("pagingURL:{}", url);
        Document doc = createHTMLDocument(url, cookies);
        LOGGER.trace("pagingURL:{}-->{}", url, doc.body());
        Elements trElements = doc.select(".admin tbody tr");
        if (CollectionUtils.isEmpty(trElements)) {
            LOGGER.info("pagingURL:{} is the end page of the {} blog.", url, host);
            return Lists.newArrayList();
        }

        List<ITeyeBlogArticle> list = trElements.stream().map(tr -> {
            Elements td = tr.select("td");
            Element anchorElement = td.get(0).selectFirst("a");
            String ancherUrl = anchorElement.absUrl("href");
            String title = anchorElement.attr("title");
            String dateText = td.get(1).text();
            ITeyeBlogArticle o = new ITeyeBlogArticle();
            // 设置博客标题
            o.setBlogTitle(title);
            // 设置博客的链接地址
            o.setOriginalUrl(ancherUrl);
            // 设置博客的发布时间
            DateTime publicationTime = createPublicationTime(dateText);
            Preconditions.checkNotNull(publicationTime, "tr html:%s", trElements);
            o.setPublicationTime(publicationTime);
            // 设置博客的Markdown的文件地址
            String markdownFileName = createMarkdownFileName(o);
            o.setMarkdownFileName(markdownFileName);
            return o;
        }).collect(Collectors.toList());

        return list;
    }

    /**
     * @param article
     * @param cookies
     * @return
     * @throws Exception
     * @author kanpiaoxue
     *         Create Time 2018年12月8日 下午9:57:08
     *         Description : 解析博客的正文，得到：博客正文、博客的分类标签
     */
    private ITeyeBlogArticle parsePage(ITeyeBlogArticle article, Map<String, String> cookies)
            throws Exception {
        String url = String.format("%s/edit", article.getOriginalUrl());
        LOGGER.info("start parsePage. url:{}", url);
        Document doc = createHTMLDocument(url, cookies);
        // 得到博客分类标签列表
        Element blogCategoryListElement = doc.getElementById("blog_category_list");
        String blogCategorys = blogCategoryListElement.val();
        article.setBlogCategorys(blogCategorys);
        // 得到博客的博文内容
        Element blogContentEditorBodyElement = doc.getElementById("editor_body");
        String blogContent = blogContentEditorBodyElement.val();
        // 将博文解析成HTML对象，方便后续解析
        Document blogContentEditorBodyDoc = Jsoup.parse(blogContent);
        Element contentDiv = blogContentEditorBodyDoc.body().selectFirst(".iteye-blog-content-contain");

        // 对于非常久远的blog内容： 20120831 之前的内容，iteye的博客页面HTML发生了变化，这里进行处理。
        String bodyString = Objects.nonNull(contentDiv) ? contentDiv.outerHtml()
                : buildOldBlogContent(blogContentEditorBodyDoc);
        article.setBlogBody(bodyString);
        return article;
    }

    /**
     * @param str
     * @return
     * @author kanpiaoxue
     *         Create Time 2018年12月8日 下午9:03:56
     *         Description : 处理字符串中的特殊字符
     */
    private String processSpecilCharacters(String str) {
        String rs = StringUtils.replaceEachRepeatedly(str.trim(), searchList, replacementList);
        return rs;
    }

    /**
     * @throws InterruptedException
     * @author kanpiaoxue<br>
     *         Create Time 2018年12月8日 下午9:51:25<br>
     *         Description :
     *         因为网站（https://www.iteye.com/）有"反高频爬取页码"的功能，所以需要降低访问网站的频率，所以这里会让程序随机的休眠一段时间
     */
    private void sleepTime() throws InterruptedException {
        Random r = new Random();
        // 休眠时间范围：基础休眠时间 + 随机休眠时间。
        long time = baseSleepTime + r.nextInt(sleepTime);
        LOGGER.info("system will sleep:{} seconds.", time);
        TimeUnit.SECONDS.sleep(time);
    }

    /**
     * @param articles
     * @author kanpiaoxue
     *         Create Time 2018年12月8日 下午9:20:50
     *         Description : 当没有博文需要导出的时候，尝试正常退出程序
     */
    private void tryExistSystem(List<ITeyeBlogArticle> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            LOGGER.info("finish get blog from {}. System will exit!", host);
            System.exit(NORMAL_EXIST_CODE);
        }
    }

}
