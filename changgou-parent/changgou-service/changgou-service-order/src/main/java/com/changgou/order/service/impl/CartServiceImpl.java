package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    //数据存入到redis
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;


    /**
     * 加入购物车
     * @param num
     * @param id
     * @return
     */
    @Override
    public void add(Integer num, Long id, String username) {
        //dang 添加购物车数量<=0,移除
        if (num<=0){
            redisTemplate.boundHashOps("Cart_"+username).delete(id);

            //如果此时购物车数量为空 则连购物车一起移除
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if (size==null || size<=0){
                redisTemplate.delete("Cart_"+username);
            }
            return;
        }


        //查询商品的详情
        //1查询sku
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        //2查询spu
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();
        //封装
        OrderItem orderItem = getOrderItem(num, id, sku, spu);

        //将购物车数据存入Redis：命名空间-->username
        redisTemplate.boundHashOps("Cart_"+username).put(id, orderItem);
    }

    /**
     * 购物车集合查询
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        return redisTemplate.boundHashOps("Cart_" + username).values();
    }


    /**
     * 创建一个orderItem对象
     * @param num
     * @param id
     * @param sku
     * @param spu
     * @return
     */
    private OrderItem getOrderItem(Integer num, Long id, Sku sku, Spu spu) {
        //将加入购物车得信息封装成orderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());//三级分类
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());//商品的名称  sku的名称
        orderItem.setPrice(sku.getPrice());//sku的单价
        orderItem.setNum(num);//购买的数量
        orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());//单价* 数量
        orderItem.setPayMoney(orderItem.getNum() * orderItem.getPrice());//单价* 数量
        orderItem.setImage(sku.getImage());//商品的图片dizhi
        return orderItem;
    }
}
