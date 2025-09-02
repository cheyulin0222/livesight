package com.arplanets.LiveSight.authorization.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogProducer {

    @Value("${aws.sqs.audit-log-queue-url}")
    private String auditLogQueueUrl;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final BlockingQueue<LogMessage> logQueue = new LinkedBlockingQueue<>();

    private static final int BATCH_SIZE = 10;
    private static final long BATCH_SEND_INTERVAL_MS = 5000;

    public void queueLog(LogMessage entry) {
        try {
            logQueue.put(entry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新設置中斷狀態
            log.error("Failed to queue audit log entry: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = BATCH_SEND_INTERVAL_MS)
    public void sendQueuedLogs() {
        if (logQueue.isEmpty()) {
            return;
        }

        List<LogMessage> messagesToSend = new ArrayList<>();
        // 將佇列中所有可用的元素（或指定數量的元素）從佇列中取出，並轉移到另一個 Collection 中
        logQueue.drainTo(messagesToSend, BATCH_SIZE);

        if (messagesToSend.isEmpty()) {
            return;
        }

        try {
            List<SendMessageBatchRequestEntry> sqsBatchEntries = new ArrayList<>();
            for (LogMessage message : messagesToSend) {
                String messageBody = objectMapper.writeValueAsString(message);
                sqsBatchEntries.add(SendMessageBatchRequestEntry.builder()
                                .id(message.getLogSn())
                                .messageBody(messageBody)
                        .build());
            }

            SendMessageBatchRequest sendBatchRequest = SendMessageBatchRequest.builder()
                    .queueUrl(auditLogQueueUrl)
                    .entries(sqsBatchEntries)
                    .build();

            sqsClient.sendMessageBatch(sendBatchRequest);
            log.info("Successfully sending queue.");
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log entry to JSON: {}", e.getMessage(), e);
        } catch (SqsException e) {
            log.error("Failed to send audit logs to SQS: {}", e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending audit logs: {}", e.getMessage(), e);
        }
    }
}
