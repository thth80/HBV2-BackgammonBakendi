package project.model;

import java.util.ArrayList;

public class StringList {
	
	private ArrayList<String> strings;

	public StringList()
	{
		strings = new ArrayList<String>();
	}
	
	public int getSize()
	{
		return strings.size();
	}
	
	public void addStringEntry(String entry)
	{
		strings.add(entry);
	}
	
	public void addStringEntryLast(String entry)
	{
		addStringEntry(entry);
	}
	
	public String formatChatString(String chat, String writer)
	{
		return "["+writer + "]: " + chat; 
	}
	
	public ArrayList<String> getMostRecent()
	{
		if(strings.size() > 70)
			removeOldest(50);
		
		return strings;
	}
	private void removeOldest(int remaining)
	{
		ArrayList<String> afterRemoval = new ArrayList();
		for(int i = strings.size() - remaining; i < strings.size(); i++ )
			afterRemoval.add(strings.get(i));
		strings = afterRemoval;
	}
	
	public void removeString(String entry)
	{
		for(String s: strings)
			if(s.equals(entry))
			{
				strings.remove(strings.indexOf(s));
				return;
			}
	}
	
	public String[] toArray()
	{
		Object[] obj = strings.toArray();
		String[] strings = new String[obj.length];
		int index = 0;
		
		for(Object o: obj)
			strings[index++] = (String)o;
		return strings;
	}
	
	//Gert ráð fyrir að except sé í listanum? Prófum það fyrst
	public String[] toArrayExcept(String except)
	{
		String[] remaining = new String[strings.size() - 1];
		int index = 0;
		for(String s: strings)
			if(!s.equals(except))
				remaining[index++] = s;
		
		return remaining;
	}
	
	//Gerum aftur ráð fyrir að allir strengir í excepts séu í staflanum
	public String[] toArrayExcept(String[] excepts)
	{
		String[] remaining = new String[strings.size() - excepts.length];
		int index = 0;
		for(String s: strings)
		{
			boolean foundMatch = false;
			for(String ex: excepts)
				if(s.equals(ex))
				{
					foundMatch = true;
					break;
				}
			if(!foundMatch) 
				remaining[index++] = s;
		}
		return remaining;
	}
	
	public String[] deleteAllEntriesContaining(String username)  
	{
		ArrayList<String> removed = new ArrayList();
		for(String entry: strings)
			if(entry.contains(username))
			{
				strings.remove(strings.indexOf(entry));
				removed.add(entry);
			}
		
		return (String[])removed.toArray();
	}
}
