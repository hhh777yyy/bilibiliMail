package main;

import bean.Followingsinfo;
import bean.Videoinfo;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import mail.SendMail;
import util.HttpClient;

import javax.print.DocFlavor;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Start {




    public static void main(String[] args) throws Exception {
        Map<Integer,Integer> basicUpVideoCount = new HashMap<Integer, Integer>();;//-->应该为Map<Integer,Integer> basicUpVideoCount
        Map<Integer,Integer> currentUpVideoCount = new HashMap<Integer, Integer>();;//-->应该为Map<Integer,Integer> basicUpVideoCount

        List<Integer> list = AllMidFollow();
        basicUpVideoCount = VideoCount(list);//获取up主2/27投稿数，以此为基准

        for(Map.Entry<Integer,Integer> entry : basicUpVideoCount.entrySet()){
            System.out.println(entry.getKey()+"->"+entry.getValue());
        }

        SendMail sendMail = new SendMail();

        while (true) {

            currentUpVideoCount = VideoCount(list);//获取up主现有投稿数
            System.out.println("查询成功");

            for(Map.Entry<Integer,Integer> entry1 : basicUpVideoCount.entrySet()){

                Integer basicValue1 = entry1.getValue() == null?-1:entry1.getValue();
                Integer currentValue1 = currentUpVideoCount.get(entry1.getKey()) == null?-1:currentUpVideoCount.get(entry1.getKey());

//                System.out.println(basicValue1);
//                System.out.println(currentValue1);

                if(currentValue1 > basicValue1){


                    sendMail.sendEmail(entry1.getKey()+"有更新","1119848245@qq.com");
                    basicValue1 = basicValue1 +1;
                    basicUpVideoCount.put(entry1.getKey(),basicValue1);

                    System.out.println("发送成功");
                    System.out.println(basicUpVideoCount.get(entry1.getKey()));
                }
            }

        }
    }

    //获取up主的投稿数-->应改为map<up主mid，up主投稿数>
    public static Map<Integer, Integer> VideoCount(List<Integer> list) throws MalformedURLException {
        HttpClient httpClient = new HttpClient();
        String urlString2 = "https://api.bilibili.com/x/space/arc/search?mid=";
        String pdata = "&ps=30&tid=0&pn=1&keyword=&order=pubdate&jsonp=jsonp";
        Map<Integer, Integer> map=new HashMap<Integer, Integer>();

        for (int i=0;i<list.size();i++){
            Integer midFollow = list.get(i);
            String jsonVideo = httpClient.client(urlString2, midFollow, pdata);
//            Videoinfo videoinfo = JSON.parseObject(jsonVideo, Videoinfo.class);

            Videoinfo videoinfo = new Gson().fromJson(jsonVideo, Videoinfo.class);
            map.put(midFollow,videoinfo.getData().getPage().getCount());//up主mid对应其投稿数
        }






        return map;

    }

    //获取所有关注up主的mid信息
    public static List<Integer> AllMidFollow() throws MalformedURLException {
        HttpClient httpClient = new HttpClient();

        String urlString = "https://api.bilibili.com/x/relation/followings?vmid=87432177&pn=";//只能获取到50个mid
//      https://api.bilibili.com/x/relation/followings?vmid=87432177&pn=3&ps=50&order=desc&jsonp=jsonp
//        Integer mid = 87432177;
        String pdata = "&ps=50";
        List<Integer> list2 = new ArrayList<Integer>();

        for(Integer page=1;page<4;page++){
            String json = httpClient.client(urlString,page,pdata);
//            Followingsinfo followingsinfo = JSON.parseObject(json, Followingsinfo.class);
            Followingsinfo followingsinfo = new Gson().fromJson(json, Followingsinfo.class);
            List<Followingsinfo.DataBean.ListBean> list = followingsinfo.getData().getList();

            for(int i = 0;i<list.size();i++){
                int midFollow = list.get(i).getMid();
                list2.add(midFollow);

            }
        }




        return list2;


    }

}
