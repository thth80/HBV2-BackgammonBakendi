package project.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import project.model.DataCenter;
import project.model.MSG;
import project.model.MatchCollection;
import project.model.Move;
import project.model.LobbyManager;
import project.model.MatchManager;
import project.model.UMS;
import project.model.PostBoxes;

@RestController
public class MatchController {
	
	private static MatchCollection ongoingMatches = new MatchCollection();
	
	public static void addNewMatch(MatchManager match, String playerOne, String playerTwo)
	{
		ongoingMatches.addMatch(playerOne, match);
		ongoingMatches.addMatch(playerTwo, match);
	}
	
	public static boolean addObserver(int id, String observer)
	{
		MatchManager currMatch = getMatch(id);
		if(currMatch == null)
			return false;
		else
		{
			currMatch.addMatchObserver(observer);
			return true;
		}
	}
	
	private MatchController()
	{
		
	}
	
	@RequestMapping("/submitMatchChat")   
    public HashMap<String, String>[] submitChat(HttpSession session, HttpServletResponse response, 
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="chat", required=true) String chat)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	getMatch(username).receiveChatEntry(username, chat);
    	
    	return UMS.retrieveInGameMessages(username);
    }
	
	@RequestMapping("/accept")   
    public HashMap<String, String>[] accept(HttpSession session,
    		@RequestParam(value="name", required=true) String accepter)
    {
    	(getMatch(accepter)).onDoublingAccept();
    	return UMS.retrieveInGameMessages(accepter);
    }
	
	@RequestMapping("/reject")   
    public HashMap<String, String>[] reject(HttpSession session,
    		@RequestParam(value="name", required=true) String rejecter)
    {
		MatchManager currMatch = getMatch(rejecter);
    	currMatch.handleEndOfCurrentGame(rejecter, true);
    
    	if(currMatch.hasMatchEnded())
    	{
    		currMatch.finishMatchNormally(rejecter);
    		currMatch.closeMatch();
    	}
    	
    	return UMS.retrieveInGameMessages(rejecter);
    }
	
	@RequestMapping("/outOfTime")   
    public HashMap<String, String>[] outOfTime(HttpSession session,
    		@RequestParam(value="name", required=true) String player)
    {
		MatchManager currMatch = getMatch(player);
		
		currMatch.deliverMadeMoves();
    	currMatch.beginNewRound();
    	
    	return UMS.retrieveInGameMessages(player);
    }
	
	@RequestMapping("/diceThrow")   
    public HashMap<String, String>[] throwDice(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	(getMatch(username)).onDiceThrow();
    	return UMS.retrieveInGameMessages(username);
    }
    

    @RequestMapping("/greenSquare") 
    public HashMap<String, String>[] square(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="pos", required=true) int pos,
    		@RequestParam(value="name", required=true) String mover)
    {	
    	MatchManager currMatch = getMatch(mover);
    	currMatch.onGreenSquareClick(pos);
    	if(currMatch.hasGameEnded())
    	{
    		String loser = currMatch.getOtherPlayer(mover);
    		currMatch.deliverMadeMoves();
    		currMatch.handleEndOfCurrentGame(loser, false);
    		
    		if(currMatch.hasMatchEnded())
    		{
    			currMatch.finishMatchNormally(loser);
    			currMatch.closeMatch();
    		}
    	}
    	return UMS.retrieveInGameMessages(mover);
    }
    
    @RequestMapping("/startNewGame")   
    public HashMap<String, String>[] startNewGame(HttpSession session,
    		@RequestParam(value="name", required=true) String username)
    {
    	(getMatch(username)).startNewGame(username);
    	return UMS.retrieveInGameMessages(username);
    }
    
    
    @RequestMapping("/observerLeaving")   		
    public HashMap<String, String>[] observerLeavingMatch(HttpSession session , HttpServletResponse response,
    		@RequestParam(value="name", required=true) String leavingObserver)
    {
    	UMS.removeUser(leavingObserver, UMS.MATCH);
    	return (HashMap<String, String>[]) new HashMap[0];
    }
   
	
	@RequestMapping("/playerLeaving")   		
    public HashMap<String, String>[] playerLeavingMatch(HttpSession session , HttpServletResponse response,
    		@RequestParam(value="name", required=true) String leavingPlayer)
    {
    	UMS.removeUser(leavingPlayer, UMS.MATCH);
    	
    	MatchManager currMatch = getMatch(leavingPlayer);
    	currMatch.handleEndOfCurrentGame(leavingPlayer, true);
    	currMatch.handleLeavingPlayerEndingMatch(leavingPlayer);
    	currMatch.closeMatch();
    	
		ongoingMatches.removeMatch(leavingPlayer);
		int matchId = currMatch.getId();
		
		if(getMatch(matchId) == null)
			LobbyManager.deleteMatchEntry(matchId+ "");
    	
    	return (HashMap<String, String>[]) new HashMap[0];
    }
	
	@RequestMapping("/refreshMatch")   
    public HashMap<String, String>[] refreshMatch(HttpSession session,
    		@RequestParam(value="name", required=true) String username)
    {
    	return UMS.retrieveInGameMessages(username);
    }
	
	
	
    @RequestMapping("/endTurn")   
    public HashMap<String, String>[] doneMoving(HttpSession session ,HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	MatchManager currMatch = getMatch(username);
    	currMatch.onEndTurn();
    	
    	if(currMatch.hasGameEnded())
    	{
    		currMatch.handleEndOfCurrentGame(username, false);
    		if(currMatch.hasMatchEnded())
    		{
    			currMatch.finishMatchNormally(username);
    			currMatch.closeMatch();
    		}
    	}
    	
    	//höfum samband við LM ef það skal breyta status á leik
    		
    	return UMS.retrieveInGameMessages(username);
    }
    
    @RequestMapping("/cube")   
    public HashMap<String, String>[] flipCube(HttpSession session,
    		@RequestParam(value="name", required=true) String doubler)
    {
    	(getMatch(doubler)).onCubeFlip(); 
    	return UMS.retrieveInGameMessages(doubler);
    }
    
    private static MatchManager getMatch(int id)
	{
		return ongoingMatches.getMatch(id);
	}
    
    private static MatchManager getMatch(String username)
	{
		return ongoingMatches.getMatch(username);
	}
}
