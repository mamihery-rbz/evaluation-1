package site.easy.to.build.crm.service.csv;

import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.util.CsvUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {
    @Transactional
    public <T> void insertDataFromCsv(MultipartFile file, Class<T> clazz,Connection conn) {
        String tableName = getTableName(clazz);
        String tempTableName = tableName + "_temp";

        List<T> records = CsvUtil.parseCsv(file, clazz,';');

        try (Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);
            String createTableSQL = getCreateTableScript(stmt, tableName);

            stmt.execute("DROP TABLE IF EXISTS " + tempTableName);
            stmt.execute(createTableSQL.replace(tableName, tempTableName));

            insertIntoTable(conn, tempTableName, records);

            insertIntoTable(conn, tableName, records);

            stmt.execute("DROP TEMPORARY TABLE IF EXISTS " + tempTableName);

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TEMPORARY TABLE IF EXISTS " + tempTableName);
            } catch (SQLException ignored) {}

            throw new RuntimeException("Erreur lors de l'insertion : " + e.getMessage());
        }
    }

    private <T> String getTableName(Class<T> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            return tableAnnotation.name();
        }
        throw new RuntimeException("L'entité " + clazz.getSimpleName() + " n'a pas d'annotation @Table");
    }

    private String getCreateTableScript(Statement stmt, String tableName) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName)) {
            if (rs.next()) {
                return rs.getString(2);
            }
        }
        throw new RuntimeException("Impossible de récupérer le script de création de la table : " + tableName);
    }

    private <T> void insertIntoTable(Connection conn, String tableName, List<T> records) throws SQLException {
        if (records.isEmpty()) return;

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        Field[] fields = records.get(0).getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            sql.append(fields[i].getName());
            if (i < fields.length - 1) sql.append(", ");
        }
        sql.append(") VALUES ");

        List<String> valuesList = new ArrayList<>();
        for (T record : records) {
            StringBuilder values = new StringBuilder("(");
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                try {
                    Object value = fields[i].get(record);
                    values.append(valueToSQL(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Erreur lors de l'accès au champ : " + fields[i].getName());
                }
                if (i < fields.length - 1) values.append(", ");
            }
            values.append(")");
            valuesList.add(values.toString());
        }

        sql.append(String.join(", ", valuesList));

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.executeUpdate();
        }
    }
    private String valueToSQL(Object value) {
        if (value instanceof String || value instanceof Date || value instanceof Timestamp) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }
}
