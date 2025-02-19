package uk.ac.cardiff.AftercareConnect;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViewMessage {
    private int id;          // Unique ID for the message
    private String name;     // Name of the person who sent the message
    private String email;    // Email address of the sender
    private String userType; // Type of user (e.g., Admin, User, etc.)
    private String message;  // The message content
}
