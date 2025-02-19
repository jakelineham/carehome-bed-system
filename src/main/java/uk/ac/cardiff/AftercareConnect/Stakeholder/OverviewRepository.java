package uk.ac.cardiff.AftercareConnect.Stakeholder;

import java.util.List;

public interface OverviewRepository {
    List<AreaMonthlyOverview> getOverview(String area, int year);
}
