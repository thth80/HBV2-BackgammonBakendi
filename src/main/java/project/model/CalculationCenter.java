package project.model;

public class CalculationCenter {
	private static final int EASY = 0, MED = 1, HARD = 2, RAND = 3;
	private Board board;
	private DicePair dicePair;
	private int team;
	private int difficulty;
	private Move[] moveStack;
	private Move[] bestMovesYet;
	private double bestValueYet;

	private CalculationCenter(Board board, DicePair dicePair, int team, int diff )
	{
		this.board = board; 
		this.dicePair = dicePair;
		this.team = team;
		this.difficulty = diff;
		this.moveStack = new Move[4];
		this.bestMovesYet = new Move[4];
		this.bestValueYet = -99999999;
	}

	//ath að moveStack er alltaf 4 á lengd, með null í tómum sætum. Aftur á móti er lengd bestMovesYet háð fjölda hreyfinga hverju sinni
	private void updateBestMoves()
	{
		int movesCompleted = 1;
		for(int i = 3; i > 0; i--)
		{
			if(moveStack[i] != null)
			{
				movesCompleted = i + 1;
				break;
			}
		}
		bestMovesYet = null;
		bestMovesYet = new Move[movesCompleted];
		for(int i = 0; i < movesCompleted; i++) //hér gæti þurft að afrita Move hlutinn. Hluturinn breyist þó aldrei eftir að hann verður til
			bestMovesYet[i] = moveStack[i];		// tilvísun í hann er aðeins eytt. Þetta ætti að virka.
	}

	private void submitLegitPlay()
	{
		double currentBoardValue;
		if(this.difficulty == RAND) currentBoardValue = Math.random()*100;
		else if(this.difficulty == EASY) currentBoardValue = BoardEvaluator.easyEval(board);
		else if(this.difficulty == MED) currentBoardValue = BoardEvaluator.mediumEval(board);
		else  							currentBoardValue = BoardEvaluator.hardEval(board);

		if(currentBoardValue > bestValueYet)
		{
			bestValueYet = currentBoardValue;
			updateBestMoves();
		}
	}

	private void playSearch(int movesLeft)
	{
		if(movesLeft == 0)
		{
			submitLegitPlay();
			return;
		}

		for(int start = 0; start <= 25; start++)
		{
			int diceToUse = dicePair.getWorkingDice(movesLeft);
			int destination = (isTeamWhite())? start - diceToUse : start + diceToUse ;
			if(!board.isPlayPossible(new Move(start, destination, this.team))) continue;
			
			if(destination <= 0 || destination >= 25)
				destination = (isTeamWhite())? 26 : 27;

			Move lastMove = performPlay(start, destination, this.team);
			pushMoveOnStack(lastMove);

			playSearch(movesLeft-1);

			reverseLastPlay(lastMove);
			popMoveStack();
		}
	}

	private Move performPlay(int start, int destination, int team)
	{
		Move move = new Move(start, destination, team, false);
		int otherTeam = (this.team+1)%2;
		if(board.containsTeam(destination, otherTeam)) move.setKilledToTrue();

		board.forwardMovement(move);
		return move;
	}
	private void reverseLastPlay(Move lastMove)
	{
		board.reverseMovement(lastMove);
	}

	public static Move[] randomLegalMoves(Board board, DicePair dicePair, int team)
	{
		CalculationCenter calcs = new CalculationCenter(board, dicePair, team, CalculationCenter.RAND);
		return calcs.startPlaySearch();
	}
	public static Move[] easyBotMoves(Board board, DicePair dicePair, int team)
	{
		CalculationCenter calcs = new CalculationCenter(board, dicePair, team, CalculationCenter.EASY);
		return calcs.startPlaySearch();
	}
	public static Move[] mediumBotMoves(Board board, DicePair dicePair, int team)
	{
		CalculationCenter calcs = new CalculationCenter(board, dicePair, team, CalculationCenter.MED);
		return calcs.startPlaySearch();
	}
	public static Move[] hardBotMoves(Board board, DicePair dicePair, int team)
	{
		CalculationCenter calcs = new CalculationCenter(board, dicePair, team, CalculationCenter.HARD);
		return calcs.startPlaySearch();
	}

	private Move[] startPlaySearch()
	{
		if(dicePair.isPaired())
		{
			int maxMoves = 4;
			while(maxMoves > 0)
			{
				playSearch(maxMoves);
				if(legitPlayFound()) return bestMovesYet;
				else					maxMoves--;
			}
			return new Move[0];
		}
		else
		{
			int maxMoves = 2;
			playSearch(maxMoves);
			dicePair.flipDice();
			playSearch(maxMoves);
			if(legitPlayFound()) return bestMovesYet;
			else 				 maxMoves = 1;

			playSearch(maxMoves);
			dicePair.flipDice();
			playSearch(maxMoves);
			if(legitPlayFound()) return bestMovesYet;
			else   				 return new Move[0];
		}
	}

	private boolean isTeamWhite()
	{
		return this.team == 0;
	}

	private boolean legitPlayFound()
	{
		return this.bestValueYet > -999999;
	}

	private void popMoveStack()
	{
		for(int i = 3; i >= 0; i--){
			if(this.moveStack[i] != null){
				this.moveStack[i] = null; return;
			}
		}
	}
	private void pushMoveOnStack(Move latestMove)
	{
		for(int i = 0; i < 4; i++){
			if(this.moveStack[i] == null){
				this.moveStack[i] = latestMove; return;
			}
		}
	}
}
