package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String,String> searchMap, Model model){
        //1调用changgou-service-search微服务
        Map resultMap = skuFeign.search(searchMap);
        //2搜索数据结果
        model.addAttribute("result",resultMap);
        //3搜索条件
        model.addAttribute("searchMap",searchMap);

        //4.记住之前的URL
        //拼接url
        String url = url(searchMap);
        model.addAttribute("url",url);

        //创建一个分页的对象  可以获取当前页 和总个记录数和显示的页码(以当前页为中心的5个页码)
        Page<SkuInfo> pageInfo = new Page<SkuInfo>(
                Long.parseLong(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNum").toString())+1,
                Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo",pageInfo);
        return "search";
    }

    /**
     * 拼装组装用户请求的url地址
     * 获取用户每次请求的地址
     * 页面需要在这次的请求的地址上添加额外的搜索条件
     */
    private String url(Map<String, String> searchMap) {
        String url = "/search/list";
        if(searchMap!=null && searchMap.size()>0){
            url+="?";
            for (Map.Entry<String, String> stringStringEntry : searchMap.entrySet()) {
                //key是搜索条件对象
                String key = stringStringEntry.getKey();
                //value搜索的值
                String value = stringStringEntry.getValue();
                //跳过分页参数
                if(key.equals("pageNum")){
                    continue;
                }

                //如果是排序 则 跳过 拼接排序的地址 因为有数据
                if(stringStringEntry.getKey().equals("sortField") || stringStringEntry.getKey().equals("sortRule")){
                    continue;
                }
                url += stringStringEntry.getKey() + "=" + stringStringEntry.getValue() + "&";

            }
            //去掉多余的&
            if(url.lastIndexOf("&")!=-1){
                url =  url.substring(0,url.lastIndexOf("&"));
            }
        }
        return url;
    }
}
