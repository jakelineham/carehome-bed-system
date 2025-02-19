package uk.ac.cardiff.AftercareConnect.Stakeholder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MonthlyOverviewService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Runs at 11:00 PM on the last day of every month
    @Scheduled(cron = "0 0 23 L * ?")
    public void generateMonthlyOverview() {
        // Query to calculate total and available beds for each area across all wings
        String query = "SELECT area, SUM(totalNumOfBeds) AS totalBeds, " +
                "SUM(COALESCE(generalCareAvailable, 0) + " +
                "COALESCE(mentalHealthAvailable, 0) + " +
                "COALESCE(dementiaAvailable, 0) + " +
                "COALESCE(rehabAvailable, 0) + " +
                "COALESCE(palliativeAvailable, 0)) AS availableBeds " +
                "FROM aftercare GROUP BY area";

        jdbcTemplate.query(query, (rs) -> {
            String area = rs.getString("area");
            int totalBeds = rs.getInt("totalBeds");
            int availableBeds = rs.getInt("availableBeds");

            // If totalBeds is greater than 0, calculate the bed fill rate
            if (totalBeds > 0) {
                // Calculate bed fill percentage: (totalBeds - availableBeds) / totalBeds * 100
                float bedFillPercentage = ((float) (totalBeds - availableBeds) / totalBeds) * 100;

                // Insert calculated data into areaMonthlyOverview table
                String insertQuery = "INSERT INTO areaMonthlyOverview (month, year, area, bedFillPercentage) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(insertQuery, LocalDate.now().getMonthValue(), LocalDate.now().getYear(), area, bedFillPercentage);
            }
        });
    }
}
