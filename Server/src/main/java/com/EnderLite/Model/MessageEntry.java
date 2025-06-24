package com.EnderLite.Model;

import java.util.Date;

/**
 * Class that is a representation of a message entry in the database.
 */
public class MessageEntry {
    /**
     * Message Sender
     */
    public User Sender;
    /**
     * Sent date
     */
    public Date Date;
    /**
     * Message content sent by Sender
     */
    public String Content;
}
