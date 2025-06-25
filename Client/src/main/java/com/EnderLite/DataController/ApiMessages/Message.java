package com.EnderLite.DataController.ApiMessages;

import java.util.List;

/**
 * Message response container
 * @author Micro9261
 */
public class Message{
    private String login;
    private String email;
    private String passw;
    private List<String> logins;
    private List<String> chats;
    private ResponseStatus status;
    
    /**
     * Constructor with parameters
     * @param login login
     * @param email emial
     * @param passw password
     * @param logins logins list
     * @param chats chats list
     * @param status ResponseStatus type
     */
    public Message(String login, String email, String passw, List<String> logins, List<String> chats, ResponseStatus status){
        this.login = login;
        this.email = email;
        this.passw = passw;
        this.logins = logins;
        this.chats = chats;
        this.status = status;
    }

    /**
     * Sets login
     * @param login login to be set
     */
    public void setLogin(String login){
        this.login = login;
    }

    /**
     * Gets login
     * @return login contained in container
     */
    public String getLogin(){
        return login;
    }

    /**
     * Sets email
     * @param email email to be set
     */
    public void setEmail(String email){
        this.email = email;
    }

    /**
     * Gets email
     * @return email contained in container
     */
    public String getEmail(){
        return email;
    }

    /**
     * Sets password
     * @param passw password to be set
     */
    public void setPassw(String passw){
        this.passw = passw;
    }

    /**
     * Gets password
     * @return password contained in container
     */
    public String getPassw(){
        return passw;
    }

    /**
     * Sets logins list
     * @param logins list of Strings to be set
     */
    public void setLogins(List<String> logins){
        this.logins = logins;
    }

    /**
     * Gets logins list
     * @return String list
     */
    public List<String> getLogins(){
        return logins;
    }

    /**
     * Sets chats list
     * @param chats
     */
    public void setChats(List<String> chats){
        this.chats = chats;
    }

    /**
     * Gets chats names
     * @return String list
     */
    public List<String> getChats(){
        return chats;
    }

    /**
     * Sets response status type
     * @param status new status
     */
    public void setStatus(ResponseStatus status){
        this.status = status;
    }

    /**
     * Gets response status type
     * @return status contained in container
     */
    public ResponseStatus getStatus(){
        return status;
    }

}