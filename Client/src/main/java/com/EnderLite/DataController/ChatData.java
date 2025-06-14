package com.EnderLite.DataController;

public class ChatData{
    private String chatId;
    private Rank rank;

    ChatData(String chatId, Rank rank){
        this.chatId = chatId;
        this.rank = rank;
    }

    //chatId
    public String getId(){
        return this.chatId;
    }

    public void setId(String id){
        this.chatId = id;
    }

    //rank
    public void setRank(Rank rank){
        this.rank = rank;
    }

    public Rank getRank(){
        return this.rank;
    }

    //checks only serwer name, because one user can't have 2
    //different ranks at the same time
    @Override
    public boolean equals(Object ob){

        if ( ob == this ){
            return true;
        }

        if ( !(ob instanceof ChatData) ){
            return false;
        }

        ChatData other = (ChatData)ob;
        return other.getId().equals( this.chatId );
    }
}