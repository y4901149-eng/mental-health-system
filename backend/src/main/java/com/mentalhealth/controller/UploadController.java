package com.mentalhealth.controller;

import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    @Value("${server.port:8081}")
    private String port;

    @PostConstruct
    public void init() {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    @PostMapping("/image")
    public ResultVO<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResultVO.badRequest("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + ext;

        try {
            Path dest = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), dest);
            String url = "/uploads/" + fileName;
            return ResultVO.success(url);
        } catch (IOException e) {
            return ResultVO.badRequest("上传失败: " + e.getMessage());
        }
    }
}
