package uk.ac.cardiff.AftercareConnect;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ArrayList;

@Repository
public class AftercareRepositoryJDBC implements AftercareRepository{

    private JdbcTemplate jdbcTemplate;
    private RowMapper<Aftercare> homeMapper;

    public AftercareRepositoryJDBC(JdbcTemplate aJdbc) {
        this.jdbcTemplate = aJdbc;
        setHomeMapper();
    }

    private void setHomeMapper() {
        homeMapper = (rs, i) -> new Aftercare(
                rs.getInt("id"),
                rs.getString("homeName"),
                rs.getString("address"),
                rs.getString("email"),
                rs.getString("phoneNo"),
                rs.getString("area"),
                rs.getBoolean("has12HourCare"),
                rs.getBoolean("speaksWelsh"),
                rs.getBoolean("assistedLiving"),
                rs.getInt("totalNumOfBeds"),
                rs.getInt("generalCareTotal"),
                rs.getInt("generalCareAvailable"),
                rs.getInt("mentalHealthTotal"),
                rs.getInt("mentalHealthAvailable"),
                rs.getInt("dementiaTotal"),
                rs.getInt("dementiaAvailable"),
                rs.getInt("rehabTotal"),
                rs.getInt("rehabAvailable"),
                rs.getInt("palliativeTotal"),
                rs.getInt("palliativeAvailable")
        );
    }

    @Override
    public List<Aftercare> getAftercare() {
        String sql = "select * from aftercare";
        return jdbcTemplate.query(sql, homeMapper);
    }

    @Override
    public void updateAftercare(int id,
                                String homeName,
                                String email,
                                String phoneNo,
                                boolean has12HourCare,
                                boolean speaksWelsh,
                                boolean assistedLiving,
                                int newBedTotal,
                                Integer generalCareTotal,
                                Integer generalCareAvailable,
                                Integer mentalHealthTotal,
                                Integer mentalHealthAvailable,
                                Integer dementiaTotal,
                                Integer dementiaAvailable,
                                Integer rehabTotal,
                                Integer rehabAvailable,
                                Integer palliativeTotal,
                                Integer palliativeAvailable) {
        String sql = "UPDATE aftercare SET " +
                "homeName = ?, " +
                "email = ?, " +
                "phoneNo = ?, " +
                "has12HourCare = ?, " +
                "speaksWelsh = ?, " +
                "assistedLiving = ?, " +
                "totalNumOfBeds = ?, " +
                "generalCareTotal = ?, " +
                "generalCareAvailable = ?, " +
                "mentalHealthTotal = ?, " +
                "mentalHealthAvailable = ?, " +
                "dementiaTotal = ?, " +
                "dementiaAvailable = ?, " +
                "rehabTotal = ?, " +
                "rehabAvailable = ?, " +
                "palliativeTotal = ?, " +
                "palliativeAvailable = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(sql, homeName, email, phoneNo, has12HourCare,
                speaksWelsh, assistedLiving, newBedTotal, generalCareTotal, generalCareAvailable,
                mentalHealthTotal, mentalHealthAvailable, dementiaTotal, dementiaAvailable,
                rehabTotal, rehabAvailable, palliativeTotal, palliativeAvailable, id);
    }

    @Override
    public void addCarehome(
            String homeName,
        String address,
        String email,
        String phoneNo,
        String area,
        boolean has12HourCare,
        boolean speaksWelsh,
        boolean assistedLiving,
        int totalNumOfBeds,
        //Integer instead of int as int cannot be null
        Integer generalCareTotal,
        Integer generalCareAvailable,
        Integer mentalHealthTotal,
        Integer mentalHealthAvailable,
        Integer dementiaTotal,
        Integer dementiaAvailable,
        Integer rehabTotal,
        Integer rehabAvailable,
        Integer palliativeTotal,
        Integer palliativeAvailable) {
        String sql = "INSERT INTO aftercare (homeName, address, email, phoneNo, area, has12HourCare, speaksWelsh, assistedLiving, " +
                "totalNumOfBeds, generalCareTotal, generalCareAvailable, mentalHealthTotal, mentalHealthAvailable, " +
                "dementiaTotal, dementiaAvailable, rehabTotal, rehabAvailable, palliativeTotal, palliativeAvailable, accepted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        jdbcTemplate.update(sql, homeName, address, email, phoneNo, area, has12HourCare, speaksWelsh, assistedLiving,
                totalNumOfBeds, generalCareTotal, generalCareAvailable, mentalHealthTotal, mentalHealthAvailable,
                dementiaTotal, dementiaAvailable, rehabTotal, rehabAvailable, palliativeTotal, palliativeAvailable);
    }

    @Override
    public void approveCarehome(String approvedEmail) {
        String sql = "update aftercare set accepted = 1 where email = ?";
        jdbcTemplate.update(sql, approvedEmail);
    }

    @Override
    public void removeTempCarehome(String email) {
        String sql = "delete from aftercare where email = ? and accepted = 0";
        jdbcTemplate.update(sql, email);
    }

    @Override
    public void updateAvailableBeds(int id,
                                    Integer generalCareAvailable,
                                    Integer mentalHealthAvailable,
                                    Integer dementiaAvailable,
                                    Integer rehabAvailable,
                                    Integer palliativeAvailable) {
        String sql = "update aftercare set " +
                "generalCareAvailable = ?, " +
                "mentalHealthAvailable = ?, " +
                "dementiaAvailable = ?, " +
                "rehabAvailable = ?, " +
                "palliativeAvailable = ? " +
                "where id = ?";
        jdbcTemplate.update(sql,
                generalCareAvailable,
                mentalHealthAvailable,
                dementiaAvailable,
                rehabAvailable,
                palliativeAvailable,
                id);
    }

    @Override
    public List<Aftercare> getIncomingRegistrations() {
        String sql = "select id, homeName, address, email, phoneNo, area, has12HourCare, " +
                "speaksWelsh, +" +
                "assistedLiving, " +
                "totalNumOfBeds, " +
                "generalCareTotal, " +
                "generalCareAvailable, " +
                "mentalHealthTotal, " +
                "mentalHealthAvailable, " +
                "dementiaTotal, " +
                "dementiaAvailable, " +
                "rehabTotal, " +
                "rehabAvailable, " +
                "palliativeTotal, " +
                "palliativeAvailable " +
                "from aftercare where accepted = 0";
        return jdbcTemplate.query(sql, homeMapper);
    }

    @Override
    public boolean isApproved(int id) {
        String sql = "select accepted from aftercare where id = ?";
        Boolean isAccepted = jdbcTemplate.queryForObject(sql, new Object[]{id}, Boolean.class);
        return isAccepted != null && isAccepted;
    }

    @Override
    public List<Aftercare> filterAftercare(String name, String area, boolean has12HourCare,
                      boolean speaksWelsh, boolean assistedLiving, boolean hasGeneralWard, boolean hasMentalWard,
                      boolean hasDementiaWard, boolean hasRehabilitationWard, boolean hasPalliativeWard) {

        StringBuilder sql = new StringBuilder("SELECT * FROM aftercare WHERE accepted = true");

        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND homeName LIKE ?");
            params.add("%" + name + "%");
        }


        if (area != null && !area.isEmpty()) {
            sql.append(" AND area = ?");
            params.add(area);
        }

        if(has12HourCare) {
            sql.append(" and has12HourCare = ?");
            params.add(true);
        }

        if (speaksWelsh) {
            sql.append(" AND speaksWelsh = ?");
            params.add(true);
        }

        if (assistedLiving) {
            sql.append(" AND assistedLiving = ?");
            params.add(true);
        }

        if (hasGeneralWard) {
            sql.append(" AND generalCareTotal IS NOT NULL");
        }

        if (hasMentalWard) {
            sql.append(" AND mentalHealthTotal IS NOT NULL");
        }

        if (hasDementiaWard) {
            sql.append(" AND dementiaTotal IS NOT NULL");
        }

        if (hasRehabilitationWard) {
            sql.append(" AND rehabTotal IS NOT NULL");
        }

        if (hasPalliativeWard) {
            sql.append(" AND palliativeTotal IS NOT NULL");
        }

        return jdbcTemplate.query(sql.toString(), homeMapper, params.toArray());
    }

    @Override
    public List<Aftercare> getApprovedCarehomes() {
        String sql = "SELECT * FROM aftercare WHERE accepted = 1";
        return jdbcTemplate.query(sql, homeMapper);
    }
}


