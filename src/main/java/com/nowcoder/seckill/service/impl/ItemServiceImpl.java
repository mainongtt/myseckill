package com.nowcoder.seckill.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nowcoder.seckill.common.BusinessException;
import com.nowcoder.seckill.common.ErrorCode;
import com.nowcoder.seckill.component.ObjectValidator;
import com.nowcoder.seckill.dao.ItemMapper;
import com.nowcoder.seckill.dao.ItemStockLogMapper;
import com.nowcoder.seckill.dao.ItemStockMapper;
import com.nowcoder.seckill.dao.PromotionMapper;
import com.nowcoder.seckill.entity.Item;
import com.nowcoder.seckill.entity.ItemStock;
import com.nowcoder.seckill.entity.ItemStockLog;
import com.nowcoder.seckill.entity.Promotion;
import com.nowcoder.seckill.service.ItemService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService, ErrorCode {

    private Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private PromotionMapper promotionMapper;

    @Autowired
    private ObjectValidator validator;

    @Autowired
    private ItemStockLogMapper itemStockLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 本地缓存
    private Cache<String, Object> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public List<Item> findItemsOnPromotion() {
        List<Item> items = itemMapper.selectOnPromotion();
        return items.stream().map(item -> {
            // 查库存
            ItemStock stock = itemStockMapper.selectByItemId(item.getId());
            item.setItemStock(stock);
            // 查活动
            Promotion promotion = promotionMapper.selectByItemId(item.getId());
            if (promotion != null && promotion.getStatus() == 0) {
                item.setPromotion(promotion);
            }
            return item;
        }).collect(Collectors.toList());
    }

    public Item findItemById(int id) {
        if (id <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        // 查商品
        Item item = itemMapper.selectByPrimaryKey(id);
        if (item != null) {
            // 查库存
            ItemStock stock = itemStockMapper.selectByItemId(id);
            item.setItemStock(stock);

            // 查活动
            Promotion promotion = promotionMapper.selectByItemId(id);
            if (promotion != null && promotion.getStatus() == 0) {
                item.setPromotion(promotion);
            }
        }

        return item;
    }

    public Item findItemInCache(int id) {
        if (id <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        Item item = null;
        String key = "item:" + id;

        // guava
        item = (Item) cache.getIfPresent(key);
        if (item != null) {
            return item;
        }

        // redis
        item = (Item) redisTemplate.opsForValue().get(key);
        if (item != null) {
            cache.put(key, item);
            return item;
        }

        // mysql
        item = this.findItemById(id);
        if (item != null) {
            cache.put(key, item);
            redisTemplate.opsForValue().set(key, item, 3, TimeUnit.MINUTES);
        }

        return item;
    }

    public void increaseSales(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        itemMapper.increaseSales(itemId, amount);
    }

    public boolean decreaseStock(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        int rows = itemStockMapper.decreaseStock(itemId, amount);
        return rows > 0;
    }

    @Override
    public boolean decreaseStockInCache(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        String key = "item:stock:" + itemId;
        long result = redisTemplate.opsForValue().decrement(key, amount);
        if (result < 0) {
            // 回补库存
            this.increaseStockInCache(itemId, amount);
            logger.debug("回补库存完成 [" + itemId + "]");
        } else if (result == 0) {
            // 售罄标识
            redisTemplate.opsForValue().set("item:stock:over:" + itemId, 1);
            logger.debug("售罄标识完成 [" + itemId + "]");
        }

        return result >= 0;
    }

    @Override
    public boolean increaseStockInCache(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        String key = "item:stock:" + itemId;
        redisTemplate.opsForValue().increment(key, amount);

        return true;
    }

    @Override
    public ItemStockLog createItemStockLog(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        ItemStockLog log = new ItemStockLog();
        log.setId(UUID.randomUUID().toString().replace("-", ""));
        log.setItemId(itemId);
        log.setAmount(amount);
        log.setStatus(0);

        itemStockLogMapper.insert(log);

        return log;
    }

    @Override
    public void updateItemStockLogStatus(String id, int status) {
        ItemStockLog log = itemStockLogMapper.selectByPrimaryKey(id);
        log.setStatus(status);
        itemStockLogMapper.updateByPrimaryKey(log);
    }

    @Override
    public ItemStockLog findItemStorkLogById(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        return itemStockLogMapper.selectByPrimaryKey(id);
    }
}
