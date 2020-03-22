package main;


import bean.Followingsinfo;
import bean.Videoinfo;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import mail.SendMail;
import org.apache.log4j.Logger;
import util.HttpClient;

import java.net.MalformedURLException;
import java.util.*;



public class Start {
    //实例化log4j
    private static Logger logger = Logger.getLogger(Start.class.getName());

    public static void main(String[] args) throws Exception {
        Map<Integer, Integer> basicUpVideoCount;
        Map<Integer, Integer> currentUpVideoCount;
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
            logger.debug("while(true)");
//            通过所有up主的mid号获取实时up主的投稿数，以map<up主mid号,投稿数>的形式储存
            currentUpVideoCount = VideoCount(list);
            logger.debug("check");
//            basicUpVideoCount和currentUpVideoCount的key（up主）相同的，其value值（投稿数）大小对比
            for (Map.Entry<Integer, Integer> entry1 : basicUpVideoCount.entrySet()) {

                Integer basicValue1 = entry1.getValue() == null ? -1 : entry1.getValue();
                Integer currentValue1 = currentUpVideoCount.get(entry1.getKey()) == null ? -1 : currentUpVideoCount.get(entry1.getKey());

                if (currentValue1 > basicValue1) {
                    Boolean sendOrNot = SendMail.sendEmail(entry1.getKey() + "有更新,现投稿视频数为" + currentValue1, "15626128581@163.com");
                    basicValue1 = basicValue1 + 1;
                    basicUpVideoCount.put(entry1.getKey(), basicValue1);
//                    检验邮件是否发送成功，发送成功的话，把记录输出到error日志
                    if (sendOrNot) {
                        logger.error("发送mail成功");
                        logger.error(entry1.getKey() + "有更新,现投稿视频数" + currentValue1);
                    }
                }
            }
        }
    }

        //获取up主的投稿数，以map<up主mid，up主投稿数>的形式存储
        private static Map<Integer, Integer>  VideoCount(List<Integer> list) {
            HttpClient httpClient = new HttpClient();

            String jsonVideo;
            Videoinfo videoinfo;

            String urlString2 = "https://api.bilibili.com/x/space/arc/search?mid=";
            String pdata = "&ps=30&tid=0&pn=1&keyword=&order=pubdate&jsonp=jsonp";
            logger.debug("url");
            Map<Integer, Integer> map = new HashMap<>();

            for (int i = 0; i < list.size(); i++) {
                Integer midFollow = list.get(i);
                jsonVideo = httpClient.client(urlString2, midFollow, pdata);
                videoinfo = JSON.parseObject(jsonVideo, Videoinfo.class);

/*
 *              长期运行，有时会产生连接超时错误，加上一层while判断，
 *              若连接url超时或出错，jsonVideo的值没获取到，则一直尝试重连，直至连接成功，获取到jsonVideo和videoinfo的值
 */
                while (videoinfo == null || jsonVideo == null){
                    logger.debug("videoinfo为空");
                    jsonVideo = httpClient.client(urlString2, midFollow, pdata);
                    videoinfo = JSON.parseObject(jsonVideo, Videoinfo.class);
                }

/*
 *              原先的： vlist = videoinfo.getData().getList().getVlist();
 *                      List<Videoinfo.DataBean.ListBean.VlistBean> vlist = collect(vlistOpt,toList());
 *                      Integer count = videoinfo.getData().getPage().getCount();
 *              原先的多层级联调用易产生NullPointException
 *              optional解决长期运行，多层级联调用易产生NullPointException的问题
 *              optional类转换为list类，是在optional类封装的对象外再封装一层List（ 该list.size()=1 ），要取出optional类封装的对象，要用foreach循环
 */
                Optional<Videoinfo> videoinfoOpt = Optional.of(videoinfo);
                Optional<List<Videoinfo.DataBean.ListBean.VlistBean>> vlistOpt = videoinfoOpt.
                        map(Videoinfo::getData)
                        .map(Videoinfo.DataBean::getList)
                        .map(Videoinfo.DataBean.ListBean::getVlist);

                List<List<Videoinfo.DataBean.ListBean.VlistBean>> vlist = vlistOpt.
                        map(Collections::singletonList).
                        orElse(Collections.emptyList());

                Optional<Integer> countOpt =videoinfoOpt.map(Videoinfo::getData)
                        .map(Videoinfo.DataBean::getPage)
                        .map(Videoinfo.DataBean.PageBean::getCount);

                List<Integer> count = countOpt.
                        map(Collections::singletonList).
                        orElse(Collections.emptyList());

//                迭代vlist，取出VlistBean中key = mid 的value值与midFollow对比
                for (int j = 0 ; j < vlist.size() ; j++){
                    for (int z = 0 ; z < vlist.get(j).size() ; z++)
                        if (vlist.get(j).get(z).getMid() == midFollow){
                            if (0 != count.get(j)){
                                map.put(midFollow, count.get(j));//up主mid对应其投稿数
                            }else {
                                logger.error("cannot get count");
                            }
                        }
                }
            }
            logger.debug("投稿数");
            return map;
        }

    /**       获取所有关注up主的mid信息，以list形式存储
     *          https://api.bilibili.com/x/relation/followings?vmid=87432177&pn=3&ps=50&order=desc&jsonp=jsonp
     *         该api每页最多存储50个up主信息        pn：页数   ps：每页最多up主信息数
     */
        private static List<Integer> AllMidFollow() {
            HttpClient httpClient = new HttpClient();

            String urlString = "https://api.bilibili.com/x/relation/followings?vmid=87432177&pn=";
            String pdata = "&ps=50";

            List<Integer> list = new ArrayList<>();
            List<Followingsinfo.DataBean.ListBean> list1 ;


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

