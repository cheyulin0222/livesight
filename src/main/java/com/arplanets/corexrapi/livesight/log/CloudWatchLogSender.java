package com.arplanets.corexrapi.livesight.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


@Service
@RequiredArgsConstructor
public class CloudWatchLogSender implements InitializingBean, Runnable {
    @Value("${aws.cloudwatch.log-group-name}")
    private String groupName;
    @Value("${spring.application.name}")
    private String applicationName;

    private final int MAX_RETRIES = 3;
    private static final int MAX_BATCH_SIZE = 50;
    private static final long MAX_FLUSH_TIME_MILLIS = 5000;

    private final CloudWatchLogsAsyncClient awsLogClient;
    private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    private final Map<String, Boolean> streamCreationAttempted = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        String streamKey = getCurrentStream();
        try {
            System.out.println("--- CLOUD WATCH SENDER INIT: Attempting to proactively create Log Stream " + streamKey);
            createLogStreamProactively(streamKey).join();
            System.out.println("--- CLOUD WATCH SENDER INIT: Proactive stream creation completed for " + streamKey + ".");
        } catch (Exception e) {
            System.err.println("--- CLOUD WATCH SENDER INIT ERROR: Failed to initialize/create stream " + streamKey + ": " + e.getMessage());
        }

        Thread senderThread = new Thread(this, "CloudWatchLogSender-Thread");
        senderThread.setDaemon(true);
        senderThread.start();
    }

    @Override
    public void run() {
        System.out.println("CloudWatchLogSender-Thread started.");
        // 為了讓這個執行緒能夠被安全且可控地停止
        while (!Thread.currentThread().isInterrupted()) {
            try {
                sendLogsBatchWithTimeout();
            } catch (InterruptedException e) {
                // 收到中斷訊號，退出循環
                Thread.currentThread().interrupt();
                System.err.println("CloudWatchLogSender-Thread interrupted and stopped.");
                break;
            } catch (Exception e) {
                // 處理發送或處理日誌時發生的意外錯誤
                System.err.println("Fatal error in log sender thread: " + e.getMessage());

                // 為了避免無限次錯誤，暫停 5 秒後重試
                // 使用 BlockingQueue 的 poll 實現退避，同時監聽中斷
                try {
                    // 嘗試從佇列中取出一個元素，但等待 5 秒，如果沒有則返回 null
                    // 這避免了 Thread.sleep()，同時實現了等待 5 秒的目的
                    QueueingAppender.getLogQueue().poll(5000, TimeUnit.MILLISECONDS);

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void sendLogsBatchWithTimeout() throws InterruptedException {
        List<ILoggingEvent> batch = new ArrayList<>();

        // 嘗試從佇列中取出第一條日誌。如果佇列為空，則最多阻塞 (等待) 5 秒。
        ILoggingEvent firstEvent = QueueingAppender.getLogQueue().poll(MAX_FLUSH_TIME_MILLIS, TimeUnit.MILLISECONDS);

        // 若 firstEvent 為 null，執行緒進入下一輪循環。
        if (firstEvent == null) {
            return;
        }

        // 取出第一條日誌將其加入批次
        batch.add(firstEvent);

        // 從佇列中取出剩餘的日誌，直到批次達到 50 條。
        QueueingAppender.getLogQueue().drainTo(batch, MAX_BATCH_SIZE - 1);

        // 計算單一目標 Log Stream 名稱 (使用發送時的時間)
        String streamKey = getCurrentStream();

        List<InputLogEvent> events = batch.stream()
                .map(event -> InputLogEvent.builder()
                        .timestamp(event.getTimeStamp())
                        .message(event.getFormattedMessage())
                        .build()
                )
                .toList();

        sendToCloudWatch(streamKey, events);
    }

    private String getCurrentStream() {
        String currentHourlyDate = LocalDateTime.now(TAIPEI_ZONE).format(DATE_FORMATTER);
        return applicationName + "-logs-" + currentHourlyDate;
    }

    private void sendToCloudWatch(String streamName, List<InputLogEvent> logEvents) {
        // CloudWatch Logs 必須按照其 時間戳 (timestamp) 嚴格地以遞增順序排列。
        List<InputLogEvent> sortedEvent = logEvents.stream()
                .sorted((e1, e2) -> (int) (e1.timestamp() - e2.timestamp()))
                .toList();

        PutLogEventsRequest request = PutLogEventsRequest.builder()
                .logGroupName(groupName)
                .logStreamName(streamName)
                .logEvents(sortedEvent)
                .build();

        sendToCloudWatchInternal(streamName, request, 0);


    }

    private CompletableFuture<Void> sendToCloudWatchInternal(String streamName, PutLogEventsRequest  request, int retryCount) {

        return awsLogClient.putLogEvents(request)
                // 參數類型 Consumer<T>
                // 返回 CompletableFuture<Void>
                // 接收上一步的結果 (T)，執行一個動作，但不傳遞任何新值給下一步
                .thenRun(() -> streamCreationAttempted.remove(streamName))
                .exceptionallyCompose(e -> {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;

                    if (cause instanceof ResourceNotFoundException && !streamCreationAttempted.containsKey(streamName)) {
                        // 首次遇到 ResourceNotFoundException 且尚未嘗試創建
                        return createLogStreamAndRetry(streamName, request);
                    }

                    if (retryCount < MAX_RETRIES) {
                        System.err.println("--- CW SENDER: Log send failed (Attempt " + (retryCount + 1) + "/" + MAX_RETRIES + ") due to: " + cause.getMessage() + ". Retrying...");

                        return sendToCloudWatchInternal(streamName, request, retryCount + 1);

                    } else {
                        // 達到最大重試次數
                        System.err.println("--- CW SENDER FATAL ERROR: Log batch permanently lost after " + MAX_RETRIES + " retries. Final cause: " + cause.getMessage());
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    private CompletableFuture<Void> createLogStreamAndRetry(String streamName, PutLogEventsRequest putLogRequest) {
        // 嘗試創建 Log Stream
        System.out.println("--- CW SENDER: Attempting to create Log Stream: " + streamName);
        CreateLogStreamRequest createStreamRequest = CreateLogStreamRequest.builder()
                .logGroupName(groupName)
                .logStreamName(streamName)
                .build();

        return awsLogClient.createLogStream(createStreamRequest)
                .thenRun(() -> {
                    System.out.println("--- CW SENDER: Log Stream created successfully: " + streamName);
                    streamCreationAttempted.put(streamName, true);
                })
                .exceptionally(e -> {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof ResourceAlreadyExistsException) {
                        streamCreationAttempted.put(streamName, true);
                        return null;
                    }
                    System.err.println("--- CW SENDER FATAL ERROR: Failed to ensure Log Stream existence: " + cause.getMessage());
                    return null;
                })
                .thenCompose(v -> {
                    // 創建成功，重試 PutLogEvents (使用一個新的CompletableFuture)
                    System.out.println("--- CW SENDER: Retrying PutLogEvents after creation.");
                    return sendToCloudWatchInternal(streamName, putLogRequest, 0);

                })
                .exceptionally(e -> {
                    System.err.println("--- CW SENDER FATAL ERROR: Log lost after stream creation failed in retry attempt: " + e.getMessage());
                    return null;
                });

    }

    private CompletableFuture<Void> createLogStreamProactively(String streamName) {
        System.out.println("--- CW SENDER: Attempting to proactively create Log Stream: " + streamName);
        CreateLogStreamRequest request = CreateLogStreamRequest.builder()
                .logGroupName(groupName)
                .logStreamName(streamName)
                .build();

        return awsLogClient.createLogStream(request)
                .thenRun(() -> System.out.println("--- CW SENDER: Proactively created Log Stream: " + streamName))
                .exceptionally(e -> {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof ResourceAlreadyExistsException) {
                        System.out.println("--- CLOUD WATCH SENDER INIT: stream already exists for " + streamName + ".");
                        return null;
                    }
                    System.err.println("--- CW SENDER ERROR: Failed to proactively create Log Stream " + streamName + ": " + cause.getMessage());
                    throw new RuntimeException("Proactive Log Stream creation failed.", cause);
                });
    }




}
