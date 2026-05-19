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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        session.setTitle((title != null && !title.trim().isEmpty()) ? title.trim() : "新的对话");
        session.setStatus("ACTIVE");
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public List<ChatMessage> getSessionMessages(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId) || !"ACTIVE".equals(session.getStatus())) {
            return Collections.emptyList();
        }
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime));
    }

    @Override
    public Map<String, Object> sendMessage(Long sessionId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("消息内容不能为空");
        }

        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId) || !"ACTIVE".equals(session.getStatus())) {
            throw new RuntimeException("会话不存在或无权访问");
        }

        String messageContent = content.trim();

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setSenderRole("USER");
        userMsg.setContent(messageContent);
        messageMapper.insert(userMsg);

        String reply;
        try {
            reply = aiService.getReply(messageContent, userId);
        } catch (Exception e) {
            reply = "抱歉，我现在暂时无法连接到 AI 服务。你可以稍后再试，也可以先把想说的话记录在这里。";
        }

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setSenderRole("ASSISTANT");
        aiMsg.setContent(reply);
        messageMapper.insert(aiMsg);

        long count = messageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId));
        if (count <= 2) {
            session.setTitle(buildSessionTitle(messageContent));
        }
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);

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

    private String buildSessionTitle(String content) {
        String compact = content.replaceAll("\\s+", " ");
        return compact.length() > 20 ? compact.substring(0, 20) + "..." : compact;
    }
}
