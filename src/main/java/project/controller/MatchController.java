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
	
	
	/*
	 * Senda gameOver skilaboð  X
	 * Refresha Game Board   	X
	 * Senda matchOver ef Match búinn á alla  X  
	 */
	@RequestMapping("/reject")   
    public HashMap<String, String>[] reject(HttpSession session,
    		@RequestParam(value="name", required=true) String rejecter)
    {
		MatchManager currMatch = getMatch(rejecter);
		currMatch.onPlayerRejecting(rejecter);
    	
    	return UMS.retrieveInGameMessages(rejecter);
    }
	
	@RequestMapping("/timeOut")   
    public HashMap<String, String>[] outOfTime(HttpSession session,
    		@RequestParam(value="name", required=true) String player)
    {
		MatchManager currMatch = getMatch(player);
		
		currMatch.onEndTurn();
    	
    	return UMS.retrieveInGameMessages(player);
    }
	
	@RequestMapping("/diceThrow")   
    public HashMap<String, String>[] throwDice(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	(getMatch(username)).onDiceThrow();
    	return UMS.retrieveInGameMessages(username);
    }
    

	/*
	 * BLOKKA Hvíta reiti á framenda X
	 * Blokkva End Turn á framenda X
	 * Senda einungis rétt move X
	 * Refresha Game Board   X
	 * Senda gameOver ef leikur búinn  X
	 * Senda matchOver skilaboð á rétta staði X
	 */
    @RequestMapping("/greenSquare") 
    public HashMap<String, String>[] square(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="from", required=true) int fromSquare,
    		@RequestParam(value="to", required=true) int toSquare,
    		@RequestParam(value="name", required=true) String mover)
    {	
    	MatchManager currMatch = getMatch(mover);
    	currMatch.onGreenSquareClick(fromSquare, toSquare ,mover);
    	
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
   
    /*
	 * BLOKKA TAKKANA á framenda 
	 * Senda gameOver skilaboð X
	 * Senda matchOver skilaboð á rétta staði X
	 * Vona að framendinn slökkvi á inputs áður en ýtt er á input
	 */
	@RequestMapping("/playerLeaving")   		
    public HashMap<String, String>[] playerLeavingMatch(HttpSession session , HttpServletResponse response,
    		@RequestParam(value="name", required=true) String leavingPlayer)
    {
    	UMS.removeUser(leavingPlayer, UMS.MATCH);
    	
    	MatchManager currMatch = getMatch(leavingPlayer);
    	currMatch.handleEndOfCurrentGame(leavingPlayer, true);
    	currMatch.handleLeavingPlayerEndingMatch(leavingPlayer);
    	
		ongoingMatches.removeMatch(leavingPlayer);
	
		LobbyManager.deleteMatchEntry(currMatch.getId()+ "");
    	
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
