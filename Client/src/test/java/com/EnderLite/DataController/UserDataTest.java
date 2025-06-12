package com.EnderLite.DataController;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class UserDataTest {
    private UserData userData;
    /**
     * Rigorous Test :-)
     */
    @BeforeEach
    public void setUp(){
        userData = new UserData();
    }
     
    @Test
    public void CreatingWithoutLoginAndEmail() {
        assertEquals("", userData.getLogin());
        assertEquals("", userData.getEmail());
    }

    @Test
    public void CreatingWithLoginAndEmail(){
        userData = new UserData("test", "test@gmail.com");

        assertEquals("test", userData.getLogin());
        assertEquals("test@gmail.com", userData.getEmail());
    }

    @Test
    public void setAndGetPassword(){
        userData.setPassw("12345");

        assertEquals("12345", userData.getPassw());
    }

    @Test
    public void ChangeEmailAndLogin(){
        userData.setEmail("testing@gmail.com");
        assertEquals("testing@gmail.com", userData.getEmail());

        userData.setLogin("tested");
        assertEquals("tested", userData.getLogin());
    }

    //Friends

    @Test
    public void addFriendsAndFindFromUserData(){
        userData.addFriend("DemonSlayer3000");
        userData.addFriend("Shaman");
        userData.addFriend("User3303");

        List<String> checkList = userData.getFriendsList();
        assertNotEquals(-1, checkList.indexOf("DemonSlayer3000"));
        assertNotEquals(-1, checkList.indexOf("Shaman"));
        assertNotEquals(-1, checkList.indexOf("User3303"));
    }

    @Test
    public void findAndRemoveFriendFromUserData(){
        userData.addFriend("DemonSlayer3000");
        userData.addFriend("Shaman");
        userData.addFriend("User3303");

        assertTrue(userData.removeFriend("Shaman"));
        assertFalse(userData.removeFriend("Shaman"));
        assertFalse(userData.removeFriend("test"));
    }

    @Test
    public void findAndChangeFriendLoginInUserData(){
        userData.addFriend("Shaman");
        userData.addFriend("User3303");

        userData.changeFriendLogin("Shaman", "newPlayer");

        List<String> checkList = userData.getFriendsList();

        assertEquals(-1, checkList.indexOf("Shaman"));
        assertNotEquals(-1, checkList.indexOf("newPlayer"));
    }

    //Chats

    @Test
    public void addAndFindChatsToUserData(){
        userData.addChat("Chat1", Rank.USER);
        userData.addChat("Chat2", Rank.ADMIN);

        List<ChatData> checkList = userData.getChatList();

        assertNotEquals(-1, checkList.indexOf(new ChatData("Chat1", null)));
        assertNotEquals(-1, checkList.indexOf(new ChatData("Chat2", null)));
        assertEquals(-1, checkList.indexOf(new ChatData("null", null)));
    }

    @Test
    public void findAndGetChatRankFromUserData(){
        userData.addChat("Chat1", Rank.USER);
        userData.addChat("Chat2", Rank.ADMIN);

        assertEquals(Rank.ADMIN, userData.getChatRank("Chat2"));
        assertEquals(Rank.USER, userData.getChatRank("Chat1"));
    }

    @Test
    public void findAndRemoveChatsFromUserData(){
        userData.addChat("Chat1", Rank.USER);
        userData.addChat("Chat2", Rank.ADMIN);
        userData.addChat("Chat3", Rank.ADMIN);

        List<ChatData> checkList = userData.getChatList();

        userData.removeChat("Chat2");
        userData.removeChat("Chat3");

        assertNotEquals(-1, checkList.indexOf(new ChatData("Chat1", null)));
        assertEquals(-1, checkList.indexOf(new ChatData("Chat2", null)));
        assertEquals(-1, checkList.indexOf(new ChatData("Chat3", null)));
    }


    @Test
    public void findAndChangeChatNameInUserData(){
        userData.addChat("Chat1", Rank.USER);

        List<ChatData> checkList = userData.getChatList();

        userData.changeChatName("Chat1", "Testing");
        assertEquals(-1, checkList.indexOf(new ChatData("Chat1", null)));
        assertNotEquals(-1, checkList.indexOf(new ChatData("Testing", null)));
    }

    @Test
    public void findAndChangeChatRankInUserData(){
        userData.addChat("Chat1", Rank.USER);

        userData.changeChatRank("Chat1", Rank.ADMIN);
        assertEquals(Rank.ADMIN, userData.getChatRank("Chat1"));
    }
}
