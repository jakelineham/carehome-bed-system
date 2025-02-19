package uk.ac.cardiff.AftercareConnect.Stakeholder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AreaMonthlyOverview {
    public int month;
    public int year;
    public float bedFillPercentage;
}
