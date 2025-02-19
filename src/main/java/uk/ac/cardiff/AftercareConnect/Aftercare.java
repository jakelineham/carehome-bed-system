package uk.ac.cardiff.AftercareConnect;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Aftercare {
    private int id;
    private String homeName;
    private String address;
    private String email;
    private String phoneNo;
    private String area;
    private boolean has12HourCare;
    private boolean speaksWelsh;
    private boolean assistedLiving;
    private int totalNumOfBeds;
    private int generalCareTotal;
    private int generalCareAvailable;
    private int mentalHealthTotal;
    private int mentalHealthAvailable;
    private int dementiaTotal;
    private int dementiaAvailable;
    private int rehabTotal;
    private int rehabAvailable;
    private int palliativeTotal;
    private int palliativeAvailable;

    public String yesOrNo(Boolean facility){
        String answer = "No";
        if (facility == true){
            answer = "Yes";
        }
        return answer;
    }

    public int getTotalAvailableBeds(){
        return generalCareAvailable + mentalHealthAvailable + dementiaAvailable + rehabAvailable + palliativeAvailable;
    }
}
