package com.mynote.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mynote.common.RedisKey;
import com.mynote.domain.po.Note;
import com.mynote.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncTask {
    private final NoteService noteService;
    private final StringRedisTemplate redisTemplate;
    private static final int BATCH_SIZE = 500;//每次批量更新500条，防止SQL过长
    //定时任务：每五分钟同步一次
    @Scheduled(fixedRate = 300000)
    public void syncViewCountToDB() {
        log.info("开始同步 Redis 浏览量到数据库...");
        int totalSyncCount = 0;

        //1.规定获取key条件
        ScanOptions options = ScanOptions.scanOptions()
                .match(RedisKey.USER_NOTE_VIEW_COUNT_PREFIX + "*")
                .count(100)
                .build();
        //2.批量更新数据库
        Map<Long, Integer> batchMap = new HashMap<>();

        //3.获取key并处理
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            //3.1 遍历cursor
            while (cursor.hasNext()) {
                String key = cursor.next();
                String countStr = redisTemplate.opsForValue().get(key);

                Long noteId = Long.parseLong(key.replace(RedisKey.USER_NOTE_VIEW_COUNT_PREFIX, ""));
                int incrementCount = Integer.parseInt(countStr);

                batchMap.put(noteId, incrementCount);
                //删除更新过的key，这样比直接更新效率好
                redisTemplate.delete(key);

                if (batchMap.size() >= BATCH_SIZE) {
                    //大于500条，则批量更新数据库
                    totalSyncCount += executeBatchUpdate(batchMap);
                    batchMap.clear();
                    log.info("已同步 {} 条浏览量到数据库", totalSyncCount);
                }
            }
            //处理剩余的key
            if (!batchMap.isEmpty()) {
                totalSyncCount += executeBatchUpdate(batchMap);
            }
        } catch (Exception e) {
            log.error("同步浏览量失败",e);
        }
        log.info("同步完成，共同步 {} 条浏览量到数据库", totalSyncCount);
    }

    private int executeBatchUpdate(Map<Long, Integer> batchMap) {
        if (batchMap.isEmpty()) {
            return 0;
        }
        //构建 CASE_WHEN 语句
        StringBuilder caseWhen = new StringBuilder();
        for (Map.Entry<Long, Integer> entry : batchMap.entrySet()) {
            caseWhen.append(String.format("WHEN %d THEN view_count + %d ",
                    entry.getKey(), entry.getValue()));
        }
        // 构建完整的 SET 子句
        String setSql = String.format(
                "view_count = CASE id %s ELSE view_count END",
                caseWhen.toString()
        );
        // 使用 MyBatis-Plus 原生 UpdateWrapper 执行
        UpdateWrapper<Note> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(setSql)
                .in("id", batchMap.keySet());
        boolean result = noteService.update(updateWrapper);

        log.debug("批量更新 {} 条笔记，SQL: {}", batchMap.size(), setSql);

        return result ? batchMap.size() : 0;
    }
}
