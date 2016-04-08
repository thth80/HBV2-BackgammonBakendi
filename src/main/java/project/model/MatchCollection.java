package project.model;

import java.util.ArrayList;

import project.model.PostBoxes.PostBox;

public class MatchCollection {
	public class MatchData
	{
		public MatchManager match;
		public int matchId;
		public String username;
		
		public MatchData(MatchManager match, int id, String username)
		{
			this.match = match;
			matchId = id;
			this.username = username;
		}
	}
	
	private ArrayList<MatchData> allMatches;
	public MatchCollection()
	{
		allMatches = new ArrayList();
	}
	public void addMatch(String username, MatchManager psm)
	{
		MatchData newMatch = new MatchData(psm, psm.getId(), username);
		allMatches.add(newMatch);
	}
	
	public MatchManager getMatch(int id)
	{
		for(MatchData data: allMatches)
			if(data.matchId == id)
				return data.match;
		return null;
	}
	
	public MatchManager getMatch(String user)
	{
		for(MatchData data: allMatches)
			if(data.username.equals(user))
				return data.match;
		return null;
	}
	
	public void removeMatch(String username)
	{
		for(int i = allMatches.size() - 1; i >= 0; i--)
			if(allMatches.get(i).username.equals(username))
			{
				allMatches.remove(i);
				return;
			}
	}
	
}
