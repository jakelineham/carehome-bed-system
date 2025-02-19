package uk.ac.cardiff.AftercareConnect.Stakeholder;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ReportService {

    public String generateReport(List<AreaMonthlyOverview> data, String area, int year) {
        StringBuilder report = new StringBuilder();

        // Add header information
        report.append("Bed Fill Rate Report\n");
        report.append("Area: ").append(area).append("\n");
        report.append("Year: ").append(year).append("\n\n");

        // Add column headers
        report.append(String.format("%-10s %-20s\n", "Month", "Bed Fill Percentage"));
        report.append("=".repeat(30)).append("\n");

        // Add data
        data.forEach(entry -> {
            String monthName = java.time.Month.of(entry.getMonth()).name();
            report.append(String.format("%-10s %-20.2f%%\n", monthName, entry.getBedFillPercentage()));
        });

        return report.toString();
    }
}
