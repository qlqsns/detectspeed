package com.example.demo.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class mqttConfig {

    private static final String BROKER_URL = "tcp://192.168.137.16:1883"; // 브로커 주소 (포트는 기본 1883)
    private static final String CLIENT_ID = "springboot-client";


    @Bean
    public MqttClient mqttClient() {
        MqttClient client = null;
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            // MQTT 연결
            client.connect(options);
            System.out.println("MQTT 브로커에 성공적으로 연결되었습니다! (" + BROKER_URL + ")");
        } catch (MqttException e) {
            // 연결 실패 시 로그 출력하고 클라이언트를 null로 설정
            System.err.println("MQTT 브로커 연결 실패: " + e.getMessage());
        }
        return client; // 연결 실패 시 null 반환
    }

}
