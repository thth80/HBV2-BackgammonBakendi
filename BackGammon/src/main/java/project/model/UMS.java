package project.model;

import java.util.HashMap;

public class UMS {
	private static PostBoxes lobbyMessages = new PostBoxes();
	private static PostBoxes gameMessages = new PostBoxes();
	private static PostBoxes trophyMessages = new PostBoxes();
	private static PostBoxes statsMessages = new PostBoxes();
	private static PostBoxes generalMessages = new PostBoxes();
	
	public static final int LOBBY = 1;
	public static final int MATCH = 2;
	public static final int TROPHY = 3;
	public static final int STATS = 4;
	public static final int GENERAL = 5;
	
	private UMS() //HVAÐ MEÐ skilaboð til BOT?
	{
		
	}
	
	public synchronized static void addUser(String username, int type) 
	{														
		switch(type)
		{
			case LOBBY:
				lobbyMessages.addPostBox(username);
				break;
			case MATCH:
				gameMessages.addPostBox(username);
				break;
			case TROPHY:
				trophyMessages.addPostBox(username);
				break;
			case STATS:
				statsMessages.addPostBox(username);
				break;
			case GENERAL:
				generalMessages.addPostBox(username);
				break;
		}
	}
	public synchronized static void removeUser(String username, int type)
	{
		switch(type)
		{
			case LOBBY:
				lobbyMessages.removePostBox(username);
				break;
			case MATCH:
				gameMessages.removePostBox(username);
				break;
			case TROPHY:
				trophyMessages.removePostBox(username);
				break;
			case STATS:
				statsMessages.removePostBox(username);
				break;
			case GENERAL:
				generalMessages.removePostBox(username);
				break;
		}
	}
	
	public static void storeInGameMessageLast(String[] usernames, HashMap<String,String> msg)
	{
		for(String user: usernames)
			gameMessages.storeMessage(user, msg);
	}
	
	public static void storeLobbyMessages(String user, HashMap<String, String>[] msgs)
	{
		lobbyMessages.storeMessages(user, msgs);
	}
	public static void storeLobbyMessage(String user, HashMap<String, String> msg)
	{
		lobbyMessages.storeMessage(user, msg);
	}
	public static void storeLobbyMessage(String[] userList, HashMap<String,String> msg)
	{
		for(String user: userList)
			lobbyMessages.storeMessage(user, msg);
	}
	
	public static void storeLobbyMessageLast(String[] userList, HashMap<String,String> msg)
	{
		for(String user: userList)
			lobbyMessages.storeMessage(user, msg);
	}
	
	public static void storeTrophyMessages(String user, HashMap<String,String>[] msgs)
	{
		trophyMessages.storeMessages(user, msgs);
	}
	public static void storeTrophyMessage(String user, HashMap<String,String> msg)
	{
		trophyMessages.storeMessage(user, msg);
	}
	
	public static void storeStatsMessages(String user, HashMap<String,String>[] msgs)
	{
		statsMessages.storeMessages(user, msgs);
	}
	public static void storeStatsMessage(String user, HashMap<String,String> msg)
	{
		statsMessages.storeMessage(user, msg);
	}
	
	public static void storeInGameMessage(String[] userList, HashMap<String,String> msg)
	{
		for(String user: userList)
			gameMessages.storeMessage(user, msg);
	}
	public static void storeInGameMessage(String user, HashMap<String,String> msg)
	{
		gameMessages.storeMessage(user, msg);
	}
	public static void storeInGameMessages(String[] userList, HashMap<String,String>[] msgs)
	{
		for(String user: userList)
			gameMessages.storeMessages(user, msgs);
	}
	public static void storeInGameMessages(String user, HashMap<String,String>[] msgs)
	{
		gameMessages.storeMessages(user, msgs);
	}
	
	public static void storeGeneralMessages(String username, HashMap<String,String>[] msgs)
	{
		generalMessages.storeMessages(username, msgs);
	}
	
	public static HashMap<String, String>[] retrieveInGameMessages(String username)
	{
		return gameMessages.getMessages(username);
	}
	
	public static HashMap<String, String>[] retrieveLobbyMessages(String username)
	{
		return lobbyMessages.getMessages(username);
	}
	
	public static HashMap<String, String>[] retrieveTrophyMessages(String username)
	{
		return trophyMessages.getMessages(username);
	}
	
	public static HashMap<String, String>[] retrieveStatsMessages(String username)
	{
		return statsMessages.getMessages(username);
	}
	
	/*public static MSG[] dumpAllMessages()
	{
		String[] allUsers = msgsAwaitingUsers.getOwnerList();
		MSG[] msgs = new MSG[0];
		for(String user: allUsers)
			msgs = MSG.mergeMessages(msgs, msgsAwaitingUsers.getMessages(user));
		
		return msgs;
	}
	public static String[] dumpAllUsers()
	{
		return msgsAwaitingUsers.getOwnerList();
	}  */
}
