package uk.ac.cardiff.AftercareConnect;

import java.util.List;

public interface UserRepository {

    List<UserAccount> getAllAccounts();

    List<UserAccount> getIncomingRegistrations();

    List<UserAccount> getIncomingAreaRegistrations(String area);

    void approveUser(String approvedEmail);

    void removeTempUser(String email);

    void addUser(String userType, int assignedCarehomeId, String name, String email, String password, String area);

    UserAccount findByEmail(String email);

    UserAccount findById(int id);

    String getAccountType(int userId);

    void updateUser(int id, String name, String email, String password, String area);

    int getAssignedCarehomeId(String email);

    boolean checkForEmailUserAccounts(String email);

    boolean checkForEmailAftercare(String email);

}
