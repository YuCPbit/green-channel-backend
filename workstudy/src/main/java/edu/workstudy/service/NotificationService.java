package edu.workstudy.service;

/**
 * 消息通知适配器接口
 * 职责：将勤工助学模块的消息需求，转换为消息中心能听懂的语言。
 * 实现方：消息模块的同学（或者你自己先写个Mock实现）
 */
public interface NotificationService {

    /**
     * 发送预警消息
     * @param receiverId 接收人ID（如：资助中心管理员ID）
     * @param title 消息标题
     * @param content 消息内容
     * @param businessKey 业务主键（如：学生ID，用于消息中心跳转详情）
     */
    void sendWarning(Long receiverId, String title, String content, String businessKey);

    /**
     * 发送业务通知（如：录用成功通知、薪酬到账通知）
     * @param receiverId 接收人ID（如：学生ID）
     * @param title 消息标题
     * @param content 消息内容
     * @param businessKey 业务主键（如：协议ID、薪酬ID）
     */
    void sendNotice(Long receiverId, String title, String content, String businessKey);
}