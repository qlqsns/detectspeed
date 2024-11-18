package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.HashMap;
import static com.example.demo.mqtt.mqttSubscriberService.IMAGE_SAVE_PATH;
import java.io.File;

@Controller
public class HomeController {

    private String detectionStatus = "과속 탐지중..";

    public void updateStatus(String newStatus) {
        this.detectionStatus = newStatus;
    }

    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getStatus() {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("detectionStatus", detectionStatus);
        statusData.put("detectionCount", getImageFileCount());
        return statusData;
    }

    public int getImageFileCount() {
        File directory = new File(IMAGE_SAVE_PATH);
        if (directory.exists() && directory.isDirectory()) {
            return directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg")).length;
        }
        return 0;
    }

    @GetMapping("/") // 기본 경로
    public String index(Model model) {
        model.addAttribute("detectionStatus", detectionStatus);
        model.addAttribute("detectionCount", getImageFileCount());
        return "index"; // templates/index.html로 매핑
    }
}

