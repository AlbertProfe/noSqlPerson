package dev.cifo.noSqlPerson;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PersonService {

    private final DynamoDbTable<Person> personTable;

    public PersonService(DynamoDbEnhancedClient enhancedClient) {
        // Connect the Person class to your DynamoDB table
        this.personTable = enhancedClient.table("person",
                TableSchema.fromBean(Person.class));
    }

    /**
     * Get all persons from the DynamoDB table.
     * @return
     */
    public PageIterable<Person> getAllPersons() {
        PageIterable<Person> people = personTable.scan();
        return people;
    }

    /**
     * Get a specific page of persons from DynamoDB.
     * @param pageSize The number of items per page
     * @return A single Page of Person objects
     */
    /*public Page<Person> getPersonPage(int pageSize) {
        PageIterable<Person> people = personTable.scan();
        return people.stream()
                .findFirst()
                .orElse(null);
    }*/

    /**
     * Save or update a Person.
     * If a person with the same id + operation already exists, it will be replaced.
     */
    public Person save(Person person) {
        String id = UUID.randomUUID().toString();
        if (person.getId() == null || person.getId().isBlank()) {
            person.setId(id);
        }

        if (person.getOperation() == null || person.getOperation().isBlank()) {
            person.setOperation("CREATE");   // Default operation if none provided
        }

        person.setCreatedAt(Instant.now());

        personTable.putItem(person); // Save to DynamoDB

        Key key = Key.builder()
                .partitionValue(person.getId())
                .sortValue(person.getOperation())
                .build();

        System.out.println("Person saved: " + getPersonByKey(id, "CREATE"));

        return person;
    }

    /**
     * Get a person by their composite key (id + operation).
     * @param id The partition key
     * @param operation The sort key
     * @return The person if found, null otherwise
     */
    public Person getPersonByKey(String id, String operation) {
        Key key = Key.builder()
                .partitionValue(id)
                .sortValue(operation)
                .build();
        
        return personTable.getItem(key);
    }
}
