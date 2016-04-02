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
public class LoginController {
	
	private LoginController()
	{
	
	} 
	
	@RequestMapping("/logout")    
    public HashMap<String, String>[] logOut(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username) throws Exception
	{
    	UMS.removeUser(username, UMS.LOBBY);
    	UMS.removeUser(username, UMS.GENERAL);
    	LobbyManager.unSubscribe(username);
		return (HashMap<String, String>[])new HashMap[0];
    }
	
	@RequestMapping("/login")    
    public HashMap<String, String>[] logIn(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="pw", required=true) String password) throws Exception
	{
    	String allowedUsername = null;
		while(allowedUsername == null)
			allowedUsername = DataCenter.attemptLogIn(username, password);
			
    	if(allowedUsername.length() > 0){
    		UMS.addUser(username, UMS.LOBBY);
    		UMS.addUser(username, UMS.GENERAL);
    		UMS.storeLobbyMessage(username, MSG.welcomeUser(username));
        	LobbyManager.subscribeUser(username);
    		
        	return UMS.retrieveLobbyMessages(username);
    	}
    	else
    		return (HashMap<String,String>[]) new HashMap[]{MSG.explainMessage("There was no matching username found")};	
    }
    
    @RequestMapping("/signup")  
    public HashMap<String, String>[] signUp(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="pw", required=true) String password) throws Exception
    {
    	String allowedUsername = null;
    	while(allowedUsername == null)
			allowedUsername = DataCenter.attemptSignUp(username, password);
			
    	if(allowedUsername.length() > 0){
    		UMS.addUser(username, UMS.LOBBY);
    		UMS.addUser(username, UMS.GENERAL);
    		UMS.storeLobbyMessage(username, MSG.welcomeUser(username));
        	LobbyManager.subscribeUser(username);
    		
    		while(!DataCenter.createNewUserTrophyEntries(username));
    		while(!DataCenter.createNewUserVersusEntries(username));  
    		
    		return UMS.retrieveLobbyMessages(username);
    	}
    	else
    		return (HashMap<String,String>[]) new HashMap[]{MSG.explainMessage("Username is already in use")};
    }
}
