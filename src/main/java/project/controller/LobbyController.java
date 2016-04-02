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
    		@RequestParam(value="addedTime", required=true) String addedTime
    		)  
    {
    	HashMap<String, String> waitData = MSG.waitingEntryNoID(waiter, points, addedTime);
    	LobbyManager.storeWaitListEntry(waitData);  
    	
    	return UMS.retrieveLobbyMessages(waiter);
    }
	
	@RequestMapping("/checkForJoin")   
    public HashMap<String, String>[] checkForJoin(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)  	
    {
    	return UMS.retrieveGeneralMessages(username);
    }
	
	@RequestMapping("/refreshLobby")   
    public HashMap<String, String>[] refreshLobby(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)  	
    {
    	return UMS.mergeArrays(UMS.retrieveLobbyMessages(username), UMS.retrieveGeneralMessages(username));
    }
	
	 @RequestMapping("/removeWaitEntry")   
	    public HashMap<String, String>[] removeWaitEntry(HttpSession session, HttpServletResponse response,
	    		@RequestParam(value="name", required=true) String username,
	    		@RequestParam(value="waitId", required=true) String id)  	
	    {
	    	LobbyManager.deleteSingleWaitEntry(username, id);
	    	return UMS.retrieveLobbyMessages(username);
	    }
	
	@RequestMapping("/startBotMatch")  
    public HashMap<String, String>[] joinOfflineMatch(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String human,
    		@RequestParam(value="points", required=true) String points,
    		@RequestParam(value="addedTime", required=true) String addedTime,
    		@RequestParam(value="diff", required=true) String diff)
    {
    	HashMap<String, String> matchData = MSG.newOngoingEntryNoID(human, points, addedTime, "EASY_BOT");
    	
    	int generatedMatchId = LobbyManager.createBotMatchEntry(matchData);
    	LobbyManager.deleteWaitEntries(human);
    	
    	UMS.addUser(human, UMS.MATCH);
    	MatchManager match = MatchManager.createNewBotMatch(matchData, generatedMatchId);
    	MatchController.addNewMatch(match, human , matchData.get("playerTwo"));
    	
    	//Hér er presentMatch sent á framenda
    	match.startMatch(matchData.get("playerOne"), Integer.parseInt(matchData.get("addedTime")));   
    	
    	return UMS.retrieveLobbyMessages(human);
    }
    
    @RequestMapping("/joinHumanMatch")  
    public HashMap<String, String>[] joinOnlineMatch(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="matchId", required=true) int waitId,
    		@RequestParam(value="joiner", required=true) String joiner)
    {	
    	HashMap<String, String> matchEntry = LobbyManager.createMatchEntryIfPossible(waitId, joiner);
    	if(matchEntry != null)
    	{
    		UMS.addUser(joiner, UMS.MATCH);
    		UMS.addUser(matchEntry.get("playerOne"), UMS.MATCH);
    		MatchManager match = MatchManager.createNewHumanMatch(matchEntry);
    		
    		LobbyManager.deleteWaitEntries(matchEntry.get("playerOne"));
    		LobbyManager.deleteWaitEntries(joiner);
    		LobbyManager.updateMatchEntries(matchEntry);
    		
    		MatchController.addNewMatch(match, matchEntry.get("playerOne"), joiner);
    		match.startMatch(matchEntry.get("playerOne"), Integer.parseInt(matchEntry.get("addedTime")));
    	}
    	
    	return UMS.retrieveLobbyMessages(joiner);
    }
    
    @RequestMapping("/goToTrophy")   
    public HashMap<String, String>[] goToTrophy(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	DataCenter.generateVersusStatsMessages(username);	
    	return UMS.retrieveLobbyMessages(username);
    }
    
    @RequestMapping("/goToStats")   
    public HashMap<String, String>[] goToStats(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	DataCenter.generateAllTrophyMessages(username);
    	return UMS.retrieveLobbyMessages(username);
    }
    
    @RequestMapping("/observeMatch")    
    public HashMap<String, String>[] observeMatch(HttpSession session, HttpServletResponse response ,
    		@RequestParam(value="name", required=true) String observer,
    		@RequestParam(value="waitId", required=true) int id)
    {
    	boolean couldObserve = MatchController.addObserver(id, observer);
    	if(!couldObserve)
    		UMS.storeLobbyMessage(observer, MSG.explainMessage("The match was no longer running"));
    	
    	return UMS.retrieveLobbyMessages(observer);
    }
    
    @RequestMapping("/submitLobbyChat")   
    public HashMap<String, String>[] submitChat(HttpSession session, HttpServletResponse response, 
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="chatEntry", required=true) String chat)
    {
    	LobbyManager.receiveChatEntry(username, chat);
    	return UMS.retrieveLobbyMessages(username);
    }
    
}
