package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;

public class V27__seed_first_admin extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        String hash = new BCryptPasswordEncoder().encode("Admin@123!");

        String sql = """
            INSERT INTO users (email, password_hash, role, enabled, created_at)
            VALUES (?, ?, 'ADMIN', true, CURRENT_TIMESTAMP)
            ON CONFLICT (email) DO NOTHING
            """;

        try (PreparedStatement stmt = context.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "admin@example.com");
            stmt.setString(2, hash);
            stmt.executeUpdate();
        }
    }
}
