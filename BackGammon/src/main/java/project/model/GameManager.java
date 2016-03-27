package project.model;

import java.util.ArrayList;
import java.util.HashMap;

public class GameManager {
	
	public static final int TEAM_WH = 0, TEAM_BL = 1, TEAM_NONE = 2, NONE_CHOSEN = -1, WHITE_END_ZONE = 26, BLACK_END_ZONE = 27;
	private Board board;
	private DicePair whiteDice, blackDice;
	private Move[] movesMade; 
	private String[] players;
	private int pivotSquare, turnOwner, cube, lastDoubler;
	private StringList observers;

	private GameManager(String p1, String p2) 
	{
		players = new String[]{p1, p2};
		board = Board.createNormalBoard();
		
		whiteDice = new DicePair();
		blackDice = new DicePair();
		movesMade = null;
		pivotSquare = NONE_CHOSEN;
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
	
	public static GameManager cloneGame(GameManager game)  
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

	public void greenSquareClicked(int toSquare) 
	{
		Move forwardMove = Move.getMoveIfForward(pivotSquare, toSquare, turnOwner);
		if(forwardMove != null)
		{
			performForwardMovement(forwardMove, toSquare);
			deliverInTurnMoveMessage(prepareInTurnForwardMove(forwardMove));
		}
		else
		{
			Move reverseMove = this.removeReverseMove(toSquare);
			performReverseMovement(reverseMove, this.board);
			getCurrentDice().markAsUnused(calculateRealMovement(reverseMove.from, reverseMove.to));
			deliverInTurnMoveMessage(prepareInTurnReverseMove(reverseMove));
		}
		
		//Þarf að senda nýjan skammt af from/white reitum til framenda
		pivotSquare = NONE_CHOSEN;
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
	
	private int[] generateWhiteSquares()
	{
		ArrayList<Integer> allWhites = new ArrayList();
		int[] unusedValues = getCurrentDice().getUnusedValues();
		for(int i = 0; i < 28; i++)
		{
			boolean possFor = getPossibleForwardDestinations(i, unusedValues, allWhites);
			boolean possBack = getPossibleReverseDestinations(i, allWhites);
			if(possFor || possBack)
				allWhites.add(i);
		}
		
		return toIntArray(allWhites);
	}
	
	public void whiteSquareClicked(int fromSquare)
	{
		pivotSquare = fromSquare;
		ArrayList<Integer> allPossible = new ArrayList();
		getPossibleForwardDestinations(pivotSquare, getCurrentDice().getUnusedValues(), allPossible);
		getPossibleReverseDestinations(pivotSquare, allPossible);
		
		int[] toGreenLight = toIntArray(allPossible);
		UMS.storeInGameMessage(getCurrentUserName(), MSG.greenLight(toGreenLight));
	}
	
	private boolean getPossibleForwardDestinations(int fromPos, int[] diceValsRemaining, ArrayList<Integer> possible)
	{	
		boolean foundPossible = false;
		for(int i = 0; i < diceValsRemaining.length; i++)
		{
			int destination = (turnOwner == TEAM_WH)? fromPos - diceValsRemaining[i] :fromPos + diceValsRemaining[i] ;
			Move candidateMove = new Move(fromPos, destination, turnOwner);
			if(board.isPlayPossible(candidateMove))
			{
				if(destination >= 1 && destination <= 24) possible.add(destination);
				else if(destination > 24) 				  possible.add(BLACK_END_ZONE);
				else if(destination < 1) 				  possible.add(WHITE_END_ZONE);
				foundPossible = true;
			}
		}
		return foundPossible;
	}
	
	private boolean getPossibleReverseDestinations(int fromPos, ArrayList<Integer> possible)
	{
		//þessi move geta falið í sér ólögleg(<0 eða >25) gildi, þarf að leiðrétta?
		boolean foundPossible = false;
		Move[] backMoves = getPossibleBackMoves(fromPos);
		for(Move backMove: backMoves)
		{
			possible.add(backMove.from);
			foundPossible = true;
		}
		return foundPossible;
	}
	
	//Hér þarf að WHITE LIGHT-a
	public void pivotSquareClicked()
	{
		pivotSquare = NONE_CHOSEN;
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
		results.put("loserPawns", getTeamNumberFromName(getOtherUserName(winner)));
		results.put("winnerPawns", getTeamNumberFromName(getOtherUserName(winner)));
		results.put("cubeValue", this.cube);
		results.put("multiplier", board.getMultiplier(loserNum));

		return results;
	}

	public void doublingCubeWasFlipped()
	{
		deliverDoublingOfferMessage(this.getCurrentUserName(), this.getOtherUserName());
	}

	public void doublingAccepted()
	{
		this.cube *= 2;
		getCurrentDice().rollDice();
		int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
		movesMade = new Move[moveCount];
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
		for(Move move: this.movesMade)
			if(move != null)
				performReverseMovement(move, copy);
	}
	
	public Board getBoardCopy()
	{
		return Board.createCopy(this.board);
	}

	public void beginNewRound()
	{
		switchTurn();
		this.movesMade = null;
		pivotSquare = NONE_CHOSEN;
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
			if(movesMade[i] != null && movesMade[i].to == squarePos) counter++;

		Move[] moves = new Move[counter];
		int index = 0;
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == squarePos) moves[index++] = movesMade[i];

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
	
	private HashMap<String, String> prepareInTurnReverseMove(Move move) 
	{													   	
		ArrayList<Integer> froms = new ArrayList();
		ArrayList<Integer> tos = new ArrayList();
		ArrayList<Boolean> killMoves = new ArrayList();
		
		froms.add(move.to);
		tos.add(move.from);
		killMoves.add(false);
		if(!move.killed) 
			return MSG.inTurnMove((Integer[])froms.toArray(),(Integer[]) tos.toArray(),(Boolean[]) killMoves.toArray());
		
		int deadzone = (move.team == TEAM_WH)? 0 : 25 ;
		froms.add(deadzone);
		tos.add(move.to);
		killMoves.add(false); 
		return MSG.inTurnMove((Integer[])froms.toArray(),(Integer[]) tos.toArray(),(Boolean[]) killMoves.toArray());
	}
	
	private HashMap<String, String> prepareInTurnForwardMove(Move move) 
	{												
		ArrayList<Integer> froms = new ArrayList();
		ArrayList<Integer> tos = new ArrayList();
		ArrayList<Boolean> killMoves = new ArrayList();
		
		froms.add(move.from);
		tos.add(move.to);
		killMoves.add(move.killed);
		
		if(!move.killed) 
			return MSG.inTurnMove((Integer[])froms.toArray(),(Integer[]) tos.toArray(),(Boolean[]) killMoves.toArray());
		
		int deadZone = (move.team == TEAM_WH)? 0 : 25 ;
		froms.add(move.to);
		tos.add(deadZone);
		killMoves.add(false); 
		return MSG.inTurnMove((Integer[])froms.toArray(),(Integer[]) tos.toArray(),(Boolean[]) killMoves.toArray());
	}

	private int calculateRealMovement(int from, int to)
	{
		if(to == WHITE_END_ZONE) to = 0;
		else if(to == BLACK_END_ZONE) to = 25;
		return Math.abs(to - from);
	}
	
	//************ Delivery adferdir her ad nedan ***************************
	
	private void deliverInTurnMoveMessage(HashMap<String, String> moves)
	{
		if(movesLeft() == 0) UMS.storeInGameMessage(getCurrentUserName(), MSG.mayEndTurn());
		UMS.storeInGameMessage(getCurrentUserName(), moves);
	}
	
	public void deliverEndMovesToOthers()
	{
		this.deliverPostTurnMoveMessages(this.movesMade, this.getCurrentUserName());
	}
	
	public void deliverEntireGameState(String observer, Board copyBoard)
	{
		int[] diceVals = new int[]{whiteDice.first(), whiteDice.second(), blackDice.first(), blackDice.second()};
		int[] counts = new int[28]; int[] teams = new int[28];
		for(int pos = 0; pos < 28; pos++)
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
		
		int[] whiteSquares = generateWhiteSquares();
		UMS.storeInGameMessage(starter, MSG.whiteLight(whiteSquares));
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
		return MSG.animateMessage((Integer[])froms.toArray(), (Integer[])tos.toArray(), (Boolean[])killMoves.toArray());
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
		
		UMS.storeInGameMessage(doubler, MSG.whiteLight(generateWhiteSquares()));
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
		
		UMS.storeInGameMessage(players[0], MSG.showButtons(canDouble(players[0])));
	}
	
	private void deliverPostDiceThrowMessage(int moveCount, String thrower)
	{
		HashMap<String, String> diceVals 
			= MSG.diceThrow(getCurrentDice().first(), getCurrentDice().second(),turnOwner, getCurrentUserName());
		if(moveCount == 0) UMS.storeInGameMessage(thrower, MSG.mayEndTurn());
	
		UMS.storeInGameMessage(players, diceVals);
		UMS.storeInGameMessage(observers.toArray(), diceVals);
		
		UMS.storeInGameMessage(thrower, MSG.whiteLight(generateWhiteSquares()));
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
	
	private Move removeReverseMove(int toSquare)
	{
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == pivotSquare && movesMade[i].from == toSquare)
			{
				Move move = movesMade[i];
				movesMade[i] = null;
				return move;
			}
		return null;
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