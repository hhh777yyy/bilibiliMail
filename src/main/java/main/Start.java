package main;


import bean.Followingsinfo;
import bean.Videoinfo;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import mail.SendMail;
import org.apache.log4j.Logger;
import util.HttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Start {
    //实例化log4j
    static Logger logger = Logger.getLogger(Start.class.getName());

    public static void main(String[] args) throws Exception {
        Map<Integer, Integer> basicUpVideoCount = new HashMap<Integer, Integer>();
        Map<Integer, Integer> currentUpVideoCount = new HashMap<Integer, Integer>();
//        获取到所有up主的mid
        List<Integer> list = AllMidFollow();
//        通过所有up主的mid获取当前项目启动时up主投稿数，以此为基准，以map<up主mid号,投稿数>的形式储存
        basicUpVideoCount = VideoCount(list);
//        查看基准的map<up主mid号,投稿数>的所有的key-value对，方便之后做对比
        for (Map.Entry<Integer, Integer> entry : basicUpVideoCount.entrySet()) {
            logger.debug(entry.getKey() + "->" + entry.getValue());
        }
        SendMail sendMail = new SendMail();
        while (true) {
            logger.debug("进入while(true)循环");
//            通过所有up主的mid号获取实时up主的投稿数，以map<up主mid号,投稿数>的形式储存
            currentUpVideoCount = VideoCount(list);
            logger.debug("查询成功");
//            basicUpVideoCount和currentUpVideoCount的key（up主）相同的，其value值（投稿数）大小对比
            for (Map.Entry<Integer, Integer> entry1 : basicUpVideoCount.entrySet()) {

                Integer basicValue1 = entry1.getValue() == null ? -1 : entry1.getValue();
                Integer currentValue1 = currentUpVideoCount.get(entry1.getKey()) == null ? -1 : currentUpVideoCount.get(entry1.getKey());

                if (currentValue1 > basicValue1) {
                    Boolean sendOrNot = sendMail.sendEmail(entry1.getKey() + "有更新,现投稿视频数为" + currentValue1, "15626128581@163.com");
                    basicValue1 = basicValue1 + 1;
                    basicUpVideoCount.put(entry1.getKey(), basicValue1);
//                    检验邮件是否发送成功，发送成功的话，把记录输出到error日志
                    if (sendOrNot = true) {
                        logger.error("发送mail成功");
                        logger.error(entry1.getKey() + "有更新,现投稿视频数" + currentValue1);
                    }
                }
            }
        }
    }

        //获取up主的投稿数，以map<up主mid，up主投稿数>的形式存储
        public static Map<Integer, Integer>  VideoCount(List<Integer> list)  throws MalformedURLException {
            HttpClient httpClient = new HttpClient();
            String urlString2 = "https://api.bilibili.com/x/space/arc/search?mid=";
            String pdata = "&ps=30&tid=0&pn=1&keyword=&order=pubdate&jsonp=jsonp";
            logger.debug("拼凑url完成");
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();

            for (int i = 0; i < list.size(); i++) {
                Integer midFollow = list.get(i);
                String jsonVideo = httpClient.client(urlString2, midFollow, pdata);
                Videoinfo videoinfo = JSON.parseObject(jsonVideo, Videoinfo.class);

                map.put(midFollow, videoinfo.getData().getPage().getCount());//up主mid对应其投稿数
            }
            logger.debug("获取关注人的投稿数成功");

            return map;
        }

//       获取所有关注up主的mid信息，以list形式存储
        public static List<Integer> AllMidFollow() throws MalformedURLException {
            HttpClient httpClient = new HttpClient();

            String urlString = "https://api.bilibili.com/x/relation/followings?vmid=87432177&pn=";
            String pdata = "&ps=50";

            List<Integer> list = new ArrayList<Integer>();
            List<Followingsinfo.DataBean.ListBean> list1 = null ;

//            https://api.bilibili.com/x/relation/followings?vmid=87432177&pn=3&ps=50&order=desc&jsonp=jsonp
//            该api每页最多存储50个up主信息        pn：页数   ps：每页最多up主信息数
            for (Integer page = 1; page < 4; page++) {
                String json = httpClient.client(urlString, page, pdata);
                Followingsinfo followingsinfo = JSON.parseObject(json, Followingsinfo.class);
                list1 = followingsinfo.getData().getList();

                for (int i = 0; i < list1.size(); i++) {
                    int midFollow = list1.get(i).getMid();
                    list.add(midFollow);
                }
            }

            return list;
        }





}

