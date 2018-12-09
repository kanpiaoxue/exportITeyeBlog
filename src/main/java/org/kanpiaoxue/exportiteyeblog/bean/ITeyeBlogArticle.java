package org.kanpiaoxue.exportiteyeblog.bean;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 * ITeyeBlog.java
 *
 * @author kanpiaoxue
 * @version 1.0
 *          Create Time 2018年12月5日 下午9:57:17
 *          Description : iteye博客文章javabean
 */
public class ITeyeBlogArticle implements Cloneable {
    /**
     * 博客标题
     */
    private String blogTitle;
    /**
     * 分类标签
     */
    private String blogCategorys;
    /**
     * 文件路径
     */
    private String markdownFileName;

    /**
     * 博客内容
     */
    private String blogBody;

    /**
     * iteye中的博客地址
     */
    private String originalUrl;

    /**
     * 发表时间
     */
    private DateTime publicationTime;

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public ITeyeBlogArticle clone() throws CloneNotSupportedException {
        return (ITeyeBlogArticle) super.clone();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ITeyeBlogArticle other = (ITeyeBlogArticle) obj;
        if (blogBody == null) {
            if (other.blogBody != null) {
                return false;
            }
        } else if (!blogBody.equals(other.blogBody)) {
            return false;
        }
        if (blogCategorys == null) {
            if (other.blogCategorys != null) {
                return false;
            }
        } else if (!blogCategorys.equals(other.blogCategorys)) {
            return false;
        }
        if (blogTitle == null) {
            if (other.blogTitle != null) {
                return false;
            }
        } else if (!blogTitle.equals(other.blogTitle)) {
            return false;
        }
        if (markdownFileName == null) {
            if (other.markdownFileName != null) {
                return false;
            }
        } else if (!markdownFileName.equals(other.markdownFileName)) {
            return false;
        }
        if (originalUrl == null) {
            if (other.originalUrl != null) {
                return false;
            }
        } else if (!originalUrl.equals(other.originalUrl)) {
            return false;
        }
        if (publicationTime == null) {
            if (other.publicationTime != null) {
                return false;
            }
        } else if (!publicationTime.equals(other.publicationTime)) {
            return false;
        }
        return true;
    }

    /**
     * @return the blogBody
     */
    public String getBlogBody() {
        return blogBody;
    }

    /**
     * @return the blogCategorys
     */
    public String getBlogCategorys() {
        return blogCategorys;
    }

    /**
     * @return the blogTitle
     */
    public String getBlogTitle() {
        return blogTitle;
    }

    /**
     * @return the markdownFileName
     */
    public String getMarkdownFileName() {
        return markdownFileName;
    }

    /**
     * @return the originalUrl
     */
    public String getOriginalUrl() {
        return originalUrl;
    }

    /**
     * @return the publicationTime
     */
    public DateTime getPublicationTime() {
        return publicationTime;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((blogBody == null) ? 0 : blogBody.hashCode());
        result = prime * result + ((blogCategorys == null) ? 0 : blogCategorys.hashCode());
        result = prime * result + ((blogTitle == null) ? 0 : blogTitle.hashCode());
        result = prime * result + ((markdownFileName == null) ? 0 : markdownFileName.hashCode());
        result = prime * result + ((originalUrl == null) ? 0 : originalUrl.hashCode());
        result = prime * result + ((publicationTime == null) ? 0 : publicationTime.hashCode());
        return result;
    }

    /**
     * @param blogBody
     *            the blogBody to set
     */
    public void setBlogBody(String blogBody) {
        this.blogBody = blogBody;
    }

    /**
     * @param blogCategorys
     *            the blogCategorys to set
     */
    public void setBlogCategorys(String blogCategorys) {
        this.blogCategorys = blogCategorys;
    }

    /**
     * @param blogTitle
     *            the blogTitle to set
     */
    public void setBlogTitle(String blogTitle) {
        this.blogTitle = StringUtils.isNotBlank(blogTitle) ? blogTitle.trim() : blogTitle;
    }

    /**
     * @param markdownFileName
     *            the markdownFileName to set
     */
    public void setMarkdownFileName(String markdownFileName) {
        this.markdownFileName = markdownFileName;
    }

    /**
     * @param originalUrl
     *            the originalUrl to set
     */
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    /**
     * @param publicationTime
     *            the publicationTime to set
     */
    public void setPublicationTime(DateTime publicationTime) {
        this.publicationTime = publicationTime;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ITeyeBlogArticle [blogTitle=" + blogTitle + ", blogCategorys=" + blogCategorys
                + ", markdownFileName=" + markdownFileName + ", blogBody=" + blogBody + ", originalUrl="
                + originalUrl + ", publicationTime=" + publicationTime.toString("yyyy-MM-dd") + "]";
    }

}
