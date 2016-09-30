package com.xuxue.tes;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by liuwei on 2016/9/26.
 */
public class Test {

    static Logger LOG= LoggerFactory.getLogger(Test.class);


    public static void main(String[] args)throws Exception{

        String s="{\"url\":\"https://img.alicdn.com/imgextra/i1/2266950171/TB2wDE2gVXXXXXTXXXXXXXXXXXX_!!2266950171.jpg\",\"taskId\":26199,\"level\":4,\"referer\":\"https://detail.tmall.com/item.htm?id\\u003d524053480750\\u0026rn\\u003d92f583c0e65b1272cca96f0780440ce3\\u0026abbucket\\u003d0\",\"store\":true,\"ossType\":0,\"accessKey\":\"AuY3mCfqPXETorkE\",\"secretKey\":\"cMWaCnGRl7wctiWGngPIVmDhjDK0HA\",\"endpoint\":\"http://oss-cn-beijing.aliyuncs.com\",\"bucket\":\"crawler-img\"}";

    }

}
