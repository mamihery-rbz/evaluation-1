package site.easy.to.build.crm.util;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static <T> List<T> parseCsv(MultipartFile file, Class<T> clazz, char separator) {
        List<T> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(separator))) {  // Spécifiez le séparateur ici

            for (CSVRecord csvRecord : csvParser) {
                T instance = clazz.getDeclaredConstructor().newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String columnName = getColumnName(field);

                    if (csvRecord.isMapped(columnName)) {
                        String value = csvRecord.get(columnName).trim();

                        if (!value.isEmpty()) {
                            Object parsedValue = convertValue(field.getType(), value);
                            field.set(instance, parsedValue);
                        }
                    }
                }
                records.add(instance);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
        }
        return records;
    }
    private static String getColumnName(Field field) {
        // Check for @Column annotation
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            return columnAnnotation.name(); // Return the name specified in @Column annotation
        }

        // Check for @JoinColumn annotation
        JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
        if (joinColumnAnnotation != null) {
            return joinColumnAnnotation.name(); // Return the name specified in @JoinColumn annotation
        }

        // Default to the field name if no annotation is found
        return field.getName();
    }
    private static Object convertValue(Class<?> type, String value) throws Exception {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == Date.class) {
            return dateFormat.parse(value); // Format YYYY-MM-DD
        } else if (type == Timestamp.class) {
            return Timestamp.valueOf(value); // Format YYYY-MM-DD HH:MM:SS
        } else {
            return value; // Cas String et autres types compatibles
        }
    }
}
