package uk.ac.cardiff.AftercareConnect;

import java.util.List;

public interface AftercareRepository {
    List<Aftercare> getAftercare();

    void updateAftercare(
            int id,
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
            Integer palliativeAvailable);

    void addCarehome(
            String homeName,
            String address,
            String email,
            String phoneNo,
            String area,
            boolean has12HourCare,
            boolean speaksWelsh,
            boolean assistedLiving,
            int totalNumOfBeds,
            Integer generalCareTotal,
            Integer generalCareAvailable,
            Integer mentalHealthTotal,
            Integer mentalHealthAvailable,
            Integer dementiaTotal,
            Integer dementiaAvailable,
            Integer rehabTotal,
            Integer rehabAvailable,
            Integer palliativeTotal,
            Integer palliativeAvailable);

    void updateAvailableBeds(
            int id,
            Integer generalCareAvailable,
            Integer mentalHealthAvailable,
            Integer dementiaAvailable,
            Integer rehabAvailable,
            Integer palliativeAvailable);

    List<Aftercare> getIncomingRegistrations();

    void approveCarehome(String approvedEmail);

    void removeTempCarehome(String email);

    boolean isApproved(int id);

    List<Aftercare> filterAftercare(String name, String area, boolean has12HourCare,
                    boolean speaksWelsh, boolean assistedLiving, boolean hasGeneralWard, boolean hasMentalWard,
                    boolean hasDementiaWard, boolean hasRehabilitationWard, boolean hasPalliativeWard);

    List<Aftercare> getApprovedCarehomes();
}
