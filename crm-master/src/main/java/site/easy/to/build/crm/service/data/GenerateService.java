package site.easy.to.build.crm.service.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class GenerateService {
    @PersistenceContext
    private EntityManager entityManager;

    private final Random random = new Random();

    private final List<String> firstNames = Arrays.asList("Alice", "Bob", "Charlie", "David", "Emma", "Fiona", "George", "Hannah", "Ian", "Julia");
    private final List<String> lastNames = Arrays.asList("Smith", "Johnson", "Brown", "Williams", "Jones", "Miller", "Davis", "Garcia", "Martinez", "Taylor");

    @Transactional
    public void generateRandomData(String tableName, int minRows, int maxRows) {
        // Récupérer les informations sur les colonnes
        List<Object[]> columns = entityManager.createNativeQuery(
                        "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :tableName")
                .setParameter("tableName", tableName)
                .getResultList();

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("La table " + tableName + " n'existe pas !");
        }

        // Identifier la colonne de clé primaire auto-incrémentée
        String primaryKeyColumn = columns.stream()
                .filter(column -> "PRI".equals(column[2]) && "auto_increment".equalsIgnoreCase((String) column[3]))
                .map(column -> (String) column[0])
                .findFirst()
                .orElse(null);

        int numRows = random.nextInt(maxRows - minRows + 1) + minRows;

        for (int i = 0; i < numRows; i++) {
            insertRandomRow(tableName, columns, primaryKeyColumn);
        }
    }

    private void insertRandomRow(String tableName, List<Object[]> columns, String primaryKeyColumn) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder(" VALUES (");

        String firstName = getRandomFirstName();
        String lastName = getRandomLastName();
        String username = (firstName.charAt(0) + lastName).toLowerCase();
        String email = (firstName + "." + lastName + "@example.com").toLowerCase();

        for (Object[] column : columns) {
            String columnName = (String) column[0];
            String dataType = (String) column[1];

            // Ignorer la colonne de clé primaire auto-incrémentée
            if (columnName.equals(primaryKeyColumn)) {
                continue;
            }

            sql.append(columnName).append(", ");
            values.append(generateRandomValue(tableName, columnName, dataType, firstName, lastName, username, email)).append(", ");
        }

        sql.setLength(sql.length() - 2);
        values.setLength(values.length() - 2);
        sql.append(")").append(values).append(")");

        entityManager.createNativeQuery(sql.toString()).executeUpdate();
    }

    private String generateRandomValue(String tableName, String columnName, String dataType, String firstName, String lastName, String username, String email) {
        if (isForeignKey(tableName, columnName)) {
            String referencedTable = getReferencedTable(tableName, columnName);
            String referencedColumn = getReferencedColumn(tableName, columnName);
            return String.valueOf(getRandomForeignKey(referencedTable, referencedColumn));
        }

        switch (dataType.toLowerCase()) {
            case "int":
            case "bigint":
            case "smallint":
                return String.valueOf(random.nextInt(10000));

            case "varchar":
            case "text":
            case "char":
                if (tableName.equalsIgnoreCase("trigger_ticket") && columnName.equalsIgnoreCase("status")) {
                    List<String> ticketStatuses = Arrays.asList("Open", "assigned", "On Hold", "In Progress", "Resolved", "Closed", "Reopened", "Pending Customer Response", "Escalated", "Archived");
                    return "'" + ticketStatuses.get(random.nextInt(ticketStatuses.size())) + "'";
                } else if (tableName.equalsIgnoreCase("trigger_lead") && columnName.equalsIgnoreCase("status")) {
                    List<String> leadStatuses = Arrays.asList("Meeting to be scheduled", "Assign to sales representative","Archived","success");
                    return "'" + leadStatuses.get(random.nextInt(leadStatuses.size())) + "'";
                } else {
                    return switch (columnName.toLowerCase()) {
                        case "username" -> "'" + username + "'";
                        case "first_name" -> "'" + firstName + "'";
                        case "last_name" -> "'" + lastName + "'";
                        case "email" -> "'" + email + "'";
                        case "password" -> "'" + getRandomString(10) + "'";
                        case "provider" -> "'local'";
                        case "name" -> "'" + firstName + " " + lastName + "'";
                        case "phone" -> "'" + getRandomPhoneNumber() + "'";
                        case "address" -> "'" + getRandomAddress() + "'";
                        case "city" -> "'" + getRandomCity() + "'";
                        case "state" -> "'" + getRandomState() + "'";
                        case "country" -> "'" + getRandomCountry() + "'";
                        case "position" -> "'" + getRandomPosition() + "'";
                        case "department" -> "'" + getRandomDepartment() + "'";
                        case "subject" -> "'" + getRandomSubject() + "'";
                        case "description" -> "'" + getRandomDescription() + "'";
                        case "twitter" -> "'" + getRandomTwitter() + "'";
                        case "facebook" -> "'" + getRandomFacebook() + "'";
                        case "youtube" -> "'" + getRandomYoutube() + "'";
                        default -> "'" + getRandomString(10) + "'";
                    };
                }

            case "boolean":
            case "tinyint(1)":
                return random.nextBoolean() ? "1" : "0";

            case "date":
                return "'" + getRandomDate() + "'";

            case "datetime":
            case "timestamp":
                return "'" + getRandomDateTime() + "'";

            case "decimal":
                return String.valueOf(random.nextDouble() * 10000);

            default:
                return "NULL";
        }
    }

    private String getReferencedTable(String tableName, String columnName) {
        Query query = entityManager.createNativeQuery(
                "SELECT REFERENCED_TABLE_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                        "AND COLUMN_NAME = ? AND REFERENCED_TABLE_NAME IS NOT NULL");
        query.setParameter(1, tableName);
        query.setParameter(2, columnName);
        return (String) query.getSingleResult();
    }

    private String getReferencedColumn(String tableName, String columnName) {
        Query query = entityManager.createNativeQuery(
                "SELECT REFERENCED_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                        "AND COLUMN_NAME = ? AND REFERENCED_TABLE_NAME IS NOT NULL");
        query.setParameter(1, tableName);
        query.setParameter(2, columnName);
        return (String) query.getSingleResult();
    }

    private boolean isForeignKey(String tableName, String columnName) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                        "AND COLUMN_NAME = ? AND REFERENCED_TABLE_NAME IS NOT NULL");
        query.setParameter(1, tableName);
        query.setParameter(2, columnName);
        return ((Number) query.getSingleResult()).intValue() > 0;
    }

    private Integer getRandomForeignKey(String tableName, String columnName) {
        List<Integer> ids = entityManager.createNativeQuery("SELECT " + columnName + " FROM " + tableName)
                .getResultList();
        return ids.isEmpty() ? null : ids.get(random.nextInt(ids.size()));
    }

    private String getRandomFirstName() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }

    private String getRandomLastName() {
        return lastNames.get(random.nextInt(lastNames.size()));
    }

    private String getRandomDate() {
        int year = 2022 + random.nextInt(4);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private String getRandomDateTime() {
        return getRandomDate() + " " + getRandomTime();
    }

    private String getRandomTime() {
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    private String getRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String getRandomPhoneNumber() {
        return String.format("%03d-%03d-%04d", random.nextInt(1000), random.nextInt(1000), random.nextInt(10000));
    }

    private String getRandomAddress() {
        return random.nextInt(9999) + " " + getRandomState() + " St" + getRandomCountry();
    }

    private String getRandomCity() {
        List<String> cities = Arrays.asList("New York", "Los Angeles", "Chicago", "Houston", "Phoenix");
        return cities.get(random.nextInt(cities.size()));
    }

    private String getRandomState() {
        List<String> states = Arrays.asList("NY", "CA", "IL", "TX", "AZ");
        return states.get(random.nextInt(states.size()));
    }

    private String getRandomCountry() {
        List<String> countries = Arrays.asList("USA", "Canada", "UK", "Australia", "Germany");
        return countries.get(random.nextInt(countries.size()));
    }

    private String getRandomPosition() {
        List<String> positions = Arrays.asList("Manager", "Developer", "Analyst", "Tester", "Designer");
        return positions.get(random.nextInt(positions.size()));
    }

    private String getRandomDepartment() {
        List<String> departments = Arrays.asList("HR", "IT", "Finance", "Marketing", "Sales");
        return departments.get(random.nextInt(departments.size()));
    }

    private String getRandomStatus() {
        List<String> statuses = Arrays.asList("Open", "assigned", "On Hold", "In Progress", "Resolved", "Closed", "Reopened", "Pending Customer Response", "Escalated", "Archived");
        return statuses.get(random.nextInt(statuses.size()));
    }

    private String getRandomSubject() {
        List<String> subjects = Arrays.asList("Meeting", "Project Update", "Client Call", "Team Outing", "Training");
        return subjects.get(random.nextInt(subjects.size()));
    }

    private String getRandomDescription() {
        List<String> descriptions = Arrays.asList("Description 1", "Description 2", "Description 3", "Description 4", "Description 5");
        return descriptions.get(random.nextInt(descriptions.size()));
    }

    private String getRandomTwitter() {
        return "https://twitter.com/" + getRandomString(8);
    }

    private String getRandomFacebook() {
        return "https://facebook.com/" + getRandomString(8);
    }

    private String getRandomYoutube() {
        return "https://youtube.com/" + getRandomString(8);
    }
}
