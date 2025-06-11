package com.EnderLite.DataController;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

enum Rank{
    USER,
    ADMIN
}

record ChatData(String chatId, Rank rand) {
    //Default
}


public class UserData {

    private StringProperty login;
    private StringProperty email;
    private ListProperty<String> friendsList = new SimpleListProperty<String>();
    private ListProperty<ChatData> chatsList = new SimpleListProperty<ChatData>();
    
    //login
    public StringProperty getLoginProperty(){
        return login;
    }

    public void setLogin(String newLogin){
        login.set(newLogin);
    }

    public String getLogin(){
        return login.get();
    }

    //email
    public StringProperty getEmailProperty(){
        return email;
    }

    public void setEmail(String newEmail){
        email.set(newEmail);
    }

    public String getEmail(){
        return email.get();
    }

    //friendsList
    public ListProperty<String> getFriendsListProperty(){
        return friendsList;
    }

    public void addFriend(String login){
        friendsList.add(login);
    }

    public ObservableList<String> getFriendsList(){
        return friendsList.get();
    }

    //chatList
    public ListProperty<ChatData> getChatListProperty(){
        return chatsList;
    }

    public void addChat(String chatId, Rank rank){
        chatsList.add(new ChatData(chatId, rank));
    }

    public ObservableList<ChatData> getChatList(){
        return chatsList.get();
    }

}