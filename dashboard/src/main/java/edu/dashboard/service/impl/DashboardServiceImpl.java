package edu.dashboard.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dashboard.domain.vo.*;
import edu.dashboard.mapper.DashboardMapper;
import edu.dashboard.service.DashboardService;
import edu.dashboard.service.WebSocketService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats";

    /**
     * FR-3.14-002: 获取缓存数据，若没有则查询数据库
     */
    @Override
    public DashboardStatsVO getCachedStats() {
        Object cached = redisTemplate.opsForValue().get(DASHBOARD_CACHE_KEY);
        if (cached != null) {
            return objectMapper.convertValue(cached, DashboardStatsVO.class);
        }
        // 防止缓存击穿，这里简单处理，实际可加锁
        DashboardStatsVO stats = dashboardMapper.selectCoreStats();
        refreshCache(stats);
        return stats;
    }

    /**
     * 定时刷新缓存并推送WebSocket (每分钟)
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void refreshAndPushStats() {
        log.info("Refreshing dashboard stats...");
        DashboardStatsVO stats = dashboardMapper.selectCoreStats();
        refreshCache(stats);
        webSocketService.broadcastDashboardStats(stats);
    }

    private void refreshCache(DashboardStatsVO stats) {
        redisTemplate.opsForValue().set(DASHBOARD_CACHE_KEY, stats, Duration.ofMinutes(5));
    }

    @Override
    public List<CollegeCompareVO> getCollegeComparison() {
        return dashboardMapper.selectCollegeCompare();
    }

    @Override
    public List<HeatmapVO> getOriginHeatmapData() {
        return dashboardMapper.selectOriginHeatmap();
    }

    @Override
    public List<SubsidyStructureVO> getSubsidyStructure() {
        return dashboardMapper.selectSubsidyStructure();
    }

    @Override
    public List<Map<String, Object>> getYearlyTrend(String currentYear) {
        return dashboardMapper.selectYearlyTrend(currentYear);
    }
}