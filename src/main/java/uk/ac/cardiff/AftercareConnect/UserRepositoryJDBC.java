package uk.ac.cardiff.AftercareConnect;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

@Repository
public class UserRepositoryJDBC implements UserRepository{

    private JdbcTemplate jdbcTemplate;
    private RowMapper<UserAccount> accountMapper;
    private BCryptPasswordEncoder passwordEncoder;

    public UserRepositoryJDBC(JdbcTemplate jdbc) {
        jdbcTemplate = jdbc;
        setAccountMapper();
        passwordEncoder = new BCryptPasswordEncoder();
    }

    private void setAccountMapper() {
        accountMapper = (rs, i) -> new UserAccount(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getString("password"),
                rs.getInt("assignedCarehomeId"),
                rs.getString("area")
        );
    }

    @Override
    public int getAssignedCarehomeId(String email) {
        String sql = "SELECT assignedCarehomeId FROM userAccounts WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{email}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            // Log the exception for debugging
            System.err.println("No assigned carehome found for email: " + email);
            // Return a default value or handle accordingly
            return -1; // Use -1 or any other default value to indicate no assignment
        }
    }

    @Override
    public List<UserAccount> getAllAccounts() {
        String sql = "select id, email, name, password, assignedCarehomeId, area from useraccounts";
        return jdbcTemplate.query(sql, accountMapper);
    }

    @Override
    public String getAccountType(int userId) {
        String sql = "SELECT r.roleName " +
                "FROM roles r " +
                "JOIN userAccounts_roles ur ON r.roleID = ur.roleID " +
                "JOIN userAccounts u ON ur.email = u.email " +
                "WHERE u.id = ?";

        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }

    @Override
    public List<UserAccount> getIncomingRegistrations() {
        String sql = "select id, email, name, password, assignedCarehomeId, area from useraccounts where accepted = 0";
        return jdbcTemplate.query(sql, accountMapper);
    }

    public List<UserAccount> getIncomingAreaRegistrations(String username) {
        String sql = "select area from userAccounts where email = ?";
        String area = jdbcTemplate.queryForObject(sql, String.class, username);
        sql = "select id, email, name, password, assignedCarehomeId, area from useraccounts where accepted = 0"
                + " and area = ?";
        return jdbcTemplate.query(sql, accountMapper, area);
    }

    @Override
    public void approveUser(String approvedEmail) {
        String sql = "update useraccounts set accepted = 1 where email = ?";
        jdbcTemplate.update(sql, approvedEmail);
    }

    @Override
    public void removeTempUser(String email) {
        String sql = "delete from useraccounts where email = ? and accepted = 0";
        jdbcTemplate.update(sql, email);
    }

    @Override
    public void addUser(String userType, int assignedCarehomeId, String name, String email, String password, String area) {
        // Add to userAccounts
        String sql = "insert into userAccounts(email, name, password, assignedCarehomeId, area, accepted) values (?, ?, ?, ?, ?, 0)";
        jdbcTemplate.update(sql,
                email,
                name,
                passwordEncoder.encode(password), // Encrypt password
                assignedCarehomeId,
                area
        );

        int userRoleId = 0;

        switch (userType) {
            case "carehomeManager":
                userRoleId = 1;
                break;
            case "socialService":
                userRoleId = 2;
                break;
            case "societyManager":
                userRoleId = 3;
                break;
            case "stakeholder":
                userRoleId = 4;
                break;
        }

        // Add to userAccounts_roles
        String roleSql = "insert into userAccounts_roles(email, roleID) values (?, ?)";
        jdbcTemplate.update(roleSql, email, userRoleId);
    }

    @Override
    public UserAccount findByEmail(String email) {
        String sql = "SELECT * FROM userAccounts WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{email}, accountMapper);
    }

    @Override
    public UserAccount findById(int id) {
        String sql = "SELECT * FROM userAccounts WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, accountMapper);
    }

    @Override
    public boolean checkForEmailUserAccounts(String email) {
        String sql = "select email from useraccounts where email = ?";
        List<String> emails = jdbcTemplate.queryForList(sql, String.class, email);
        return !emails.isEmpty();
    }

    @Override
    public boolean checkForEmailAftercare(String email) {
        String sql = "select email from aftercare where email = ?";
        List<String> emails = jdbcTemplate.queryForList(sql, String.class, email);
        return !emails.isEmpty();
    }

    @Override
    public void updateUser(int id, String name, String email, String password, String area) {
        String userSql;
        if (password == null || password.trim().isEmpty()) {
            // Update everything except the password
            userSql = "UPDATE userAccounts SET name = ?, email = ?, area = ? WHERE id = ?";
            jdbcTemplate.update(userSql, name, email, area, id);
        } else {
            // Update all fields including the password
            userSql = "UPDATE userAccounts SET name = ?, email = ?, password = ?, area = ? WHERE id = ?";
            jdbcTemplate.update(userSql, name, email, passwordEncoder.encode(password), area, id);
        }

        // Update the userAccounts_roles table using the new email as the identifier
        String roleSql = "UPDATE userAccounts_roles SET email = ? WHERE email = (SELECT email FROM userAccounts WHERE id = ?)";
        jdbcTemplate.update(roleSql, email, id);
    }
}
