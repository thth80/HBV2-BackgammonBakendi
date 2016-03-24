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
import project.model.Move;
import project.model.LobbyManager;
import project.model.MatchManager;
import project.model.UMS;
import project.model.PostBoxes;

@RestController
public class LobbyController {
	
	private LobbyController()
	{
		
	}
	
	@RequestMapping("/addWaitEntry")   
    public HashMap<String, String>[] addWaitEntry(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String waiter,
    		@RequestParam(value="points", required=true) String points,
    		@RequestParam(value="clock", required=true) String clock,
    		@RequestParam(value="addedTime", required=true) String addedTime
    		)  
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	HashMap<String, String> waitData = MSG.waitingEntryNoID(waiter, points, clock, addedTime);
    	LobbyManager.storeWaitListEntry(waitData);  
    	
    	return UMS.retrieveLobbyMessages(waiter);
    }
	
	 @RequestMapping("/removeWaitEntry")   
	    public HashMap<String, String>[] removeWaitEntry(HttpSession session, HttpServletResponse response,
	    		@RequestParam(value="name", required=true) String username,
	    		@RequestParam(value="id", required=true) String id)  	
	    {
	    	response.addHeader("Access-Control-Allow-Origin", "*");
	    	LobbyManager.deleteSingleWaitEntry(username, id);
	    	return UMS.retrieveLobbyMessages(username);
	    }
	
	@RequestMapping("/startBotMatch")  
    public HashMap<String, String>[] joinOfflineMatch(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String human,
    		@RequestParam(value="points", required=true) String points,
    		@RequestParam(value="clock", required=true) String clock,
    		@RequestParam(value="timeAdded", required=true) String addedTime,
    		@RequestParam(value="diff", required=true) String diff)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	HashMap<String, String> matchData = MSG.newOngoingEntryNoID(human, points, clock, addedTime, "EASY_BOT");
    	
    	int generatedMatchId = LobbyManager.createBotMatchEntry(matchData);
    	LobbyManager.deleteWaitEntries(human);
    	MatchManager match = MatchManager.createNewBotMatch(matchData, generatedMatchId);
    	
    	MatchController.addNewMatch(match, human , matchData.get("playerTwo"));
    	match.startMatch();   
    	
    	return UMS.retrieveLobbyMessages(human);
    }
    
    @RequestMapping("/joinHumanMatch")  
    public HashMap<String, String>[] joinOnlineMatch(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="id", required=true) int waitId,
    		@RequestParam(value="name", required=true) String joiner)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	HashMap<String, String> matchEntry = LobbyManager.createMatchEntryIfPossible(waitId, joiner);
    	if(matchEntry != null)
    	{
    		MatchManager match = MatchManager.createNewHumanMatch(matchEntry);
    		LobbyManager.deleteWaitEntries(matchEntry.get("playerOne"));
    		LobbyManager.deleteWaitEntries(joiner);
    		
    		MatchController.addNewMatch(match, matchEntry.get("playerOne"), joiner);
    		match.startMatch();
    		//STARTA MEÐ INGAMECONTROLLER?
    	}
    	
    	return UMS.retrieveLobbyMessages(joiner);
    }
    
    @RequestMapping("/leaveProgramFromLobby")   
    public HashMap<String, String>[] leaveProgram(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    		  		
    	LobbyManager.playerExitingApplication(username);
    	
    	return UMS.retrieveLobbyMessages(username);
    }
    
    //ATHATHATH : Ekki má senda OBSERVER ástand leiksins eins og það er eftir 1+ action
    
    @RequestMapping("/observeMatch")    
    public HashMap<String, String>[] observeMatch(HttpSession session, HttpServletResponse response ,
    		@RequestParam(value="name", required=true) String observer,
    		@RequestParam(value="id", required=true) int id)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	//Ef true þá gerist mikið í addObserver
    	boolean couldObserve = MatchController.addObserver(id, observer);
    	if(!couldObserve)
    		UMS.storeLobbyMessage(observer, MSG.explainMessage("The match was no longer running"));
    	
    	return UMS.retrieveLobbyMessages(observer);
    }
    
    @RequestMapping("/submitLobbyChat")   
    public HashMap<String, String>[] submitChat(HttpSession session, HttpServletResponse response, 
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="chat", required=true) String chat)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	LobbyManager.receiveChatEntry(username, chat);
    	return UMS.retrieveInGameMessages(username);
    }
    
}
