package com.sangeng.job;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sangeng.domain.entity.Article;
import com.sangeng.service.ArticleService;
import com.sangeng.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UpdateViewCountJob {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    @Scheduled(cron = "0 0/10 * * * ?")//每10分钟执行一次
    public void updateViewCount(){
        //获取redis中的浏览量
        Map<String, Integer> viewCountMap = redisCache.getCacheMap("article:viewCount");

        //更新数据库
        List<Article> articles = viewCountMap.entrySet().stream().map(item -> new Article(Long.valueOf(item.getKey()), Long.valueOf(item.getValue()))).collect(Collectors.toList());
        for (Article article : articles) {
            LambdaUpdateWrapper<Article> wrapper = Wrappers.lambdaUpdate(Article.class).eq(Article::getId, article.getId()).set(Article::getViewCount, article.getViewCount());
            articleService.update(wrapper);
        }

    }
}
