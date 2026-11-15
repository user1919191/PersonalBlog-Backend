package main.java.com.blog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaPatchInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaPatchInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaPatchInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureArticleSeriesSchema();
        ensureFavoriteFolderColumns();
    }

    private void ensureArticleSeriesSchema() {
        ensureTableExists(
                "article_series",
                "CREATE TABLE article_series (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "description VARCHAR(500) DEFAULT NULL, " +
                        "cover_image VARCHAR(255) DEFAULT NULL, " +
                        "sort_order INT NOT NULL DEFAULT 0, " +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "INDEX idx_sort (sort_order)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章专栏表'"
        );
        ensureColumn(
                "article_series",
                "cover_image",
                "ALTER TABLE article_series ADD COLUMN cover_image VARCHAR(255) NULL AFTER description"
        );
        ensureColumn(
                "article_series",
                "sort_order",
                "ALTER TABLE article_series ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER cover_image"
        );
        ensureColumn(
                "article_series",
                "create_time",
                "ALTER TABLE article_series ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER sort_order"
        );
    }

    private void ensureFavoriteFolderColumns() {
        ensureColumn(
                "favorite_folder",
                "update_time",
                "ALTER TABLE favorite_folder ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER create_time"
        );
        jdbcTemplate.update("UPDATE favorite_folder SET update_time = create_time WHERE update_time < create_time");
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        if (!tableExists(tableName)) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute(ddl);
        log.info("[SchemaPatchInitializer] added column {}.{}", tableName, columnName);
    }

    private void ensureTableExists(String tableName, String ddl) {
        if (tableExists(tableName)) {
            return;
        }
        jdbcTemplate.execute(ddl);
        log.info("[SchemaPatchInitializer] created table {}", tableName);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }
}
