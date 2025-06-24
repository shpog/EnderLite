package com.EnderLite.Model;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Class that is a representation of a group chat in the database.
 */

public class Chat {
    /**
     * Group chat ID in the database
     */
    public UUID ID;
    /**
     * Group chat name
     */
    public String Name;
    /**
     * ArrayList of UUIDs of members. Corresponding UUIDs are keys to Users in
     * database.
     */
    public ArrayList<UUID> Members;
    /**
     * ArrayList of UUIDs of administrators. Corresponding UUIDs are keys to Users
     * in database.
     */
    public ArrayList<UUID> Admins;
    /**
     * ArrayList of MessageEntry representing messages history.
     */
    public ArrayList<MessageEntry> History;

    /**
     * Constructor, setting Admins, Members and History as empty ArrayLists.
     */
    public Chat() {
        Admins = new ArrayList<UUID>();
        Members = new ArrayList<UUID>();
        History = new ArrayList<MessageEntry>();
    }
}
