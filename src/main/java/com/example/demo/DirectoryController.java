package com.example.demo;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Controller
public class DirectoryController {

    private static final String TARGET_DIR = "C:\\Users\\admin\\Desktop\\demo\\demo\\images"; // 열고자 하는 디렉터리 경로

    @GetMapping("/open-directory")
    public String openDirectory(Model model) {
        File directory = new File(TARGET_DIR);

        if (directory.exists() && directory.isDirectory()) {
            List<Map<String, String>> fileDetails = new ArrayList<>();

            // 파일 목록을 가져오고 날짜, 속도 추출
            Arrays.stream(directory.listFiles())
                    .filter(file -> file.getName().endsWith(".jpg"))  // jpg 파일만 처리
                    .forEach(file -> {
                        String fileName = file.getName();
                        // 파일 이름에서 날짜와 속도 추출 (예: image_20231118_153022_123.45.jpg)
                        String[] parts = fileName.split("_");

                        if (parts.length >= 4) {
                            String timestamp = parts[1] + " " + parts[2]; // 날짜와 시간
                            String speed = parts[3].replace(".jpg", "") + " cm/s"; // 속도 (단위 포함)

                            // 파일과 관련된 정보를 Map으로 저장
                            Map<String, String> fileData = new HashMap<>();
                            fileData.put("fileName", fileName);
                            fileData.put("timestamp", timestamp);
                            fileData.put("speed", speed);

                            fileDetails.add(fileData);
                        }
                    });

            model.addAttribute("fileDetails", fileDetails); // 파일 목록 및 추가 정보 모델에 전달
        } else {
            model.addAttribute("error", "Directory not found");
        }
        return "directory"; // templates/directory.html로 렌더링
    }


    @GetMapping("/images/{fileName}")
    @ResponseBody
    public ResponseEntity<Resource> serveImage(@PathVariable String fileName) throws IOException {
        File file = new File(TARGET_DIR, fileName);
        if (file.exists() && file.isFile()) {
            // 파일의 MIME 타입을 자동으로 감지
            String mimeType = null;
            try {
                mimeType = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // 기본 MIME 타입
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType)) // MIME 타입 설정
                    .header("Content-Disposition", "inline; filename=\"" + file.getName() + "\"") // 인라인 표시
                    .body(resource); // 이미지를 리소스로 반환
        } else {
            return ResponseEntity.notFound().build(); // 파일이 없을 경우 404 응답
        }
    }
}
