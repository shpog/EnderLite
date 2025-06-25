package com.EnderLite.DataController;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Used to store info about client's user
 * @author Micro9261
 */
public class UserData {
    private StringProperty login = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private ListProperty<String> friendsList;
    private ListProperty<ChatData> chatsList;

    /**
     * Default constructor.
     * Sets everything to empty String
     */
    UserData(){
        this.login.set("");
        this.email.set("");
    }

    /**
     * Constructor for creating user data
     * @param login client's user name
     * @param email client's user email
     */
    UserData(String login, String email){
        this.login.set(login);
        this.email.set(email);
    }

    {
        ObservableList<String> observListFriends = FXCollections.observableArrayList();
        friendsList = new SimpleListProperty<String>(observListFriends);
        ObservableList<ChatData> observListChats = FXCollections.observableArrayList();
        chatsList = new SimpleListProperty<ChatData>(observListChats);
    }

    
    /**
     * Used to get client's user propertyLogin
     * @return loginProperty (JavaFX)
     */
    public StringProperty getLoginProperty(){
        return login;
    }

    /**
     * Used to set client's user login
     * @param newLogin client's user login
     */
    public void setLogin(String newLogin){
        login.set(newLogin);
    }

    /**
     * Used to get client's user login as String
     * @return client's user login
     */
    public String getLogin(){
        return login.get();
    }

    /**
     * Used to get client's emailProperty
     * @return emailProperty (JavaFX)
     */
    public StringProperty getEmailProperty(){
        return email;
    }

    /**
     * Used to set client's user email
     * @param newEmail new email
     */
    public void setEmail(String newEmail){
        email.set(newEmail);
    }

    /**
     * Used to get client's user email as String
     * @return client's user email
     */
    public String getEmail(){
        return email.get();
    }

    /**
     * Used to get client's user friendListProperty
     * @return friendListProperty (JavaFX)
     */
    public ListProperty<String> getFriendsListProperty(){
        return friendsList;
    }

    /**
     * Used to add friend to client's user friendList
     * @param login friend login
     */
    public void addFriend(String login){
        friendsList.add(login);
    }

    /**
     * Used to remove friend from client's user friendList
     * @param login friend to be deleted
     * @return true if success, false otherwise
     */
    public boolean removeFriend(String login){
        return friendsList.remove(login);
    }

    /**
     * Used to change friend name
     * @param oldLogin old friend name
     * @param newLogin new friend name
     * @return true if success, false otherwise
     */
    public boolean changeFriendLogin(String oldLogin, String newLogin){
        int oldIndex = friendsList.indexOf(oldLogin);
        if ( -1 == oldIndex){
            return false;
        }

        friendsList.set(oldIndex, newLogin);
        return true;
    }

    /**
     * Used to get friendList
     * @return friend list
     */
    public ObservableList<String> getFriendsList(){
        return friendsList.get();
    }


    /**
     * Used to get client's user chatListProperty
     * @return chatListProperty (JavaFX)
     */
    public ListProperty<ChatData> getChatListProperty(){
        return chatsList;
    }

    /**
     * Used to add chat to chaatList
     * @param chatId chat name
     * @param rank client's user rank in chat
     */
    public void addChat(String chatId, Rank rank){
        chatsList.add(new ChatData(chatId, rank));
    }

    /**
     * Used to remove chat from chatList
     * @param chatId chat name to remove
     * @return true if success, false otherwise
     */
    public boolean removeChat(String chatId){
        return chatsList.remove(new ChatData(chatId, null));
    }

    /**
     * Used to get Rank for given chat name
     * @param chatId chat name
     * @return Rank of given chat;
     */
    public Rank getChatRank(String chatId){
        int index = chatsList.indexOf(new ChatData(chatId, null));
        return chatsList.get(index).getRank();
    }

    /**
     * Used to change client's user rank on chat
     * @param chatId chat name
     * @param rank client's user new rank on given chat
     * @return true if success, false otherwise
     */
    public boolean changeChatRank(String chatId, Rank rank){
        int oldIndex = chatsList.indexOf(new ChatData(chatId, null));
        if ( -1 == oldIndex ){
            return false;
        }

        ChatData old = chatsList.get(oldIndex);
        old.setRank( rank);
        return true;
    }

    /**
     * Used to change chat name
     * @param oldChatId old chat name
     * @param newChatId new chat name
     * @return true is success, false otherwise
     */
    public boolean changeChatName(String oldChatId, String newChatId){
        int oldIndex = chatsList.indexOf(new ChatData(oldChatId, null));
        if ( -1 == oldIndex ){
            return false;
        }

        ChatData old = chatsList.get(oldIndex);
        old.setId( newChatId);
        return true;
    }

    /**
     * Used to get chatList
     * @return chat list 
     */
    public ObservableList<ChatData> getChatList(){
        return chatsList.get();
    }

}