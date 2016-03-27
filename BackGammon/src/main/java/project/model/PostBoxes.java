package project.model;

import java.util.ArrayList;
import java.util.HashMap;

public class PostBoxes {
	
	public class PostBox
	{
		private String username;
		private ArrayList<HashMap<String, String>> messages;
		
		public PostBox(String username)
		{
			this.username = username;
			this.messages = new ArrayList<HashMap<String, String>>();
		}
		
		public void putItemLast(HashMap<String, String> message)
		{
			messages.add(message);
		}
		
		public boolean isOwnedBy(String user)
		{
			return username.equals(user);
		}
		
		public String getOwner()
		{
			return username;
		}
		
		public Object peekAtTop()
		{	
			return messages.get(messages.size() - 1);
		}
		
		public HashMap<String, String>[] retrieveAllMessages()
		{
			HashMap<String, String>[] returnMessages = (HashMap<String, String>[]) messages.toArray();
			messages.clear();
			return returnMessages;
		}
		
		public HashMap<String, String>[] readAllMessages()
		{
			return convertToArray(messages);
		}
		
		private HashMap<String, String>[] convertToArray(ArrayList<HashMap<String, String>> toConvert)
		{
			HashMap<String, String>[] arr = new HashMap[toConvert.size()];
			int index = 0;
			for(HashMap<String, String> msg: toConvert)
				arr[index++] = msg;
				
			return (HashMap<String, String>[])arr;
		}
		
		public HashMap<String, String> readMessage(int id)
		{
			for(HashMap<String, String> msg: messages)
			{
				if(msg.get("id") != null && msg.get("id").equals(id+""))
				    return msg;
			}
			return null;
		}
		
		public void removeMessage(int id)
		{
			for(HashMap<String, String> msg: messages)
			{
				if(msg.get("id") != null && msg.get("id").equals(id+""))
				{
					messages.remove(msg);
				    return;
				}
			}
		}
		
		public ArrayList<String> removeWaitingEntry(String waiter)
		{	
			ArrayList<String> removedIds = new ArrayList();
			for(HashMap<String, String> msg: messages)
			{
				if(msg.get("playerOne") != null && msg.get("playerOne").equals(waiter))
				{
					messages.remove(messages.indexOf(msg));
					removedIds.add(msg.get("id"));
				}
			}
			return removedIds;
		}
	}
	
	
	private ArrayList<PostBox> postBoxes;
	public PostBoxes()
	{
		postBoxes = new ArrayList<PostBox>();
	}
	
	public PostBoxes(String userOne, String userTwo)
	{
		postBoxes = new ArrayList<PostBox>();
		postBoxes.add(new PostBox(userOne));
		postBoxes.add(new PostBox(userTwo));
	}
	
	public void addPostBox(String username)
	{
		postBoxes.add(new PostBox(username));
	}
	
	public void removeMessage(String username, int id)
	{
		PostBox user = getPostBox(username);
		user.removeMessage(id);
	}
	public ArrayList<String> removeAllPlayerEntries(String entryType, String player)
	{
		PostBox waitingList = getPostBox(entryType);
		return waitingList.removeWaitingEntry(player);
	}
	
	public HashMap<String, String> readMessage(String username, int id)
	{
		PostBox user = getPostBox(username);
		return user.readMessage(id);
	}
	
	public synchronized void removePostBox(String username)
	{
		for(PostBox box: postBoxes)
			if(box.isOwnedBy(username))
			{
				postBoxes.remove(box);
				return;
			}
	}
	
	private PostBox getPostBox(String username)
	{
		for(PostBox box: postBoxes)
			if(box.isOwnedBy(username))
				return box;
		return null;
	}
	
	public String[] getOwnerList()
	{
		String[] users = new String[postBoxes.size()];
		int index = 0;
		for(PostBox box: postBoxes)
			users[index++] = box.getOwner();
		
		return users;
	}
	
	public void storeMessages(String username, HashMap<String, String>[] msgs)
	{
		PostBox userBox = getPostBox(username);
		if(userBox == null) return;
		for(HashMap<String, String> incomingMsg: msgs)
			userBox.putItemLast(incomingMsg);
	}
	
	public void storeMessage(String username, HashMap<String, String> msg)
	{
		PostBox user = getPostBox(username);
		if(user != null) user.putItemLast(msg);
	}
	
	public HashMap<String, String>[] getMessages(String username)
	{
		PostBox userBox = getPostBox(username);
		if(userBox == null) return null;
		else				return userBox.retrieveAllMessages();
	}
	
	public HashMap<String,String>[] readMessages(String username)
	{
		if(getPostBox(username) == null) return null;
		else							return getPostBox(username).readAllMessages();

	}
}
