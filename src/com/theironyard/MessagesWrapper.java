package com.theironyard;

import java.util.ArrayList;

/**
 * Created by zach on 10/17/16.
 */
public class MessagesWrapper {
    ArrayList<Message> messages;

    public MessagesWrapper() {
    }

    public MessagesWrapper(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
}
