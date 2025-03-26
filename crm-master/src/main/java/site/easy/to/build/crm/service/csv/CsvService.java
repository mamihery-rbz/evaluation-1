package site.easy.to.build.crm.service.csv;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.util.CsvUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CsvService {
    private JdbcTemplate jdbcTemplate;
    private ApplicationContext applicationContext;
    private EntityManager entityManager;

    public CsvService(JdbcTemplate jdbcTemplate,ApplicationContext applicationContext,EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationContext = applicationContext;
        this.entityManager = entityManager;
    }
    public static String getTableName(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return entityClass.getSimpleName().toLowerCase(Locale.ROOT);
    }
    public static String getColumnName(Field field){
        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation=field.getAnnotation(Column.class);
            return columnAnnotation.name();
        }
        else if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn columnAnnotation=field.getAnnotation(JoinColumn.class);
            return columnAnnotation.name();
        }
        else return field.getName();
    }
    public List<String> getEntityColumns(Class<?> entityClass) {
        List<String> columns = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                columns.add(columnAnnotation.name());
            } else if (field.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
                columns.add(joinColumnAnnotation.name());
            }
        }

        return columns;
    }
    public static String getTempTableName(String tableName){
        return tableName+"_temp";
    }
    public String getCreateTableScript(String tableName) {
        String sql = String.format("SHOW CREATE TABLE %s", tableName);
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getString(2));
    }
    public void createTempTable(String originalTableName) {
        String tempTableName=getTempTableName(originalTableName);
        String sql=getCreateTableScript(originalTableName);
        String tempTableScript = sql
                .replace("CREATE TABLE", "CREATE TABLE")
                .replace(originalTableName, tempTableName);
        jdbcTemplate.execute(tempTableScript);
    }
    public void deleteTempTable(String tempTableName) {
        String sql = "DROP TABLE IF EXISTS " + tempTableName;
        jdbcTemplate.execute(sql);
    }
    private Object getIdFromEntity(Object entity) throws Exception {
        if (entity == null) return null;

        List<Method> getters = new ArrayList<>();

        for (Method method : entity.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                method.setAccessible(true);
                getters.add(method);
            }
        }

        for (Method method : getters) {
            if (method.getName().endsWith("Id")) {
                return method.invoke(entity);
            }
        }

        return null;
    }

    public void insertDataTempTable(String tempTableName, List<?> listes) throws Exception {
        if (listes.isEmpty()) return;

        try {
            for (Object obj : listes) {
                List<String> nonNullColumns = new ArrayList<>();
                List<Object> nonNullValues = new ArrayList<>();

                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = null;
                    try {
                        value = field.get(obj);
                        if (value != null) {
                            nonNullColumns.add(getColumnName(field));
                            nonNullValues.add(value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field: " + field.getName(), e);
                    }
                }
                String columnNames = String.join(", ", nonNullColumns);
                String placeholders = nonNullColumns.stream().map(col -> "?").collect(Collectors.joining(", "));
                String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tempTableName, columnNames, placeholders);
                System.out.println("SQL Query: " + sql);
                jdbcTemplate.update(sql, ps -> {
                    int index = 1;
                    for (Object value : nonNullValues) {
                        Class<?> valueClass = value.getClass();
                        if (value != null && !valueClass.isPrimitive() && !valueClass.equals(String.class) &&
                                !Date.class.isAssignableFrom(valueClass) &&
                                !Timestamp.class.isAssignableFrom(valueClass) &&
                                !LocalDate.class.isAssignableFrom(valueClass) &&
                                !LocalDateTime.class.isAssignableFrom(valueClass)) {
                            try {
                                value = getIdFromEntity(value);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        ps.setObject(index++, value);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during batch insert", e);
        }
    }
    public void insertIntoRealTable(String tableName,String tempTableName) throws Exception{
        try{
            String insertIntoRealTable = "INSERT INTO " + tableName + " SELECT * FROM " + tempTableName;
            jdbcTemplate.update(insertIntoRealTable);
        }
        catch (Exception e){
            throw e;
        }
    }

    @Transactional
    public void insertData(MultipartFile file, Class<?> clazz, char separator) throws Exception {
        String tableName=getTableName(clazz);
        String tempTableName=getTempTableName(tableName);
        try {
            CSVParser csvParser = CsvUtil.readCsv(file, separator);
            List<?>listes=CsvUtil.parseCSV(csvParser,clazz,applicationContext,entityManager);
            for (int i = 0; i < listes.size(); i++) {
                System.out.println(listes.get(i));
            }
            deleteTempTable(tempTableName);
            createTempTable(tableName);
            insertDataTempTable(tempTableName,listes);
            insertIntoRealTable(tableName,tempTableName);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'insertion des données depuis le fichier CSV", e);
        }
        finally {
            deleteTempTable(tempTableName);
        }
    }

}
