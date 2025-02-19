package uk.ac.cardiff.AftercareConnect.Stakeholder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.List;

@Controller
public class StakeholderController {

    @Autowired
    private OverviewRepository overviewRepository;

    @Autowired
    private MonthlyOverviewService monthlyOverviewService;

    @Autowired
    private ReportService reportService;

    @PostMapping("/getChartData")
    @ResponseBody
    public List<AreaMonthlyOverview> getChartData(@RequestParam("area") String area, @RequestParam("year") String year) {
        int yearInt = Integer.parseInt(year);
        return overviewRepository.getOverview(area, yearInt);
    }

    // This method triggers the MonthlyOverviewService method. Use cURL, with csrf token, JSESSIONID, and username password of an admin, to test functionality.
    @PostMapping("/testScheduler")
    @ResponseBody
    public String testScheduler() {
        monthlyOverviewService.generateMonthlyOverview();
        return "Scheduler executed successfully!";
    }

    @PostMapping("/generateReport")
    @ResponseBody
    public ResponseEntity<byte[]> generateReport(
            @RequestParam("area") String area,
            @RequestParam("year") int year) {

        // Fetch the data for the report
        List<AreaMonthlyOverview> data = overviewRepository.getOverview(area, year);

        // Generate the report content as a String
        String reportContent = reportService.generateReport(data, area, year);

        // Convert the content to bytes
        byte[] reportBytes = reportContent.getBytes();

        // Declare and construct dynamic filename
        String filename = "BedFillRateReport_" + area.replaceAll("\\s+", "_") + "_" + year + ".txt";

        // Return the file as a ResponseEntity with dynamic filename
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(reportBytes);
    }

}
