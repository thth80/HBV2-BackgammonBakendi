package project.model;

import java.util.ArrayList;
import java.util.HashMap;

public class MSG {
	private static final String ACT = "action";
	
	public static HashMap<String, String> animateMessage(Integer[] froms, Integer[] tos, Boolean[] killMoves)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "animate");
		for(int i = 0; i < froms.length; i++)
		{
			msg.put("from"+ i, froms[i]+"");
			msg.put("to" + i, tos[i]+"");
			msg.put("kill"+ i, Boolean.toString(killMoves[i]));
		}
		return msg;
	}
	
	//TODO: Þessi skilaboð þurfa að vera send í Lobby storage
	public static HashMap<String, String> presentMatch(String playerOne, String playerTwo, int points, int addedTime)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "presentMatch");
		msg.put("playerOne", playerOne);
		msg.put("playerTwo", playerTwo);
		msg.put("points", points + "");
		msg.put("addedTime", addedTime + "");
		return msg;
	}
	
	public static HashMap<String, String> matchOver(String winner, String loser, int winPoints, int lossPoints)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "matchOver");
		msg.put("winner", winner);
		msg.put("loser", loser);
		msg.put("winPoints", winPoints+"");
		msg.put("lossPoints", lossPoints+"");
		return msg;
	}
	
	public static HashMap<String, String> gameOver(String winner, int cube, int mult, String type)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "gameOver");
		msg.put("winner", winner);
		msg.put("type", type);
		msg.put("cube", cube+"");
		msg.put("mult", mult+"");
		return msg;
	}
	
	public static HashMap<String, String> buildBoard(int[] counts, int[] teams,int[] diceVals ,int cube)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "wholeBoard");
		msg.put("cube", cube + "");
		
		for(int pos = 0; pos < teams.length; pos++)
		{
			msg.put("t" + pos, teams[pos] + "");
			msg.put("c" + pos, counts[pos] + "");
		}
		for(int i = 0; i < diceVals.length; i++)
			msg.put("d" + i, diceVals[i] + "");
		
		return msg;
	}
	
	public static HashMap<String, String> deletedEntry(String id)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "deletedEntry");
		msg.put("id", id);
		return msg;
	}
	
	public static HashMap<String, String> deletedEntries(ArrayList<String> removedIds)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		int index = 0;
		msg.put(ACT, "deletedEntries");
		for(String id: removedIds)
			msg.put(Integer.toString(index++), id);
		
		return msg;
	}
	
	public static HashMap<String, String> diceThrow(int first, int second, int team, String thrower)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "diceThrow");
		msg.put("firstDice", first+"");
		msg.put("secondDice", second+"");
		
		String teamStr = (team == GameManager.TEAM_WH)? "white" : "black" ;
		msg.put("team", teamStr);
		msg.put("thrower", thrower);
		return msg;
	}
	
	public static HashMap<String, String> explainMessage(String reason)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "explain");
		msg.put("explain", reason);
	
		return msg;
	}
	
	public static HashMap<String, String> mayEndTurn() 
	{									
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "mayEndTurn");
		return msg;
	}
	
	public static HashMap<String, String> inTurnMove(Integer[] froms, Integer[] tos, Boolean[] killMoves)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "inTurnMove");
		for(int i = 0; i < froms.length; i++)
		{
			msg.put("from" + i, froms[i] + "");
			msg.put("to" + i, tos[i] + "");
			msg.put("kill" + i, Boolean.toString(killMoves[i]));
		}
		return msg;
	}
	
	public static HashMap<String, String> chatBatch(ArrayList<String> chats)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "chatBatch");
		for(int i = 0; i < chats.size(); i++)
			msg.put(""+i, chats.get(i));
		
		return msg;
	}
	
	public static HashMap<String, String> newChat(String entry, String type)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "chatEntry");
		msg.put("entry", entry);
		msg.put("type", type);
		
		return msg;
	}
	
	public static HashMap<String, String> greenLight(int[] positions)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "greenLighted");
		for(int i = 0; i < positions.length; i++)
			msg.put(""+i, positions[i]+"");
		
		return msg;
	}
	
	public static HashMap<String, String> whiteLight(int[] positions)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "whiteLighted");
		for(int i = 0; i < positions.length; i++)
			msg.put(""+i, positions[i]+"");
		
		return msg;
	}
	
	public static HashMap<String, String> showButtons(boolean canDouble)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "showButtons");
		msg.put("canDouble", Boolean.toString(canDouble));
		return msg;
	}
	
	public static HashMap<String, String> trophyData(int id, int current)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "trophyEntry");
		msg.put("id", id+"");
		msg.put("percent", current+"");
		return msg;
	}
	
	public static HashMap<String, String> newOngoingEntryNoID(String human, String points, String addedTime,
													String bot)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "ongoingEntry");
		msg.put("playerOne", human);
		msg.put("points", points);
		msg.put("addedTime", addedTime);
		msg.put("playerTwo", bot);
		
		return msg;
	}
	
	public static HashMap<String, String> newOngoingEntry(HashMap<String, String> waitingEntry, String joiningPlayer)
	{
		HashMap<String, String> updatedMsg = new HashMap<String, String>();
		updatedMsg.put(ACT, "ongoingEntry");
		updatedMsg.put("playerTwo", joiningPlayer);
		
		waitingEntry.remove(ACT);
		waitingEntry.remove("playerTwo");
		
		for (HashMap.Entry<String, String> entry : waitingEntry.entrySet())
		    updatedMsg.put(entry.getKey(), entry.getValue());
	
		return updatedMsg;
	}
	
	public static HashMap<String, String> playerAccepted(String accepter)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "offerAccepted");
		msg.put("accepter", accepter);
		return msg;
	}
	
	public static HashMap<String, String> presentTrophy(int id)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "presentTrophy");
		msg.put("id", id+"");
		return msg;
	}
	
	public static HashMap<String, String> playerDoubled(String doubler,String decider ,int newStakes)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "playerDoubled");
		msg.put("doubler", doubler);
		msg.put("decider", decider);
		msg.put("stakes", newStakes+"");

		return msg;
	}
	
	public static HashMap<String, String> newVersusEntry(String recipient, String playerOne, String playerTwo,int pointsOne, 
			int pointsTwo, int pawnsOne, int pawnsTwo)
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "versusStats");
		msg.put("playerOne", recipient);
		
		if(recipient.equals(playerOne))
		{
			msg.put("playerTwo", playerTwo);
			msg.put("pointsOne", pointsOne+"");
			msg.put("pawnsOne", pawnsOne+"");
			msg.put("pointsTwo", pointsTwo+"");
			msg.put("pawnsTwo", pawnsTwo+"");
		}
		else
		{
			msg.put("playerTwo", playerOne);
			msg.put("pointsOne", pointsTwo+"");
			msg.put("pawnsOne", pawnsTwo+"");
			msg.put("pointsTwo", pointsOne+"");
			msg.put("pawnsTwo", pawnsOne+"");
		}
	
		return msg;
	}
	
	//Er sent til notenda með ID
	public static HashMap<String, String> waitingEntryNoID(String waiter, String points, String addedTime )
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "waitEntry");
		msg.put("playerOne", waiter);
		msg.put("playerTwo", "?");
		msg.put("points", points);
		msg.put("addedTime", addedTime);
		return msg;
	}
	
	public static HashMap<String, String> welcomeUser(String username) 
	{
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put(ACT, "legalSignup");
		msg.put("username", username);
		return msg;
	}
	
}