package util;

import java.io.*;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;


public class HttpClient {

    public String client(String urlString,Integer mid,String pdata) {
        StringBuilder json = new StringBuilder();
        InputStream inStream ;
        URL urlObject ;
        HttpURLConnection conn;

        try {
            urlObject = new URL(urlString+mid+pdata);
            conn = (HttpURLConnection)urlObject.openConnection();

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("connection","Keep-Alive");
            conn.connect();

            inStream = conn.getInputStream();

            // 设置为utf-8的编码 解决中文乱码
            BufferedReader in = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

}




