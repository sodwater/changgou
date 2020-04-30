package com.changgou.seckill.timer;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class  SeckillGoodsPushTask {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /****
     * 每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis(){
        //求时间菜单
        List<Date> dateMenus = DateUtil.getDateMenus();

        //循环查询每个时间区间的秒杀商品
        for (Date dateMenu : dateMenus) {
            //求时间的字符串格式
            String timespace = "SeckillGoods_" + DateUtil.data2str(dateMenu,"yyyyMMddHH");
            //通过mapper封装审核条件
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //审核通过
            criteria.andEqualTo("status","1");
            //秒杀商品库存>0
            criteria.andGreaterThan("stockCount","0");
            //时间
            criteria.andGreaterThanOrEqualTo("startTime", dateMenu);
            criteria.andLessThan("endTime", DateUtil.addDateHour(dateMenu, 2));
            //排除已经存入到redis中的SeckillGoods
            Set keys = redisTemplate.boundHashOps(timespace).keys();
            if (keys!=null && keys.size()>0){
                //开始排除
                criteria.andNotIn("id", keys);
            }

            //查询数据
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);


            for (SeckillGoods seckillGood : seckillGoods) {
                System.out.println("商品ID："+ seckillGood.getId() + "存入到了redis--" + timespace);
                //存入到redis
                redisTemplate.boundHashOps(timespace).put(seckillGood.getId(), seckillGood);

                //给每个商品做个队列集合
                redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGood.getId()).leftPushAll(putAllIds(seckillGood.getStockCount(), seckillGood.getId()));
            }
        }

    }

    /**
     * 获取每个商品的ID集合
     */
    public Long[] putAllIds(Integer num, Long id){
        Long[] ids = new Long[num];
        for (int i = 0; i < ids.length; i++){
            ids[i] = id;
        }
        return ids;
    }
}
