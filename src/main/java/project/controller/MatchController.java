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
	
	@RequestMapping("/reject")   
    public HashMap<String, String>[] reject(HttpSession session,
    		@RequestParam(value="name", required=true) String rejecter)
    {
		MatchManager currMatch = getMatch(rejecter);
    	currMatch.handleEndOfCurrentGame(rejecter, true);
    	currMatch.initNewGame();
    	if(currMatch.hasMatchEnded())
    	{
    		currMatch.finishMatchNormally(rejecter);
    		currMatch.closeMatch();
    	}
    	else
    		currMatch.startNewGame();
    	
    	return UMS.retrieveInGameMessages(rejecter);
    }
	
	@RequestMapping("/outOfTime")   
    public HashMap<String, String>[] outOfTime(HttpSession session,
    		@RequestParam(value="name", required=true) String player)
    {
		MatchManager currMatch = getMatch(player);
		UMS.storeInGameMessage(player, MSG.addedTime(currMatch.getTimeAddition()));
		//Framendi þarf að skilja að þessi skilaboð slökkva á tökkum/white/green reitum
		
		currMatch.deliverMadeMoves();
    	currMatch.beginNewRound();
    	
    	return UMS.retrieveInGameMessages(player);
    }
	
	@RequestMapping("/diceThrow")   
    public HashMap<String, String>[] throwDice(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	(getMatch(username)).onDiceThrow();
    	return UMS.retrieveInGameMessages(username);
    }
    

    @RequestMapping("/greenSquare") 
    public HashMap<String, String>[] square(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="pos", required=true) int pos,
    		@RequestParam(value="name", required=true) String mover)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	MatchManager currMatch = getMatch(mover);
    	currMatch.onGreenSquareClick(pos);
    	if(currMatch.hasGameEnded())
    	{
    		String loser = currMatch.getOtherPlayer(mover);
    		currMatch.deliverMadeMoves();
    		currMatch.handleEndOfCurrentGame(loser, false);
    		currMatch.initNewGame();
    		
    		if(currMatch.hasMatchEnded())
    		{
    			currMatch.finishMatchNormally(loser);
    			currMatch.closeMatch();
    		}
    		else
    			currMatch.startNewGame();
    	}
    	return UMS.retrieveInGameMessages(mover);
    }
	
	@RequestMapping("/playerLeavingMatch")   		
    public HashMap<String, String>[] leaveMatch(HttpSession session , HttpServletResponse response,
    		@RequestParam(value="name", required=true) String leavingPlayer)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	//Það þarf að tékka hvort match sé þegar lokið
    	
    	MatchManager currMatch = getMatch(leavingPlayer);
    	currMatch.handleEndOfCurrentGame(leavingPlayer, true);
    	currMatch.handleLeavingPlayerEndingMatch(leavingPlayer);
    	currMatch.closeMatch();
    	//currMatch.gameState.removePlayerAsSubscriber(quitter);
    	
		ongoingMatches.removeMatch(leavingPlayer);
		int matchId = currMatch.getId();
		
		if(getMatch(matchId) == null)
			LobbyManager.deleteMatchEntry(matchId);
    	
    	return UMS.retrieveInGameMessages(leavingPlayer);
    }
	
	@RequestMapping("/accept")   
    public String accept(HttpSession session,
    		@RequestParam(value="name", required=true) String accepter)
    {
    	(getMatch(accepter)).onDoublingAccept();
    	return "derp";
    }
	
    @RequestMapping("/endTurn")   
    public HashMap<String, String>[] doneMoving(HttpSession session ,HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	MatchManager currMatch = getMatch(username);
    	currMatch.onTurnFinish();;
    	
    	if(currMatch.hasGameEnded())
    	{
    		currMatch.handleEndOfCurrentGame(username, false);
    		currMatch.initNewGame();
    		if(currMatch.hasMatchEnded())
    		{
    			currMatch.finishMatchNormally(username);
    			currMatch.closeMatch();
    		}
    		else
    			currMatch.startNewGame();
    	}
    	//höfum samband við OLM ef það skal breyta status á leik
    		
    	return UMS.retrieveInGameMessages(username);
    }
    
    @RequestMapping("/cube")   
    public String flipCube(HttpSession session,
    		@RequestParam(value="name", required=true) String doubler)
    {
    	(getMatch(doubler)).onDiceThrow(); //handleCubeFlip
    	return "derp";
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
