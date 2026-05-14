package com.mentalhealth.service;

import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.entity.ChatSession;
import java.util.List;
import java.util.Map;

public interface ChatService {
    /** 获取用户的会话列表 */
    List<ChatSession> getUserSessions(Long userId);
    /** 创建新会话 */
    ChatSession createSession(Long userId, String title);
    /** 获取会话消息 */
    List<ChatMessage> getSessionMessages(Long sessionId, Long userId);
    /** 发送消息并获取 AI 回复 */
    Map<String, Object> sendMessage(Long sessionId, Long userId, String content);
    /** 删除会话 */
    void deleteSession(Long id, Long userId);
}
