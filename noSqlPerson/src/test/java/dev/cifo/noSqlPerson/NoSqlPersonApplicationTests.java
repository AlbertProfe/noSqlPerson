package dev.cifo.noSqlPerson;

import dev.cifo.noSqlPerson.person.Person;
import dev.cifo.noSqlPerson.person.PersonEventPublisher;
import dev.cifo.noSqlPerson.person.PersonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.List;

class NoSqlPersonApplicationTests {

	static PersonService personService;

	@BeforeAll
	static void setup() {
		DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
		DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
				.dynamoDbClient(dynamoDbClient)
				.build();
		PersonEventPublisher eventPublisher = new PersonEventPublisher();
		personService = new PersonService(enhancedClient, eventPublisher);
	}

	@Test
	void createPerson() {
		String id = "WEB_APP_2030";
		List<Person> persons = new ArrayList<>();

		// 500 students
		long genStart = System.currentTimeMillis();
		for (int i = 1; i <= 1000; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("STUDENT#%04d", i));
			p.setName("Student " + i);
			p.setAge(18 + (i % 10));
			p.setEmail("student" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("STUDENT#" + i);
			persons.add(p);
		}

		// 40 teachers
		for (int i = 1; i <= 400; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("TEACHER#%04d", i));
			p.setName("Teacher " + i);
			p.setAge(30 + (i % 20));
			p.setEmail("teacher" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("TEACHER#" + i);
			persons.add(p);
		}

		// 50 staff
		for (int i = 1; i <= 500; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("STAFF#%04d", i));
			p.setName("Staff " + i);
			p.setAge(25 + (i % 15));
			p.setEmail("staff" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("STAFF#" + i);
			persons.add(p);
		}

		// 50 legal
		for (int i = 1; i <= 500; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("LEGAL#%04d", i));
			p.setName("Legal " + i);
			p.setAge(28 + (i % 12));
			p.setEmail("legal" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("LEGAL#" + i);
			persons.add(p);
		}
		long genEnd = System.currentTimeMillis();

		System.out.println("========== FAKE DATA GENERATION ==========");
		System.out.println("Total persons generated: " + persons.size());
		System.out.println("Generation time: " + (genEnd - genStart) + " ms");

		// Batch write with latency metrics
		long writeStart = System.currentTimeMillis();
		int batchCount = personService.saveBatch(persons);
		long writeEnd = System.currentTimeMillis();

		long totalWriteMs = writeEnd - writeStart;
		double avgPerItem = (double) totalWriteMs / persons.size();
		double avgPerBatch = (double) totalWriteMs / batchCount;

		System.out.println("========== BATCH WRITE METRICS ==========");
		System.out.println("Total items written: " + persons.size());
		System.out.println("  - Students: 500");
		System.out.println("  - Teachers: 40");
		System.out.println("  - Staff:    50");
		System.out.println("  - Legal:    50");
		System.out.println("Batches (25 items each): " + batchCount);
		System.out.println("Total write time:       " + totalWriteMs + " ms");
		System.out.println("Avg latency per item:   " + String.format("%.2f", avgPerItem) + " ms");
		System.out.println("Avg latency per batch:  " + String.format("%.2f", avgPerBatch) + " ms");
		System.out.println("Throughput:             " + String.format("%.0f", persons.size() / (totalWriteMs / 1000.0)) + " items/sec");
		System.out.println("==========================================");

		// Print sample data from each category
		System.out.println("\n--- Sample Data (2 per category) ---");
		persons.stream().filter(p -> p.getCourseItem().startsWith("STUDENT")).limit(2).forEach(System.out::println);
		persons.stream().filter(p -> p.getCourseItem().startsWith("TEACHER")).limit(2).forEach(System.out::println);
		persons.stream().filter(p -> p.getCourseItem().startsWith("STAFF")).limit(2).forEach(System.out::println);
		persons.stream().filter(p -> p.getCourseItem().startsWith("LEGAL")).limit(2).forEach(System.out::println);
	}

	@Test
	void getAllPersonsByCourseId() {
		String id = "WEB_APP_2027";

		// Query all items for this partition key
		long queryStart = System.currentTimeMillis();
		List<Person> persons = new ArrayList<>();
		personService.queryById(id).forEach(page -> persons.addAll(page.items()));
		long queryEnd = System.currentTimeMillis();

		long totalQueryMs = queryEnd - queryStart;

		// Count by category
		long students = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("STUDENT")).count();
		long teachers = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("TEACHER")).count();
		long staff = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("STAFF")).count();
		long legal = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("LEGAL")).count();

		System.out.println("========== QUERY BY COURSE ID ==========");
		System.out.println("CourseId: " + id);
		System.out.println("Total items retrieved: " + persons.size());
		System.out.println("  - Students: " + students);
		System.out.println("  - Teachers: " + teachers);
		System.out.println("  - Staff:    " + staff);
		System.out.println("  - Legal:    " + legal);
		System.out.println("Total query time:       " + totalQueryMs + " ms");
		System.out.println("Avg latency per item:   " + String.format("%.2f", (double) totalQueryMs / persons.size()) + " ms");
		System.out.println("Throughput:             " + String.format("%.0f", persons.size() / (totalQueryMs / 1000.0)) + " items/sec");
		System.out.println("==========================================");

		// Print sample data from each category
		System.out.println("\n--- Sample Data (2 per category) ---");
		persons.stream().filter(p -> p.getCourseItem().startsWith("STUDENT")).limit(2).forEach(System.out::println);
		persons.stream().filter(p -> p.getCourseItem().startsWith("TEACHER")).limit(2).forEach(System.out::println);
		persons.stream().filter(p -> p.getCourseItem().startsWith("STAFF")).limit(2).forEach(System.out::println);
		persons.stream().filter(p -> p.getCourseItem().startsWith("LEGAL")).limit(2).forEach(System.out::println);
	}

	@Test
	void createPersonWithExtraAttributes() {
		String id = "WEB_APP_EXTRA_2030";
		List<Person> persons = new ArrayList<>();

		// 5 students with extra attributes: gpa, enrolled, notes
		for (int i = 1; i <= 5; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("STUDENT#%04d", i));
			p.setName("Student Extra " + i);
			p.setAge(18 + (i % 10));
			p.setEmail("student.extra" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("STUDENT#" + i);
			p.addExtraNumber("gpa", 3.0 + (i % 10) * 0.1);
			p.addExtraBool("enrolled", i % 2 == 0);
			p.addExtraString("notes", "Semester " + (i % 4 + 1) + " student");
			persons.add(p);
		}

		// 5 teachers with extra attributes: department, yearsExperience, tenured
		for (int i = 1; i <= 5; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("TEACHER#%04d", i));
			p.setName("Teacher Extra " + i);
			p.setAge(30 + (i % 20));
			p.setEmail("teacher.extra" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("TEACHER#" + i);
			p.addExtraString("department", i % 2 == 0 ? "Science" : "Humanities");
			p.addExtraNumber("yearsExperience", 5 + i);
			p.addExtraBool("tenured", i > 3);
			persons.add(p);
		}

		// 5 staff with extra attributes: role, floor, fullTime
		for (int i = 1; i <= 5; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("STAFF#%04d", i));
			p.setName("Staff Extra " + i);
			p.setAge(25 + (i % 15));
			p.setEmail("staff.extra" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("STAFF#" + i);
			p.addExtraString("role", i % 2 == 0 ? "Admin" : "Maintenance");
			p.addExtraNumber("floor", i);
			p.addExtraBool("fullTime", true);
			persons.add(p);
		}

		// 5 legal with extra attributes: barNumber, specialty, active
		for (int i = 1; i <= 5; i++) {
			Person p = new Person();
			p.setCourseId(id);
			p.setCourseItem(String.format("LEGAL#%04d", i));
			p.setName("Legal Extra " + i);
			p.setAge(28 + (i % 12));
			p.setEmail("legal.extra" + i + "@school.dev");
			p.setSchoolId("SCHOOL_BCN");
			p.setSchoolItem("LEGAL#" + i);
			p.addExtraString("barNumber", "BAR-" + String.format("%05d", 10000 + i));
			p.addExtraString("specialty", i % 2 == 0 ? "Contract Law" : "Education Law");
			p.addExtraBool("active", i != 3);
			persons.add(p);
		}

		System.out.println("========== EXTRA ATTRIBUTES TEST ==========");
		System.out.println("Total persons generated: " + persons.size());

		// Batch write
		long writeStart = System.currentTimeMillis();
		int batchCount = personService.saveBatch(persons);
		long writeEnd = System.currentTimeMillis();

		System.out.println("Batches processed: " + batchCount);
		System.out.println("Write time: " + (writeEnd - writeStart) + " ms");

		// Read back and verify extraAttributes survived the round-trip
		System.out.println("\n--- Round-trip verification ---");
		for (Person p : persons) {
			Person fetched = personService.getPersonByKey(p.getCourseId(), p.getCourseItem());
			System.out.println(fetched.getCourseItem()
					+ " | extraAttributes=" + fetched.getExtraAttributes());
		}
		System.out.println("===========================================");
	}

	@Test
	void getAllPersonsByCourseIdWithExtraAttributes() {
		String id = "WEB_APP_EXTRA_2030";

		// Query all items for this partition key
		long queryStart = System.currentTimeMillis();
		List<Person> persons = new ArrayList<>();
		personService.queryById(id).forEach(page -> persons.addAll(page.items()));
		long queryEnd = System.currentTimeMillis();

		long totalQueryMs = queryEnd - queryStart;

		// Count by category
		long students = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("STUDENT")).count();
		long teachers = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("TEACHER")).count();
		long staff = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("STAFF")).count();
		long legal = persons.stream().filter(p -> p.getCourseItem() != null && p.getCourseItem().startsWith("LEGAL")).count();

		System.out.println("========== QUERY BY COURSE ID (WITH EXTRA ATTRIBUTES) ==========");
		System.out.println("CourseId: " + id);
		System.out.println("Total items retrieved: " + persons.size());
		System.out.println("  - Students: " + students);
		System.out.println("  - Teachers: " + teachers);
		System.out.println("  - Staff:    " + staff);
		System.out.println("  - Legal:    " + legal);
		System.out.println("Total query time:       " + totalQueryMs + " ms");
		System.out.println("Avg latency per item:   " + String.format("%.2f", (double) totalQueryMs / persons.size()) + " ms");
		System.out.println("Throughput:             " + String.format("%.0f", persons.size() / (totalQueryMs / 1000.0)) + " items/sec");
		System.out.println("================================================================");

		// Print sample data with extraAttributes from each category
		System.out.println("\n--- Sample Data with Extra Attributes (2 per category) ---");
		persons.stream().filter(p -> p.getCourseItem().startsWith("STUDENT")).limit(2).forEach(p ->
				System.out.println(p + "  extraAttributes=" + p.getExtraAttributes()));
		persons.stream().filter(p -> p.getCourseItem().startsWith("TEACHER")).limit(2).forEach(p ->
				System.out.println(p + "  extraAttributes=" + p.getExtraAttributes()));
		persons.stream().filter(p -> p.getCourseItem().startsWith("STAFF")).limit(2).forEach(p ->
				System.out.println(p + "  extraAttributes=" + p.getExtraAttributes()));
		persons.stream().filter(p -> p.getCourseItem().startsWith("LEGAL")).limit(2).forEach(p ->
				System.out.println(p + "  extraAttributes=" + p.getExtraAttributes()));
	}
}
