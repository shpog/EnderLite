package com.EnderLite.Model;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Class that is a representation of a User in the database.
 */
public class User {
    /**
     * User ID in the database
     */
    public UUID ID;
    /**
     * User login
     */
    public String Login;
    /**
     * User email
     */
    public String Email;
    /**
     * User password
     */
    public String PasswordHash;
    /**
     * ArrayList of UUIDs of User's friends. Corresponding UUIDs are keys to Users
     * in database.
     */
    public ArrayList<UUID> FriendsList;

    /**
     * ArrayList of UUIDs of User's group chats. Corresponding UUIDs are keys to
     * Chats in database.
     */
    public ArrayList<UUID> ChatsList;

    public User() {
        FriendsList = new ArrayList<UUID>();
        ChatsList = new ArrayList<UUID>();
    }
}
