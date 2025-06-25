package com.EnderLite.DataController;

import java.util.Objects;

/**
 * Used to store chat data
 * @author Micro9261
 */
public class ChatData{
    private String chatId;
    private Rank rank;

    /**
     * Create instance of chatData
     * @param chatId chat name
     * @param rank client's user rank
     */
    public ChatData(String chatId, Rank rank){
        this.chatId = chatId;
        this.rank = rank;
    }

    /**
     * Used to get chat name
     * @return chat name
     */
    public String getId(){
        return this.chatId;
    }

    /**
     * Used to set chat name
     * @param id new name
     */
    public void setId(String id){
        this.chatId = id;
    }

    /**
     * Used to set rank for user
     * @param rank new rank
     */
    public void setRank(Rank rank){
        this.rank = rank;
    }

    /**
     * Used to get client's user rank
     * @return user rank 
     */
    public Rank getRank(){
        return this.rank;
    }

    @Override
    /**
     * checks only serwer name, because one user can't have 2
     * different ranks at the same time
     */
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

    @Override
    public int hashCode(){
        return Objects.hash(chatId, rank);
    }
}