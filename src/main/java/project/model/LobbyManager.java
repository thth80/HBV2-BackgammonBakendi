package project.model;

import java.util.ArrayList;
import java.util.HashMap;

public class LobbyManager {
	
	private static final String WAITING = "WAITING";
	private static final String ONGOING = "ONGOING";
	private static PostBoxes lobbyLists = setNewPostBoxes();
	private static StringList chatEntries = new StringList();
	private static StringList subscribers = new StringList();
	private static int globalId = 0;
	
	private LobbyManager()
	{
		
	}
	
	public static int createBotMatchEntry(HashMap<String, String> matchData)
	{
		int id = globalId++;
		matchData.put("id", id+"");
		lobbyLists.storeMessage(ONGOING, matchData);
		UMS.storeLobbyMessage(subscribers.toArray(), matchData);
		return id;
	}
	
	//einungis í þeim tilgangi gert að prófa framendann.
	public static PostBoxes setNewPostBoxes()
	{
		PostBoxes postboxes = new PostBoxes(WAITING, ONGOING);
		HashMap<String, String> ongoing = new HashMap<String, String>();
		ongoing.put("action", "ongoingEntry");
		ongoing.put("playerOne", "TolfStafir12");
		ongoing.put("playerTwo", "TolfStafir12");
		ongoing.put("points", "5");
		ongoing.put("addedTime", "0");
		ongoing.put("id", "699");
		
		postboxes.storeMessage(ONGOING, ongoing);
		
		HashMap<String, String> waiting = new HashMap<String, String>();
		waiting.put("action", "waitEntry");
		waiting.put("playerOne", "addi");
		waiting.put("playerTwo", "?");
		waiting.put("points", "9");
		waiting.put("addedTime", "45");
		waiting.put("id", "7800");
		
		postboxes.storeMessage(WAITING, waiting);
		
		waiting = new HashMap<String, String>();
		waiting.put("action", "waitEntry");
		waiting.put("playerOne", "addi");
		waiting.put("playerTwo", "?");
		waiting.put("points", "1");
		waiting.put("addedTime", "15");
		waiting.put("id", "7876700");
		
		postboxes.storeMessage(WAITING, waiting);
		
		waiting = new HashMap<String, String>();
		waiting.put("action", "waitEntry");
		waiting.put("playerOne", "eee");
		waiting.put("playerTwo", "?");
		waiting.put("points", "9");
		waiting.put("addedTime", "0");
		waiting.put("id", "78099"); 
		
		postboxes.storeMessage(WAITING, waiting);
		
		waiting = new HashMap<String, String>();
		waiting.put("action", "waitEntry");
		waiting.put("playerOne", "derp");
		waiting.put("playerTwo", "?");
		waiting.put("points", "3");
		waiting.put("addedTime", "0");
		waiting.put("id", "7809229"); 
		
		postboxes.storeMessage(WAITING, waiting);
		return postboxes;
	}
	
	public static void subscribeUser(String newUser) 
	{
		subscribers.addStringEntry(newUser);  
		
		ArrayList<String> recentChat = chatEntries.getMostRecent();
		HashMap<String, String> chatMessage = MSG.chatBatch(recentChat);
			
		UMS.storeLobbyMessages(newUser, lobbyLists.readMessages(WAITING));
		UMS.storeLobbyMessages(newUser, lobbyLists.readMessages(ONGOING));
		UMS.storeLobbyMessage(newUser, chatMessage);
	}
	
	public static void unSubscribe(String user)
	{
		subscribers.removeString(user);
	}
	
	public static void playerExitingApplication(String username)
	{
		//playerExitingMatch(username);
		subscribers.removeString(username);
		deleteWaitEntries(username);
	}
	
	public static void storeWaitListEntry(HashMap<String, String> waitData) 
	{
		int freshId = globalId++;
		waitData.put("id", freshId+"");
		lobbyLists.storeMessage(WAITING, waitData);
		UMS.storeLobbyMessage(subscribers.toArray(), waitData);
	}
	
	//MJög skrýtin aðferð, virkar röng
	public static void matchEnded(String[] players) 
	{
		String id = "5";
		removeOngoingMatchEntries(players);
		UMS.storeLobbyMessage(subscribers.toArray(), MSG.deletedEntry(id));
	}
	
	public static void receiveChatEntry(String username, String chat)
	{
		String formatted = chatEntries.formatChatString(chat, username);
		chatEntries.addStringEntry(formatted);
		UMS.storeLobbyMessage(subscribers.toArrayExcept(username), MSG.newChat(formatted, "lobby"));
	}
	
	public static void updateMatchEntries(HashMap<String, String> matchEntry)
	{
		UMS.storeLobbyMessage(subscribers.toArray(), matchEntry);
		lobbyLists.storeMessage(ONGOING, matchEntry);
	}
	
	public static synchronized HashMap<String, String> createMatchEntryIfPossible(int waitId, String joiningPlayer)
	{
		HashMap<String, String> waitingEntryMessage = lobbyLists.readMessage(WAITING, waitId);
		if(waitingEntryMessage != null)
		{	
			HashMap<String, String> newOngoing = MSG.newOngoingEntry(waitingEntryMessage, joiningPlayer);			
			return newOngoing;
		}
		else
		{
			UMS.storeLobbyMessage(joiningPlayer, MSG.explainMessage("Match no longer available"));
			return null;
		}
	}
	
	public static void deleteMatchEntry(String id)
	{
		lobbyLists.removeMessage(ONGOING, id);
		UMS.storeLobbyMessage(subscribers.toArray(), MSG.deletedEntry(id));
	}
	
	public static void deleteWaitEntries(String player)
	{
		ArrayList<String> removedIds = lobbyLists.removeAllPlayerEntries(WAITING, player);
		HashMap<String, String> deletedEntries = MSG.deletedEntries(removedIds);
		
		UMS.storeLobbyMessage(subscribers.toArray(), deletedEntries);
	}
	
	public static void deleteSingleWaitEntry(String username, String id)
	{
		lobbyLists.removeMessage(WAITING, id);
		HashMap<String, String> deletedEntry = MSG.deletedEntry(id);
		UMS.storeLobbyMessage(subscribers.toArrayExcept(username), deletedEntry);
	
	}
	
	public static void removeOngoingMatchEntries(String[] players)
	{
		for(String player: players)
			removeOngoingMatchEntry(player);
	}
	public static void removeOngoingMatchEntry(String player)
	{
		ArrayList<String> removedIds = lobbyLists.removeAllPlayerEntries(ONGOING, player);
		HashMap<String, String> deletedEntries = MSG.deletedEntries(removedIds);
		
		UMS.storeLobbyMessage(subscribers.toArray(), deletedEntries);
	}
	
}
