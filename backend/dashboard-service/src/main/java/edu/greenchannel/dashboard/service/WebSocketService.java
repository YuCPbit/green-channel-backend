package edu.greenchannel.dashboard.service;

import edu.greenchannel.dashboard.domain.vo.DashboardStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * FR-3.14-002: 广播大屏数据
     */
    public void broadcastDashboardStats(DashboardStatsVO stats) {
        broadcastModuleStats("base", stats);
    }

    public void broadcastModuleStats(String moduleCode, Object stats) {
        messagingTemplate.convertAndSend("/topic/" + moduleCode + "/stats", stats);
    }
}
