package uk.ac.cardiff.AftercareConnect.Stakeholder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OverviewRepositoryJDBC implements OverviewRepository{
    private JdbcTemplate jdbcTemplate;
    private RowMapper<AreaMonthlyOverview> overviewMapper;

    OverviewRepositoryJDBC(JdbcTemplate jdbc) {
        jdbcTemplate = jdbc;
        setOverviewMapper();
    }

    private void setOverviewMapper() {
        overviewMapper = (rs, i) -> new AreaMonthlyOverview(
                rs.getInt("month"),
                rs.getInt("year"),
                rs.getFloat("bedFillPercentage")
        );
    }

    @Override
    public List<AreaMonthlyOverview> getOverview(String area, int year) {
        String sql = "select month, year, bedFillPercentage" +
                " from AreaMonthlyOverview where area = ? and year = ?";
        return jdbcTemplate.query(sql, overviewMapper, area, year);
    }
}
