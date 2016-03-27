package project.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Service;

@Service
public class MatchManager {
	private static final int NO_CLOCK = 0;
	
	private GameManager gameState;
	private StringList chatEntries;
	private int id, whitePoints, blackPoints, addedTime, pointsToWin;
	private boolean humanMatch, isOngoing;
	private  String[] players;
	
	public MatchManager()
	{
		//VIRKAR ÞETTA?
		int addi = 1;
	}
	
	private MatchManager(String playerWhite, String playerBlack, int id)
	{
		players = new String[]{playerWhite, playerBlack};
		chatEntries = null;
		gameState = null;
		
		chatEntries = new StringList();
		whitePoints = blackPoints = 0;
		this.id = id;
		isOngoing = true;
	}
	
	public static MatchManager createNewHumanMatch(HashMap<String,String> matchData)
	{
		String whitePlayer = matchData.get("playerOne");
		String blackPlayer = matchData.get("playerTwo");
		MatchManager match = new MatchManager(whitePlayer, blackPlayer, Integer.parseInt(matchData.get("id")));
		match.gameState = GameManager.regularGame(whitePlayer, blackPlayer);
		
		match.addedTime = (matchData.get("clock").equals("true"))? Integer.parseInt(matchData.get("addedTime")): NO_CLOCK ;
		match.humanMatch = true;
		match.pointsToWin = Integer.parseInt(matchData.get("points"));
		
		return match;
	}
	
	public static MatchManager createNewBotMatch(HashMap<String, String> matchData, int id)
	{	
		String botName = "EASY_BOT";
		if(matchData.get("difficulty").equals("medium")) botName = "MED_BOT";
		else if(matchData.get("difficulty").equals("hard")) botName = "HARD_BOT";
		
		MatchManager match = new MatchManager(matchData.get("playerOne"), botName, id);
		
		match.gameState = GameManager.regularGame(matchData.get("playerOne"), botName);
		match.addedTime = (matchData.get("clock").equals("true"))? Integer.parseInt(matchData.get("addedTime")): NO_CLOCK ;
		match.humanMatch = false;
		match.pointsToWin = Integer.parseInt(matchData.get("points"));
		
		return match;
	}
	
	public void onGreenSquareClick(int squarePos) 
	{
		gameState.greenSquareClicked(squarePos);
	}
	
	public void onDiceThrow()
	{
		gameState.diceWasThrown();
	}
	
	public void onCubeFlip()
	{
		gameState.doublingCubeWasFlipped();
	}
	
	public void onDoublingAccept()
	{
		gameState.doublingAccepted();
	}
	
	//LEIKUR GETUR EKKI KLÁRAST HÉR AÐ NEÐAN **NEMA** BOT VINNI
	public void onTurnFinish()		
	{
		String turnFinisher = gameState.getCurrentUserName();
		if(humanMatch)
			gameState.userEndedTurnInHumanGame();  
		else
			gameState.userEndedTurnInBotGame(); 
			
			
		if(!gameState.hasGameEnded()) 
		{
			if(addedTime != NO_CLOCK)
				UMS.storeInGameMessage(turnFinisher, MSG.addedTime(addedTime));
			return;
		}
		
		handleEndOfCurrentGame(turnFinisher, false);
		gameState = GameManager.cloneGame(gameState); 
			
		if(!hasMatchEnded())
			startNewGame();
		else
		{
			storeMatchResults(getBlackPlayer(), getWhitePlayer(), blackPoints, whitePoints);
			closeMatch();
			UMS.storeInGameMessage(gameState.getSubscribers(),
					MSG.matchOver(getBlackPlayer(), getWhitePlayer(), blackPoints, whitePoints )); 
		}
	}
	
	public void startMatch()
	{
		startNewGame();
		UMS.storeInGameMessage(gameState.getSubscribers(), 
				MSG.presentMatch(players[0], players[1], this.pointsToWin, this.addedTime));
	}
	
	public void startNewGame()
	{
		int startingTeam = gameState.makeOpeningThrow();
		
		if(!humanMatch && startingTeam == GameManager.TEAM_BL) 
			gameState.botStartsGame();
		else		   
			gameState.humanStartsGame(startingTeam);
	}
	
	public void receiveChatEntry(String username, String chat)
	{
		String formattedChat = chatEntries.formatChatString(chat, username);
		chatEntries.addStringEntry(formattedChat);
		UMS.storeInGameMessage(gameState.getSubscribers(), MSG.newChat(formattedChat, "match"));
	}
	
	public void addMatchObserver(String observer)
    {
    	gameState.addObserver(observer);	
   
    	ArrayList<String> chatForObserver = chatEntries.getMostRecent();
    	HashMap<String, String> chatMessages = MSG.chatBatch(chatForObserver);
    	
    	UMS.storeInGameMessage(observer, chatMessages);
    	
    	Board copy = gameState.getBoardCopy();
    	gameState.reverseMovesMadeThisTurn(copy);
    	gameState.deliverEntireGameState(observer, copy);
    }
	
	public boolean hasMatchEnded()
	{
		return whitePoints >= pointsToWin || blackPoints >= pointsToWin;
	}

	public int getTimeAddition()
	{
		return this.addedTime;
	}
	
	public void beginNewRound()
	{
		gameState.beginNewRound();
	}
	
	public void handleEndOfCurrentGame(String loser, boolean forfeitedGame)
	{
		String winner = gameState.getOtherUserName(loser);
		
		HashMap<String, Integer> results = gameState.getEndGameResults(winner);
		int pointsToWinner = (!forfeitedGame)? results.get("cubeValue")*results.get("multiplier") :results.get("cubeValue");
		updateMatchScore(results.get("winner"), pointsToWinner);
		storeGameResults(results, winner, loser, pointsToWinner);
		
		String type = (forfeitedGame)? "rejected": "regular" ;
		UMS.storeInGameMessage(gameState.getSubscribers(),
				MSG.gameOver(winner,results.get("multiplier"),results.get("cubeValue"), type));
	}
	
	public String getOtherPlayer(String name)
	{
		return gameState.getOtherUserName(name);
	}
	
	public void initNewGame()
	{
		gameState = GameManager.cloneGame(gameState);
	}
	
	private void storeGameResults(HashMap<String, Integer> results, String winner, String loser, int winPoints)
	{
		try
		{
			while(!DataCenter.storeSingleGameResults(winner, loser, winPoints, results.get("cubeValue"),results.get("multiplier"), 
					results.get("winnerPawns"), results.get("loserPawns"),results.get("winnerSteps"),results.get("loserSteps"),  1  )) 
				;
			while(!DataCenter.updateGameVersus(winner,loser,winPoints,15-results.get("winnerPawns"),15-results.get("loserPawns")))
				;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void observerLeftMatch(String leavingUser)
	{
		gameState.removeObserver(leavingUser);
	}
	
	public void handleLeavingPlayerEndingMatch(String leaver)
	{
		String winner = gameState.getOtherUserName(leaver);
		boolean quitterHadMorePoints = (getPoints(leaver) > getPoints(winner));
		int winPoints = (quitterHadMorePoints)? Math.min(blackPoints, whitePoints): Math.max(blackPoints, whitePoints);
		int lossPoints = (quitterHadMorePoints)? Math.max(blackPoints, whitePoints): Math.min(blackPoints, whitePoints);
		
		storeMatchResults(winner, leaver, winPoints, lossPoints);
		
		UMS.storeInGameMessage(gameState.getSubscribers(),
				MSG.matchOver(winner, leaver, winPoints, lossPoints));
	}
	
	public void closeMatch()
	{
		isOngoing = false;
	}
	
	private int getPoints(String playerName)
	{
		if(playerName.equals(getBlackPlayer()))
			return blackPoints;
		else
			return whitePoints;
	}
	
	public void finishMatchNormally(String loser)
	{
		String winner = gameState.getOtherUserName(loser);
		int winPoints = Math.max(blackPoints, whitePoints);	
		int lossPoints = Math.min(blackPoints, whitePoints);	
		
		storeMatchResults(winner, loser, winPoints, lossPoints);
		UMS.storeInGameMessage(gameState.getSubscribers(),MSG.matchOver(winner, loser, winPoints, lossPoints));
	}
	
	public void deliverMadeMoves()
	{
		gameState.onlyIncludeValidMoves();
		gameState.deliverEndMovesToOthers();
	}
	
	public boolean hasGameEnded()
	{
		return gameState.hasGameEnded();
	}
	
	private String getBlackPlayer()
	{
		return players[1];
	}
	private String getWhitePlayer()
	{
		return players[0];
	}
	
	private void updateMatchScore(int winner, int totalMult)
	{
		if(winner == GameManager.TEAM_WH) this.whitePoints += totalMult;
		else							  this.blackPoints += totalMult;
	}
	
	private void storeMatchResults(String winner, String loser, int winPoints, int lossPoints)
	{
		try
		{
			while(!DataCenter.storeMatchResults(winner, loser, pointsToWin ,winPoints, lossPoints,  1  ));
			while(!DataCenter.checkForNewTrophies(winner));
			while(!DataCenter.checkForNewTrophies(loser));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
    
    public int getId()
    {
    	return id;
    }
}
