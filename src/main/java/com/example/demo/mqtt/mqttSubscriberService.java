package com.example.demo.mqtt;

import com.example.demo.HomeController;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class mqttSubscriberService {

    private double speed;
    private final MqttClient mqttClient;
    private final HomeController homeController;
    public static final String IMAGE_SAVE_PATH = "C:/Users/admin/Desktop/demo/demo/images/";

    @Autowired
    public mqttSubscriberService(MqttClient mqttClient, HomeController homeController) {
        this.mqttClient = mqttClient;
        this.homeController = homeController;
    }

    @PostConstruct
    public void subscribeToTopic() {
        try {
            mqttClient.subscribe("vehicle/overspeed_detection");
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    System.out.println("Received message: " + new String(message.getPayload()));

                    // 메시지를 JSON으로 파싱
                    String payload = new String(message.getPayload());
                    JSONObject json = new JSONObject(payload);

                    // 속도와 이미지를 추출
                    speed = json.getDouble("speed");
                    speed = speed * 100; // cm/s로 변환
                    speed = Math.round(speed * 100.0) / 100.0; // 소수점 셋째 자리에서 반올림

                    String imageBase64 = json.getString("image");

                    // 이미지 저장
                    saveImageFromBase64(imageBase64,speed);

                    homeController.updateStatus("과속 탐지가 1건 발견되었습니다.");

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            homeController.updateStatus("과속 탐지중..");
                        }
                    }, 5000);


                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 메시지 전송이 완료된 경우
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void saveImageFromBase64(String base64Image,Double speed) {
        try {
            // Base64로 인코딩된 이미지를 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = now.format(formatter);
            String imageFileName = "image_" + timestamp + "_" + speed + ".jpg";
            String imagePath = IMAGE_SAVE_PATH + imageFileName;

            try (FileOutputStream fos = new FileOutputStream(imagePath)) {
                fos.write(imageBytes);
                System.out.println("Image saved to: " + imagePath);
            }
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }


}
