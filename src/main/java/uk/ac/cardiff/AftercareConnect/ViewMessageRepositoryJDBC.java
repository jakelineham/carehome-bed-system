package uk.ac.cardiff.AftercareConnect;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ViewMessageRepositoryJDBC implements ViewMessageRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ViewMessage> messageMapper = (rs, rowNum) -> new ViewMessage(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("user_type"),
            rs.getString("message")
    );

    public ViewMessageRepositoryJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ViewMessage> getAllMessages() {
        String sql = "SELECT id, name, email, user_type, message FROM contact_messages";
        return jdbcTemplate.query(sql, messageMapper);
    }

    @Override
    public ViewMessage getMessageById(int id) {
        String sql = "SELECT id, name, email, user_type, message FROM contact_messages WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, messageMapper, id);
    }

    @Override
    public void deleteMessage(int id) {
        String sql = "DELETE FROM contact_messages WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void saveMessage(ViewMessage message) {
        String sql = "INSERT INTO contact_messages (name, email, user_type, message) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, message.getName(), message.getEmail(), message.getUserType(), message.getMessage());
    }
}