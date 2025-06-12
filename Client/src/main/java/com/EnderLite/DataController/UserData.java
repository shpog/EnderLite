package com.EnderLite.DataController;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserData {

    UserData(){
        this.login.set("");
        this.email.set("");
    }

    UserData(String login, String email){
        this.login.set(login);
        this.email.set(email);
    }

    private StringProperty login = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private StringProperty passw = new SimpleStringProperty();
    private ListProperty<String> friendsList;
    private ListProperty<ChatData> chatsList;

    {
        ObservableList<String> observListFriends = FXCollections.observableArrayList();
        friendsList = new SimpleListProperty<String>(observListFriends);
        ObservableList<ChatData> observListChats = FXCollections.observableArrayList();
        chatsList = new SimpleListProperty<ChatData>(observListChats);
    }

    //passw
    public StringProperty getPasswProperty(){
        return passw;
    }

    public void setPassw(String newPassw){
        passw.set(newPassw);
    }

    public String getPassw(){
        return passw.get();
    }
    
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

    public boolean removeFriend(String login){
        return friendsList.remove(login);
    }

    public boolean changeFriendLogin(String oldLogin, String newLogin){
        int oldIndex = friendsList.indexOf(oldLogin);
        if ( -1 == oldIndex){
            return false;
        }

        friendsList.set(oldIndex, newLogin);
        return true;
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

    public boolean removeChat(String chatId){
        return chatsList.remove(new ChatData(chatId, null));
    }

    public Rank getChatRank(String chatId){
        int index = chatsList.indexOf(new ChatData(chatId, null));
        return chatsList.get(index).getRank();
    }

    public boolean changeChatRank(String oldChatId, Rank rank){
        int oldIndex = chatsList.indexOf(new ChatData(oldChatId, null));
        if ( -1 == oldIndex ){
            return false;
        }

        ChatData old = chatsList.get(oldIndex);
        old.setRank( rank);
        return true;
    }

    public boolean changeChatName(String oldChatId, String newChatId){
        int oldIndex = chatsList.indexOf(new ChatData(oldChatId, null));
        if ( -1 == oldIndex ){
            return false;
        }

        ChatData old = chatsList.get(oldIndex);
        old.setId( newChatId);
        return true;
    }

    public ObservableList<ChatData> getChatList(){
        return chatsList.get();
    }

}