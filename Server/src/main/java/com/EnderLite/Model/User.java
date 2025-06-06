package com.EnderLite.Model;

import java.util.ArrayList;
import java.util.UUID;

public class User {
    public UUID ID;
    public String Login;
    public String Email;
    public String PasswordHash;
    public ArrayList<User> FriendsList;
    public ArrayList<Chat> ChatsList;
}
