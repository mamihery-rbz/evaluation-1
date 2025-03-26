package site.easy.to.build.crm.service.evaluation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfigurationBaseService {
    private final JdbcTemplate jdbcTemplate;
    public ConfigurationBaseService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void truncateAllTables() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        List<String> tables = jdbcTemplate.queryForList("SHOW TABLES", String.class);

        List<String> tablesAExclure = List.of("roles","oauth_users","user_profile","user_roles","users","employee","taux_alert");

        for (String table : tables) {
            if (!tablesAExclure.contains(table)) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            }
        }

        // Réactiver les contraintes de clés étrangères
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

}
