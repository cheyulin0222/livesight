package com.arplanets.corexrapi.livesight.service;

import com.arplanets.corexrapi.livesight.log.LogMessage;
import com.arplanets.corexrapi.livesight.log.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient;
import software.amazon.awssdk.services.iotdataplane.model.PublishRequest;



@Service
@RequiredArgsConstructor
@Slf4j
public class IotService {

    @Value("${aws.iot.topic.prefix}")
    private String iotTopic;

    private final IotDataPlaneClient iotDataPlaneClient;
    private final LoggingService loggingService;

    @Async
    public void sendIotRequest(String topicPath, SdkBytes payload, LogMessage logMessage) {
        String topic = iotTopic + topicPath;

        PublishRequest publishRequest = PublishRequest.builder()
                .topic(topic)
                .qos(1)
                .payload(payload)
                .build();

        try {
            iotDataPlaneClient.publish(publishRequest);
            log.info("Successfully sending async IoT message to topic:{}", topic);
        } catch (Exception e) {
            String message = "Failed to send async IoT message to topic: " + topic + " Error: " + e.getMessage();
            loggingService.errorByInitAPiMessage(logMessage, message, e);
        }
    }

}
