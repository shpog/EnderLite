package com.EnderLite.DataController.ApiMessages;

import java.util.List;

public class Message{
    private String login;
    private String email;
    private String passw;
    private List<String> logins;
    private List<String> chats;
    private ResponseStatus status;
    
    public Message(String login, String email, String passw, List<String> logins, List<String> chats, ResponseStatus status){
        this.login = login;
        this.email = login;
        this.passw = login;
        this.logins = logins;
        this.chats = chats;
        this.status = status;
    }

    public void setLogin(String login){
        this.login = login;
    }

    public String getLogin(){
        return login;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    public void setPassw(String passw){
        this.passw = passw;
    }

    public String getPassw(){
        return passw;
    }

    public void setLogins(List<String> logins){
        this.logins = logins;
    }

    public List<String> getLogins(){
        return logins;
    }

    public void setChats(List<String> chats){
        this.chats = chats;
    }

    public List<String> getChats(){
        return chats;
    }

    public void setStatus(ResponseStatus status){
        this.status = status;
    }

    public ResponseStatus getStatus(){
        return status;
    }

}