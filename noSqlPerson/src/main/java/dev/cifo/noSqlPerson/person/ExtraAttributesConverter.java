package dev.cifo.noSqlPerson.person;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom converter for Map<String, AttributeValue>.
 * Allows storing heterogeneous values (strings, numbers, booleans, lists, nested maps)
 * directly in DynamoDB as a native Map (M) attribute.
 */
public class ExtraAttributesConverter implements AttributeConverter<Map<String, AttributeValue>> {

    @Override
    public AttributeValue transformFrom(Map<String, AttributeValue> input) {
        if (input == null || input.isEmpty()) {
            return AttributeValue.builder().m(Map.of()).build();
        }
        return AttributeValue.builder().m(input).build();
    }

    @Override
    public Map<String, AttributeValue> transformTo(AttributeValue input) {
        if (input == null || !input.hasM()) {
            return new HashMap<>();
        }
        return new HashMap<>(input.m());
    }

    @Override
    public EnhancedType<Map<String, AttributeValue>> type() {
        return EnhancedType.mapOf(EnhancedType.of(String.class), EnhancedType.of(AttributeValue.class));
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.M;
    }
}
