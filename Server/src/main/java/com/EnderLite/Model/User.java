package com.EnderLite.Model;

import java.util.ArrayList;
import java.util.UUID;

public class User {
    public UUID ID;
    public String Login;
    public String Email;
    public String PasswordHash;
    public ArrayList<UUID> FriendsList;
    public ArrayList<UUID> ChatsList;

    public User() {
        FriendsList = new ArrayList<UUID>();
        ChatsList = new ArrayList<UUID>();
    }
}
