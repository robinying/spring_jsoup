package com.robin.spring_jsoup.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class MeiNvImageRunner implements ApplicationRunner {

    /**
     * 获取CPU个数
     */
    private int corePoolSize = 4;
    private String baseUrl = "https://www.2meinv.com/";
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, corePoolSize + 1, 60l, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(5000));

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String basePath = "D:" + File.separator + "temp" + File.separator;

        String url = "https://www.2meinv.com/article-4173-14.html";

        System.out.println(DateTime.now().toMsStr() + "--for url:" + url);
        Runnable task = () -> {
            parseHtml(url, baseUrl, basePath);

        };
        executor.execute(task);
    }

    private void parseHtml(String url, String refererPageInit, String basePath) {
        Connection connect = connection(url, refererPageInit);
        Document document = null;
        try {
            document = connect.get();
        } catch (IOException e) {
            return;
        }
        Elements elements = document.select("div.pp.hh");
        System.out.println(DateTime.now().toMsStr() + "size:" + elements.size());
        if (elements != null && CollectionUtil.isNotEmpty(elements)) {
            Element element = elements.get(0).child(0);
            System.out.println(DateTime.now().toMsStr() + "element:" + element);
            String imgUrl = element.child(0).attr("src");
            System.out.println(DateTime.now().toMsStr() + "imgUrl:" + imgUrl);
            try {
                HttpUtil.downloadFile(imgUrl, FileUtil.mkdir(basePath));
                Thread.sleep(800);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String nextUrl = element.attr("href");
            System.out.println("nextUrl:" + nextUrl);
            if (nextUrl != null) {
                parseHtml(nextUrl, refererPageInit, basePath);
            }
        }


    }

    private Connection connection(String url, String refererPageInit) {
        Connection connect = Jsoup.connect(url);
        connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connect.header("Accept-Encoding", "gzip, deflate, sdch");
        connect.header("Accept-Language", "zh-CN,zh;q=0.8");
        connect.header("Sec-Fetch-Dest", "document");
        connect.header("Upgrade-Insecure-Requests", "1");
        connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
        connect.header("Referer", refererPageInit);
        return connect;
    }
}
