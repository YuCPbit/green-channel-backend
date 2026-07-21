package edu.dashboard.service;

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
     * 广播指定模块的大屏数据
     * @param moduleCode 模块编码 (如: base, workstudy)
     * @param data 模块数据
     */
    public void broadcastModuleStats(String moduleCode, Object data) {
        // 前端订阅地址: /topic/{moduleCode}/stats
        messagingTemplate.convertAndSend("/topic/" + moduleCode + "/stats", data);
    }

    /**
     * 兼容旧方法：广播基础模块数据
     * @param stats 基础模块核心指标
     */
    public void broadcastDashboardStats(Object stats) {
        broadcastModuleStats("base", stats);
    }

    /**
     * 广播全局通知（非数据类消息）
     * @param message 消息内容
     */
    public void broadcastNotification(String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}