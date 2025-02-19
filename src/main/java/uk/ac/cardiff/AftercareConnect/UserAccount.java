package uk.ac.cardiff.AftercareConnect;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAccount {
    private int id;
    private String email;
    private String name;
    private String password;
    private int assignedCarehomeId;
    private String area;
}
