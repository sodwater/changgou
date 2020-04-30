package com.changgou.item.controller;

import com.changgou.item.service.PageService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * 描述
 *
 * 作者：苏打水
 * @version 1.0
 * @package com.changgou.item.controller *
 * @since 1.0
 */

@RestController
@RequestMapping("/page")
public class PageController {
    @Autowired
    private PageService pageService;

    /**
     * 生成静态页面
     * @param id SPU的ID
     * @return
     */
    //@ResponseBody         //返回json字符串
    @RequestMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(name="id") Long id){
        pageService.createPageHtml(id);
        return new Result(true, StatusCode.OK,"ok");
    }

    @GetMapping("/items/{id}")
    public ModelAndView html(@PathVariable(name="id") Long id){
        String url="items/"+id;
        //控制返回页面
        ModelAndView  modelAndView= new ModelAndView(url);
        return modelAndView;
    }

    @GetMapping("/templates/items")
    public ModelAndView item(){
        String url="item";
        ModelAndView  modelAndView= new ModelAndView(url);
        return modelAndView;
    }
}
