package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.entity.ChatSession;
import com.mentalhealth.mapper.ChatMessageMapper;
import com.mentalhealth.mapper.ChatSessionMapper;
import com.mentalhealth.service.AIService;
import com.mentalhealth.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatSessionMapper sessionMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Autowired
    private AIService aiService;

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getStatus, "ACTIVE")
                        .orderByDesc(ChatSession::getUpdateTime));
    }

    @Override
    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新的对话");
        session.setStatus("ACTIVE");
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public List<ChatMessage> getSessionMessages(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) return Collections.emptyList();
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime));
    }

    @Override
    public Map<String, Object> sendMessage(Long sessionId, Long userId, String content) {
        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setSenderRole("USER");
        userMsg.setContent(content);
        messageMapper.insert(userMsg);

        // 调用 AI 服务（真实 AI → 失败时自动 fallback 到 mock）
        String reply = aiService.getReply(content, userId);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setSenderRole("ASSISTANT");
        aiMsg.setContent(reply);
        messageMapper.insert(aiMsg);

        // 更新会话标题（基于第一条消息）
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            long count = messageMapper.selectCount(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getSessionId, sessionId));
            if (count <= 2) {
                String title = content.length() > 20 ? content.substring(0, 20) + "..." : content;
                session.setTitle(title);
            }
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userMessage", userMsg);
        result.put("aiMessage", aiMsg);
        return result;
    }

    @Override
    public void deleteSession(Long id, Long userId) {
        ChatSession session = sessionMapper.selectById(id);
        if (session != null && session.getUserId().equals(userId)) {
            session.setStatus("DELETED");
            sessionMapper.updateById(session);
        }
    }
}
