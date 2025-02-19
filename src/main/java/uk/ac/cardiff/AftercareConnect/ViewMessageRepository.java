package uk.ac.cardiff.AftercareConnect;

import java.util.List;

public interface ViewMessageRepository {
    List<ViewMessage> getAllMessages();    // Fetch all messages
    ViewMessage getMessageById(int id);    // Fetch a specific message by its ID
    void deleteMessage(int id);               // Delete a message by its ID
    void saveMessage(ViewMessage message);
}
