package com.mentalhealth.controller;

import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.entity.ChatSession;
import com.mentalhealth.service.ChatService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/sessions")
    public ResultVO<List<ChatSession>> sessions(HttpServletRequest request) {
        return ResultVO.success(chatService.getUserSessions((Long) request.getAttribute("userId")));
    }

    @PostMapping("/session/create")
    public ResultVO<ChatSession> createSession(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String title = body == null ? null : body.get("title");
        return ResultVO.success(chatService.createSession((Long) request.getAttribute("userId"), title));
    }

    @GetMapping("/session/{id}/messages")
    public ResultVO<List<ChatMessage>> messages(@PathVariable Long id, HttpServletRequest request) {
        return ResultVO.success(chatService.getSessionMessages(id, (Long) request.getAttribute("userId")));
    }

    @PostMapping("/session/{id}/send")
    public ResultVO<Map<String, Object>> send(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String content = body == null ? null : body.get("content");
        return ResultVO.success(chatService.sendMessage(id, (Long) request.getAttribute("userId"), content));
    }

    @DeleteMapping("/session/{id}")
    public ResultVO<?> deleteSession(@PathVariable Long id, HttpServletRequest request) {
        chatService.deleteSession(id, (Long) request.getAttribute("userId"));
        return ResultVO.success();
    }
}
