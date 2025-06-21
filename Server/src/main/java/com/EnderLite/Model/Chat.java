package com.EnderLite.Model;

import java.util.ArrayList;
import java.util.UUID;

public class Chat {
    public UUID ID;
    public String Name;
    public ArrayList<UUID> Members;
    public ArrayList<UUID> Admins;

    public ArrayList<MessageEntry> History;

    public Chat() {
        Admins = new ArrayList<UUID>();
        Members = new ArrayList<UUID>();
        History = new ArrayList<MessageEntry>();
    }
}
