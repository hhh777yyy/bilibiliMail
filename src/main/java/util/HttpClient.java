package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpClient {

    public String client(String urlString,Integer mid) throws MalformedURLException {
        StringBuilder json = new StringBuilder();
        URL urlObject = new URL(urlString+mid);
        try {
//            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            // 设置为utf-8的编码 解决中文乱码
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "utf-8"));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    public String client(String urlString,Integer mid,String pdata) throws MalformedURLException {
        StringBuilder json = new StringBuilder();
        URL urlObject = new URL(urlString+mid+pdata);
        try {
//            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            // 设置为utf-8的编码 解决中文乱码
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "utf-8"));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

}
