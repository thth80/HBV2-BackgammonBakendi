package project.model;

import java.util.ArrayList;
import java.util.HashMap;

public class GameManager {
	
	public static final int TEAM_WH = 0, TEAM_BL = 1, TEAM_NONE = 2, NONE_CHOSEN = -1, WHITE_END_ZONE = 26, BLACK_END_ZONE = 27;
	private Board board;
	private DicePair whiteDice, blackDice;
	private Move[] movesMade; 
	private String[] players;
	private int  turnOwner, cube, lastDoubler;
	private StringList observers;

	private GameManager(String p1, String p2) 
	{
		players = new String[]{p1, p2};
		board = Board.createNormalBoard();
		
		whiteDice = new DicePair();
		blackDice = new DicePair();
		movesMade = null;
		
		turnOwner = TEAM_NONE;
		lastDoubler = TEAM_NONE;
		cube = 1;
		observers = new StringList();
	}
	
	public void removePlayerAsSubscriber(String playerName)
	{
		if(playerName.equals(players[0])) 
			players = new String[]{players[1]};
		else							   
			players = new String[]{players[0]};
		
		turnOwner = TEAM_NONE;
	}
	
	public static GameManager resetGameBoard(GameManager game)  
	{
		GameManager clone = new GameManager(game.players[0], game.players[1]);
		clone.observers = game.observers;
		return clone;
	}
	
	public boolean isStillPlaying()
	{
		return turnOwner != TEAM_NONE;
	}

	public static GameManager regularGame(String u1, String u2)
	{
		return new GameManager(u1, u2);
	}
	
	public int makeOpeningThrow()
	{
		while(true)
		{
			whiteDice.throwSingle();
			blackDice.throwSingle();
			if(whiteDice.getStartingThrow() != blackDice.getStartingThrow())
				return (whiteDice.getStartingThrow() > blackDice.getStartingThrow())? TEAM_WH: TEAM_BL ;
		}
	}
	
	public void humanStartsGame(int team) 
	{
		turnOwner = team;
		if(team == TEAM_WH) whiteDice = new DicePair(blackDice.getStartingThrow(), whiteDice.getStartingThrow());
		else				blackDice = new DicePair(blackDice.getStartingThrow(), whiteDice.getStartingThrow());
		
		int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
		movesMade = new Move[moveCount];
		
		deliverHumanStartingMessage(getCurrentUserName(), turnOwner, getCurrentDice(), moveCount);
	}
	
	public void botStartsGame()
	{
		turnOwner = TEAM_BL;
		blackDice = new DicePair(blackDice.getStartingThrow(), whiteDice.getStartingThrow());
		
		DicePair startingDice = getCurrentDice();			
		Move[] movs = getBotMoves();
		executeBotMoves(movs);
		
		beginNewRound();
		
		deliverBotStartingMessage(getCurrentUserName(), TEAM_BL, startingDice, movs);
	}

	public void greenSquareClicked(int fromSquare, int toSquare) 
	{
		Move forwardMove = Move.getMoveIfForward(fromSquare, toSquare, turnOwner);
		if(forwardMove != null)
		{
			performForwardMovement(forwardMove, toSquare);
		}
		else
		{
			//VIrkar ekki fyrir end zones
			Move reverseMove = this.removeMove(fromSquare, toSquare);
			performReverseMovement(reverseMove, this.board);
			getCurrentDice().markAsUnused(calculateRealMovement(reverseMove.from, reverseMove.to));
		}
		
		UMS.storeInGameMessage(getCurrentUserName(), generateAllHighlighting());
		if(movesLeft() == 0 && !hasGameEnded()) UMS.storeInGameMessage(getCurrentUserName(), MSG.mayEndTurn());
	}
	
	private int calculateRealMovement(int from, int to)
	{
		if(to == WHITE_END_ZONE) to = 0;
		else if(to == BLACK_END_ZONE) to = 25;
		return Math.abs(to - from);
	}
	
	private void performForwardMovement(Move forwardMove, int toSquare)
	{
		if(board.containsTeam(toSquare, otherTeam(turnOwner))) forwardMove.setKilledToTrue();
		board.forwardMovement(forwardMove);
		storeMove(forwardMove);
		
		getCurrentDice().markAsUsed(calculateRealMovement(forwardMove.from, forwardMove.to));
	}
	
	private void performReverseMovement(Move reverseMove, Board boardToChange)
	{
		boardToChange.reverseMovement(reverseMove);
	}
	
	private Move removeMove(int fromSquare, int toSquare)
	{
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == fromSquare && movesMade[i].from == toSquare)
			{
				Move move = movesMade[i];
				movesMade[i] = null;
				return move;
			}
		return null;
	}
	

	
	private HashMap<String, String> generateAllHighlighting()
	{
		ArrayList<Integer> allWhites = new ArrayList();
		ArrayList<ArrayList<Integer>> greenList = new ArrayList();
		
		int[] unusedValues = getCurrentDice().getUnusedValues();
		
		for(int i = 0; i < 28; i++)
		{
			ArrayList<Integer> possible = getPossibleForwardDestinations(i, unusedValues);
			getPossibleReverseDestinations(i, possible);
			if(possible.size() > 0)
			{
				allWhites.add(i);
				greenList.add(possible);
			}
		}
		
		return MSG.allHighlights(allWhites, greenList);
	}
	
	private ArrayList<Integer> getPossibleForwardDestinations(int fromPos, int[] diceValsRemaining)
	{	
		ArrayList<Integer> possible = new ArrayList<Integer>();
		for(int i = 0; i < diceValsRemaining.length; i++)
		{
			int destination = (turnOwner == TEAM_WH)? fromPos - diceValsRemaining[i] :fromPos + diceValsRemaining[i] ;
			Move candidateMove = new Move(fromPos, destination, turnOwner);
			if(board.isPlayPossible(candidateMove))
			{
				if(destination >= 1 && destination <= 24) possible.add(destination);
				else if(destination > 24) 				  possible.add(BLACK_END_ZONE);
				else if(destination < 1) 				  possible.add(WHITE_END_ZONE);
			}
		}
		return possible;
	}
	
	private void getPossibleReverseDestinations(int fromPos, ArrayList<Integer> possible)
	{	
		Move[] backMoves = getPossibleBackMoves(fromPos);
		for(Move backMove: backMoves)
			possible.add(backMove.from);
	
	}
	
	public void setObservers(StringList obs)
	{
		this.observers = obs;
	}
	
	public void addObserver(String username)
	{
		this.observers.addStringEntry(username);
	}
	public void removeObserver(String username)
	{
		this.observers.removeString(username);
	}
	
	public void diceWasThrown() 
	{
		getCurrentDice().rollDice();
		int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
		movesMade = new Move[moveCount];
		deliverPostDiceThrowMessage(moveCount, getCurrentUserName());				 
	}
	
	public void userEndedTurnInBotGame()
	{		
		this.onlyIncludeValidMoves();
		getCurrentDice().markAllAsUnused();
		Move[] copiedMoves = this.movesMade;
		
		beginNewRound(); 

		Move[] botMoves = getBotMoves();
		DicePair botDice = getCurrentDice();
		executeBotMoves(botMoves);

		if(!hasGameEnded()) beginNewRound();
		
		deliverHumanAndBotMoves(copiedMoves, botMoves, botDice);  
	}
	
	public void userEndedTurnInHumanGame()
	{	
		getCurrentDice().markAllAsUnused();
		String humanActor = getCurrentUserName();
		Move[] humanMoves = this.movesMade;
		
		beginNewRound();
		
		deliverPostTurnMoveMessages(humanMoves, humanActor);	
	}
	
	public String getOtherUserName(String username)
	{
		return (username.equals(players[0]))? players[1]: players[0] ;
	}
	private int getTeamNumberFromName(String username)
	{
		return (username.equals(players[0]))? TEAM_WH : TEAM_BL ;
	}
		
	public HashMap<String, Integer> getEndGameResults(String winner)  
	{
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		int winnerNum = getTeamNumberFromName(winner);
		int loserNum = getTeamNumberFromName(getOtherUserName(winner));
		
		results.put("winner", winnerNum);
		results.put("loserSteps", board.countRemainingSteps(loserNum));
		results.put("winnerSteps", board.countRemainingSteps(winnerNum));
		results.put("loserPawns", board.countRemainingPawns(loserNum));
		results.put("winnerPawns", board.countRemainingPawns(winnerNum));
		results.put("cubeValue", this.cube);
		results.put("multiplier", board.getMultiplier(loserNum));

		return results;
	}

	public void doublingCubeWasFlipped()
	{
		deliverDoublingOfferMessage(this.getCurrentUserName(), this.getOtherUserName());
		this.lastDoubler = this.getTeamNumberFromName(getCurrentUserName());
	}

	public void doublingAccepted()
	{
		this.cube *= 2;
		getCurrentDice().rollDice();
		int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
		movesMade = new Move[moveCount];
		
		if(moveCount == 0)UMS.storeInGameMessage(getCurrentUserName(), MSG.mayEndTurn());
		deliverPostAcceptMessage(getOtherUserName(), getCurrentUserName());
	}
	
	public boolean isThisAPlayer(String username)
	{
		return username.equals(players[0]) || username.equals(players[1]);
	}

	boolean teamHasTurn(String username)
	{
		return username.equals(getCurrentUserName());
	}

	private Move[] getBotMoves()
	{
		getCurrentDice().rollDice();
		String botName = getCurrentUserName();
		if(botName.equals("Hard Bot"))     return CalculationCenter.hardBotMoves(board, getCurrentDice(), turnOwner);
		else if(botName.equals("Medium Bot")) return CalculationCenter.mediumBotMoves(board, getCurrentDice(), turnOwner);
		else                          return CalculationCenter.easyBotMoves(board, getCurrentDice(), turnOwner);
	}

	private void executeBotMoves(Move[] moves)
	{
		for(int i = 0; i < moves.length; i++)
			board.forwardMovement(moves[i]);
	}
	
	public void reverseMovesMadeThisTurn(Board copy)
	{
		for(int i = movesMade.length - 1; i >= 0; i--)
			if(movesMade[i] != null)
				performReverseMovement(movesMade[i], copy);
	}
	
	public Board getBoardCopy()
	{
		return Board.createCopy(this.board);
	}

	public void beginNewRound()
	{
		switchTurn();
		this.movesMade = new Move[0];
	}
	
	private int movesLeft() 
	{
		if(movesMade == null) return 1;

		int movesLeft = 0;
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] == null) movesLeft++;
		
		return movesLeft;
	}
	
	public void onlyIncludeValidMoves()
	{
		int counter = 0;
		for(Move m: this.movesMade)
			if(m != null) counter++;
		Move[] cleaned = new Move[counter];
		
		int pointer = 0;
		for(int i = 0; i < this.movesMade.length; i++)
			if(this.movesMade[i] != null)
				cleaned[pointer++] = this.movesMade[i];
		
		this.movesMade = cleaned;
	}

	private Move[] getPossibleBackMoves(int squarePos)
	{
		int counter = 0;
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == squarePos && board.containsTeam(squarePos, turnOwner))
				counter++;

		Move[] moves = new Move[counter];
		int index = 0;
		
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == squarePos && board.containsTeam(squarePos, turnOwner)) 
				moves[index++] = movesMade[i];

		return moves;
	}

	private void storeMove(Move move)
	{
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] == null)
			{
				movesMade[i] = move;  
				return; 
			}
	}
	
	
	//************ Delivery adferdir her ad nedan ***************************
	
	public void deliverEndMovesToOthers()
	{
		this.deliverPostTurnMoveMessages(this.movesMade, this.getCurrentUserName());
	}
	
	public void deliverEntireGameState(String observer, Board copyBoard)
	{
		int[] diceVals = new int[]{whiteDice.first(), whiteDice.second(), blackDice.first(), blackDice.second()};
		int[] counts = new int[28]; int[] teams = new int[28];
		for(int pos = 0; pos < counts.length; pos++)
		{
			Square sq = copyBoard.getSquare(pos);
			counts[pos] = sq.count();
			teams[pos] = sq.getTeam();
		}
		
		HashMap<String, String> boardDescript = MSG.buildBoard(counts, teams, diceVals, cube);
		UMS.storeInGameMessage(observer, boardDescript);
	}
	
	private void deliverHumanStartingMessage(String starter, int startTeam, DicePair startDice, int moveCount) 
	{	
		HashMap<String, String> diceMsg = MSG.diceThrow(startDice.first(), startDice.second(), startTeam, getCurrentUserName());
		UMS.storeInGameMessage(players, diceMsg);
		UMS.storeInGameMessage(observers.toArray(), diceMsg);
		
		UMS.storeInGameMessage(starter, generateAllHighlighting());
	}
	
	private void deliverBotStartingMessage(String human, int startTeam, DicePair startDice, Move[] moves)
	{
		//animateMessages
		HashMap<String, String> movs = prepareReturnMoves(moves);
		UMS.storeInGameMessage(human, movs);
		UMS.storeInGameMessage(observers.toArray(), movs);
		
		HashMap<String, String> diceVals = MSG.diceThrow(startDice.first(), startDice.second(), startTeam, "EASY_BOT");
		UMS.storeInGameMessage(observers.toArray(), diceVals);
		UMS.storeInGameMessage(human, diceVals);
	     
		UMS.storeInGameMessage(human, MSG.showButtons(true));
	}
	
	private void deliverPostTurnMoveMessages(Move[] moves, String actor)
	{
		String otherPlayer = this.getOtherUserName(actor);
		HashMap<String, String> movs = prepareReturnMoves(moves);
		UMS.storeInGameMessage(otherPlayer, movs);
		UMS.storeInGameMessage(observers.toArray(), movs);
		
		UMS.storeInGameMessage(otherPlayer, MSG.showButtons(canDouble(otherPlayer)));
	}
	
	private boolean canDouble(String player)
	{
		int teamNum = this.getTeamNumberFromName(player);
		return this.lastDoubler != teamNum;
	}
	
	private HashMap<String, String> prepareReturnMoves(Move[] moves) 
	{
		ArrayList<Integer> froms = new ArrayList(); ArrayList<Integer> tos = new ArrayList();
		ArrayList<Boolean> killMoves = new ArrayList();
		for(Move move: moves)
		{
			froms.add(move.from);
			tos.add(move.to);
			killMoves.add(move.killed);
			if(move.killed)
			{
				int deadZone = (move.team == TEAM_WH)? 0 : 25 ;
				froms.add(move.to);
				tos.add(deadZone);
				killMoves.add(false);
			}
		}
		return MSG.animateMessage(froms, tos, killMoves);
	}
	
	private void deliverPostAcceptMessage(String accepter, String doubler) 
	{
		HashMap<String, String> accepted = MSG.playerAccepted(accepter);  
		HashMap<String, String> diceMsg = 
				MSG.diceThrow(getCurrentDice().first(), getCurrentDice().second(), turnOwner, getCurrentUserName());
		
		UMS.storeInGameMessage(players, accepted);
		UMS.storeInGameMessage(players, diceMsg);
		UMS.storeInGameMessage(observers.toArray(), accepted);
		UMS.storeInGameMessage(observers.toArray(), diceMsg);
		
		UMS.storeInGameMessage(doubler, generateAllHighlighting());
	}
	
	private void deliverDoublingOfferMessage(String doubler, String decider)
	{
		HashMap<String, String> doubling = MSG.playerDoubled(doubler, decider ,this.cube); 
		UMS.storeInGameMessage(observers.toArray(), doubling);
		UMS.storeInGameMessage(players, doubling);
	}
	
	private void deliverHumanAndBotMoves(Move[] humanMoves, Move[] botMoves, DicePair botDice)
	{
		HashMap<String, String> humanMovs = prepareReturnMoves(humanMoves);
		UMS.storeInGameMessage(observers.toArray(), humanMovs);
		
		HashMap<String, String> botMovs = prepareReturnMoves(botMoves);
		UMS.storeInGameMessage(players[0], botMovs);
		UMS.storeInGameMessage(observers.toArray(), botMovs);
		
		UMS.storeInGameMessage(players[0], MSG.diceThrow(botDice.first(), botDice.second(), TEAM_BL, "EASY_BOT"));
		UMS.storeInGameMessage(observers.toArray(), MSG.diceThrow(botDice.first(), botDice.second(),TEAM_BL ,"EASY_BOT"));
		if(!hasGameEnded())
			UMS.storeInGameMessage(players[0], MSG.showButtons(canDouble(players[0])));
	}
	
	private void deliverPostDiceThrowMessage(int moveCount, String thrower)
	{
		HashMap<String, String> diceVals 
			= MSG.diceThrow(getCurrentDice().first(), getCurrentDice().second(),turnOwner, getCurrentUserName());
		if(moveCount == 0) UMS.storeInGameMessage(thrower, MSG.mayEndTurn());
	
		UMS.storeInGameMessage(players, diceVals);
		UMS.storeInGameMessage(observers.toArray(), diceVals);
		
		UMS.storeInGameMessage(thrower, generateAllHighlighting());
	}
	
	private boolean isTurnHuman()
	{
		String currUser = getCurrentUserName();
		return !(currUser.equals("Hard Bot") || currUser.equals("Medium Bot") || currUser.equals("Easy Bot"));
	}

	public String getCurrentUserName()
	{
		return (turnOwner == TEAM_WH)? players[0] : players[1] ;
	}
	public String getOtherUserName()
	{
		return (turnOwner == TEAM_WH)? players[1] : players[0] ;
	}
	
	public boolean hasGameEnded(){
		return board.whoWon() != TEAM_NONE;
	}
	private void switchTurn(){
		this.turnOwner = (this.turnOwner+1)%2;
	}
	
	private int otherTeam(int team){
		return (team+1)%2;
	}
	public StringList getObservers()
	{
		return this.observers;
	}
	
	private int[] toIntArray(ArrayList<Integer> greens)
	{
		int[] combined = new int[greens.size()];
		for(int i = 0; i < greens.size(); i++)
			combined[i] = greens.get(i);
			
		return combined;
	}
	
	
	
	public String[] getSubscribers()
	{
		String[] obs = observers.toArray();
		String[] all = new String[obs.length + players.length];
		int i = 0;
		while(i < obs.length)
			all[i] = obs[i++];
		all[i++] = players[0];
		all[i] = players[1];
		return all;
	}
	public String[] getPlayers()
	{
		return players;
	}
	
	private DicePair getCurrentDice(){
		return (turnOwner == TEAM_WH)? whiteDice : blackDice ;
	}
}