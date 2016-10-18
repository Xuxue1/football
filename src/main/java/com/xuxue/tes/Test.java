package com.xuxue.tes;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;


/**
 * Created by liuwei on 2016/9/26.
 */
public class Test {

    static Logger LOG= LoggerFactory.getLogger(Test.class);


    public static void main(String[] args)throws Exception{
        NumberFormat f=NumberFormat.getInstance();
        f.setMaximumFractionDigits(10);
        Double d = new Double(1234567890.1234);
        System.out.println(f.format(d));

    }

}
