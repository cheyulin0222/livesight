package com.arplanets.corexrapi.livesight.repository.impl.dynamodbsdk;

import com.arplanets.corexrapi.livesight.model.po.LiveSightPo;
import com.arplanets.corexrapi.livesight.repository.LiveSightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
@Slf4j
public class AwsDynamoDbSdkLiveSightRepository implements LiveSightRepository {

    @Value("${aws.live.sight.table.name}")
    private String tableName;

    private static final String PK_ATTRIBUTE_NAME = "pk";
    private static final String SK_ATTRIBUTE_NAME = "sk";
    private static final String SK_VALUE = "info";

    private final DynamoDbClient dynamoDbClient;

    @Override
    public LiveSightPo create(LiveSightPo liveSight) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(liveSight.getLiveSightId()).build());
        item.put(SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build());
        item.put("live_sight_id", AttributeValue.builder().s(liveSight.getLiveSightId()).build());
        item.put("org_id", AttributeValue.builder().s(liveSight.getOrgId()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        try {
            dynamoDbClient.putItem(putItemRequest);
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Failed to added item to DynamoDB", e);
        }

        return liveSight;
    }

    @Override
    public Optional<LiveSightPo> findById(String liveSightId) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(liveSightId).build(),
                        SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build()))
                .build();


        GetItemResponse response;
        try {
            response = dynamoDbClient.getItem(getItemRequest);
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Error fetching item from DynamoDB", e);
        }

        if (response.hasItem()) {
            Map<String, AttributeValue> item = response.item();
            return Optional.of(LiveSightPo.builder()
                    .liveSightId(item.get("live_sight_id") != null ? item.get("live_sight_id").s() : null)
                    .orgId(item.get("org_id") != null ? item.get("org_id").s() : null)
                    .build());
        }

        return Optional.empty();
    }
}
