package PixivDownloader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Crawler {

    private Map<String, String> cookies = new HashMap<String, String>();
    private Config config;
    private String jarpath;
    public SQLiter db;
    public final static String MiniSharkInfo = "[MiniShark] ";

    public void setJarPath(String path) {
        this.jarpath = path;
    }

    public Crawler(Config config, SQLiter db) {
        this.config = config;
        this.db = db;
    }

    public Crawler(SQLiter db) {
        this.db = db;
    }

    public void addCookie(String key, String value) {
        cookies.put(key, value);
    }

    public List<InputFile> resolveListPage(String url) {
        List<InputFile> result = new ArrayList<>();
        try {
            Response res;
            while (true) {
                try {
                    if (!cookies.isEmpty()) {
                        res = Jsoup.connect(url).cookies(cookies).method(Method.GET).execute();
                    } else {
                        res = Jsoup.connect(url).execute();
                    }
                    break;
                } catch (SocketTimeoutException e) {
                    System.out.println(MiniSharkInfo + "请求图片列表页面超时, 将重试.");
                } catch (SSLHandshakeException e) {
                    System.out.println(MiniSharkInfo + "请求图片列表页面被拒绝, 将重试.");
                } catch (SSLException e) {
                    System.out.println(MiniSharkInfo + "请求图片列表页面被关闭, 将重试.");
                } catch (ConnectException e) {// cookie错误将会超时
                    System.out.println(MiniSharkInfo + "连接超时, 请检查cookie是否错误或过期.");
                } catch (SocketException e) {
                    System.out.println(MiniSharkInfo + "意外结束, 将重试.");
                } catch (HttpStatusException e) {// cookie不正确或没有将返回错误码
                    System.out.println(MiniSharkInfo + "HTTP状态错误" + e.getStatusCode() + " 请填写正确的cookie.");
                }
            }
            Document doc = res.parse();
            Elements pages = doc.select("#wrapper").select("div.layout-body").select("div")
                    .select("div.ui-fixed-container").select("div").select("nav:nth-child(2)").select("ul")
                    .select("li.after").select("a");
            Elements images = doc.select("#wrapper").select("div.layout-body").select("div")
                    .select("div.ranking-items-container").select("div.ranking-items.adjust")
                    .select("section.ranking-item");
            for (Element image : images) {
                String dataid = image.attr("data-id");
                Element imagepage = image.select("div.ranking-image-item").select("a").get(0);
                String imagepageurl = imagepage.absUrl("href");
                System.out.println(MiniSharkInfo + "正在爬取: " + imagepageurl);
                resolveImagePage(imagepageurl, dataid, result);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void resolveImagePage(String url, String dataid, List<InputFile> result) {
        try {
            Response res = null;
            while (true) {
                try {
                    res = Jsoup.connect(url).cookies(cookies).method(Method.GET).execute();
                    break;
                } catch (SocketTimeoutException e) {
                    System.out.println(MiniSharkInfo + "请求图片页面超时, 将重试.");
                } catch (SSLHandshakeException e) {
                    System.out.println(MiniSharkInfo + "请求图片页面被拒绝, 将重试.");
                } catch (SSLException e) {
                    System.out.println(MiniSharkInfo + "请求图片页面被关闭, 将重试.");
                } catch (ConnectException e) {// cookie错误将会超时, 但是其实可以不带cookie
                    System.out.println(MiniSharkInfo + "连接超时, 请检查cookie是否错误或过期.");
                } catch (SocketException e) {
                    System.out.println(MiniSharkInfo + "意外结束, 将重试.");
                }
            }
            Document doc = res.parse();
            Element meta = doc.select("#meta-preload-data").first();
            String content = meta.attr("content");
            JSONObject obj = JSON.parseObject(content);
            int pagecount = obj.getJSONObject("illust").getJSONObject(dataid).getIntValue("pageCount");
            String p0url = obj.getJSONObject("illust").getJSONObject(dataid).getJSONObject("urls")
                    .getString("original");
            getMemoryPhoto(pagecount, p0url, dataid, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getMemoryPhoto(int pagecount, String p0url, String dataid, List<InputFile> result) {
        for (int i = 0; i < pagecount; i++) {
            String imgurl = null;
            if (i == 0) {
                imgurl = p0url;
            } else {
                imgurl = p0url.replaceAll("p0", "p" + i);
            }
            String filename = imgurl.substring(imgurl.lastIndexOf("/") + 1);
            Response resimg = null;
            while (true) {
                InputStream in = null;
                try {
                    resimg = Jsoup.connect(imgurl).cookies(cookies).ignoreContentType(true).maxBodySize(1073741824)
                            .referrer("https://www.pixiv.net/artworks/" + dataid).execute();
                    in = new ByteArrayInputStream(resimg.bodyAsBytes());
                    result.add(new InputFile(in, filename));
                    break;
                } catch (SocketTimeoutException e) {
                    System.out.println(MiniSharkInfo + "请求图片超时, 将重试.");
                } catch (SSLHandshakeException e) {
                    System.out.println(MiniSharkInfo + "请求图片被拒绝, 将重试.");
                } catch (SSLException e) {
                    System.out.println(MiniSharkInfo + "请求图片被关闭, 将重试.");
                } catch (ConnectException e) {// cookie错误将会超时, 但是其实可以不带cookie
                    System.out.println(MiniSharkInfo + "连接超时, 请检查cookie是否错误或过期.");
                } catch (SocketException e) {
                    System.out.println(MiniSharkInfo + "意外结束, 将重试.");
                } catch (HttpStatusException e) {// 防止有画师上传的同一个帖子内文件格式不同
                    if (imgurl.contains(".jpg")) {
                        imgurl = imgurl.replaceAll(".jpg", ".png");
                    } else if (imgurl.contains(".png")) {
                        imgurl = imgurl.replaceAll(".png", ".jpg");
                    }
                    System.out.println(MiniSharkInfo + "HTTP状态错误:" + e.getStatusCode() + " 将尝试另一后缀名.");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } finally {
                    try {
                        in.close();
                    } catch (NullPointerException | IOException ignored) {
                    }
                }
            }
        }
    }

    public String oldResolveListPage(String url) {
        String nextpageurl = null;
        try {
            Response res = null;
            while (true) {
                try {
                    res = Jsoup.connect(url).cookies(cookies).method(Method.GET).execute();
                    break;
                } catch (SocketTimeoutException e) {
                    System.out.println(MiniSharkInfo + "请求图片列表页面超时, 将重试.");
                } catch (SSLHandshakeException e) {
                    System.out.println(MiniSharkInfo + "请求图片列表页面被拒绝, 将重试.");
                } catch (SSLException e) {
                    System.out.println(MiniSharkInfo + "请求图片列表页面被关闭, 将重试.");
                } catch (ConnectException e) {// cookie错误将会超时
                    System.out.println(MiniSharkInfo + "连接超时, 请检查cookie是否错误或过期.");
                } catch (SocketException e) {
                    System.out.println(MiniSharkInfo + "意外结束, 将重试.");
                } catch (HttpStatusException e) {// cookie不正确或没有将返回错误码
                    System.out.println(MiniSharkInfo + "HTTP状态错误" + e.getStatusCode() + " 请填写正确的cookie.");
                }
            }
            Document doc = res.parse();
            Elements pages = doc.select("#wrapper").select("div.layout-body").select("div")
                    .select("div.ui-fixed-container").select("div").select("nav:nth-child(2)").select("ul")
                    .select("li.after").select("a");
            Element nextpage = null;
            try {
                nextpage = pages.get(0);
            } catch (IndexOutOfBoundsException e) {
                System.out.println(MiniSharkInfo + "网页格式错误，请检查cookie是否已经过期 (或者该榜单已被爬取完毕) .");
                System.exit(1);
            }
            nextpageurl = nextpage.absUrl("href");
            Elements images = doc.select("#wrapper").select("div.layout-body").select("div")
                    .select("div.ranking-items-container").select("div.ranking-items.adjust")
                    .select("section.ranking-item");
            for (Element image : images) {
                String dataid = image.attr("data-id");
                Element imagepage = image.select("div.ranking-image-item").select("a").get(0);
                String imagepageurl = imagepage.absUrl("href");
                System.out.println(MiniSharkInfo + "正在爬取: " + imagepageurl);
                oldResolveImagePage(imagepageurl, dataid);
            }
            config.setValue("startpage", url);
            config.Save();
        } catch (IOException e) {
            e.printStackTrace();
            return nextpageurl;
        }
        System.out.println(nextpageurl);
        return nextpageurl;
    }

    public void oldResolveImagePage(String url, String dataid) {
        int amount = db.checkArtworks(Integer.parseInt(dataid));// 尝试获取一个值
        if (amount == 0) {// 如果为0说明数据库中没有这个数据
            System.out.println(MiniSharkInfo + "数据库中未查到此图片页面的信息, 继续下载操作.");
        } else {// 如果不是0说明这些图片已经下载过了
            System.out.println(MiniSharkInfo + "数据库中已查到此图片页面的信息, 图片数量为" + amount + ", 自动跳过.");
            return;
        }
        try {
            Response res = null;
            while (true) {
                try {
                    res = Jsoup.connect(url).cookies(cookies).method(Method.GET).execute();
                    break;
                } catch (SocketTimeoutException e) {
                    System.out.println(MiniSharkInfo + "请求图片页面超时, 将重试.");
                } catch (SSLHandshakeException e) {
                    System.out.println(MiniSharkInfo + "请求图片页面被拒绝, 将重试.");
                } catch (SSLException e) {
                    System.out.println(MiniSharkInfo + "请求图片页面被关闭, 将重试.");
                } catch (ConnectException e) {// cookie错误将会超时, 但是其实可以不带cookie
                    System.out.println(MiniSharkInfo + "连接超时, 请检查cookie是否错误或过期.");
                } catch (SocketException e) {
                    System.out.println(MiniSharkInfo + "意外结束, 将重试.");
                }
            }
            Document doc = res.parse();
            Element meta = doc.select("#meta-preload-data").first();
            String content = meta.attr("content");
            JSONObject obj = JSON.parseObject(content);
            int pagecount = obj.getJSONObject("illust").getJSONObject(dataid).getIntValue("pageCount");
            String p0url = obj.getJSONObject("illust").getJSONObject(dataid).getJSONObject("urls")
                    .getString("original");
            getFilePhoto(pagecount, p0url, dataid);
            db.addArtworks(Integer.parseInt(dataid), pagecount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFilePhoto(int pagecount, String p0url, String dataid) {
        for (int i = 0; i < pagecount; i++) {
            String imgurl = null;
            if (i == 0) {
                imgurl = p0url;
            } else {
                imgurl = p0url.replaceAll("p0", "p" + i);
            }
            String filename = imgurl.substring(imgurl.lastIndexOf("/") + 1);
            // 这里要去掉jarpath末尾的"/"
            String imagesavepath = config.getString("imagesavepath").replace("%HERE%",
                    jarpath.substring(0, jarpath.length() - 1));
            File imgfile = Util.createFile(imagesavepath, filename);
            Response resimg = null;
            while (true) {
                BufferedInputStream in = null;
                BufferedOutputStream out = null;
                try {
                    resimg = Jsoup.connect(imgurl).cookies(cookies).ignoreContentType(true).maxBodySize(1073741824)
                            .referrer("https://www.pixiv.net/artworks/" + dataid).execute();
                    in = resimg.bodyStream();
                    out = new BufferedOutputStream(new FileOutputStream(imgfile));
                    byte[] bytes = new byte[1024];
                    int total = 0;
                    int count = 0;
                    while ((count = in.read(bytes)) != -1) {
                        out.write(bytes, 0, count);
                        total = total + count;
                    }
                    System.out.println(MiniSharkInfo + "文件" + filename + "保存完成, 共收到" + total + "字节.");
                    break;
                } catch (SocketTimeoutException e) {
                    System.out.println(MiniSharkInfo + "请求图片超时, 将重试.");
                } catch (SSLHandshakeException e) {
                    System.out.println(MiniSharkInfo + "请求图片被拒绝, 将重试.");
                } catch (SSLException e) {
                    System.out.println(MiniSharkInfo + "请求图片被关闭, 将重试.");
                } catch (ConnectException e) {// cookie错误将会超时, 但是其实可以不带cookie
                    System.out.println(MiniSharkInfo + "连接超时, 请检查cookie是否错误或过期.");
                } catch (SocketException e) {
                    System.out.println(MiniSharkInfo + "意外结束, 将重试.");
                } catch (HttpStatusException e) {// 防止有画师上传的同一个帖子内文件格式不同
                    if (imgurl.contains(".jpg")) {
                        imgurl = imgurl.replaceAll(".jpg", ".png");
                    } else if (imgurl.contains(".png")) {
                        imgurl = imgurl.replaceAll(".png", ".jpg");
                    }
                    System.out.println(MiniSharkInfo + "HTTP状态错误:" + e.getStatusCode() + " 将尝试另一后缀名.");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        out.close();
                    } catch (NullPointerException | IOException e) {
                    }
                }
            }
        }
    }

}
