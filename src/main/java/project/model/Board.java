package project.model;

public class Board {
	private static final int TEAM_WH = 0, TEAM_BL = 1, TEAM_NONE = 2;
	private Square[] squares;
	private Board()
	{
		this.squares = new Square[28];
		for(int i = 0; i < squares.length; i++)
		{
			if(i == 1) squares[i] = Square.createBlackSquare(2);
			else if(i == 6) squares[i] = Square.createWhiteSquare(5);
			else if(i == 8) squares[i] = Square.createWhiteSquare(3);
			else if(i == 12) squares[i] = Square.createBlackSquare(5);
			else if(i == 13) squares[i] = Square.createWhiteSquare(5);
			else if(i == 17) squares[i] = Square.createBlackSquare(3);
			else if(i == 19) squares[i] = Square.createBlackSquare(5);
			else if(i == 24) squares[i] = Square.createWhiteSquare(2);
			else			 squares[i] = Square.createEmptySquare();
		}
	}
	
	private Board(boolean empty)
	{
		this.squares = new Square[28];
		for(Square s: squares)
			s = Square.createEmptySquare();
	}
	public static Board createRandomBoard()
	{
		Board board = new Board();
		for(int i = 0; i < board.squares.length; i++)
			board.squares[i] = Square.createEmptySquare();

		int count = 0;
		int firstPos = 1;
		return new Board();
	}
	
	public static Board createCopy(Board original)
	{
		Board copy = new Board(true);
		for(int i = 0; i< copy.squares.length; i++)
		{
			Square toCopyFrom = original.squares[i];
			int team = toCopyFrom.getTeam();
			if(team == 0)
				copy.squares[i] = Square.createWhiteSquare(toCopyFrom.count());
			else if(team == 1)
				copy.squares[i] = Square.createBlackSquare(toCopyFrom.count());		
		}
		
		return copy;
	}

	public static Board createNormalBoard()
	{
		return new Board();
	}

	public boolean isPlayPossible(Move move)
	{
		if(!containsTeam(move.from, move.team)) return false; 
		else if(isMoveFromEndZone(move))		return false;
		else if(move.to >= 1 && move.to <= 24)  return landsInboundsCase(move.from, move.to, move.team);
		else if(!teamCanGoHome(move.team)) 		return false;

		int endGoal = (move.team == TEAM_WH)? 0 : 25 ;
		return move.to == endGoal || checkIfOutMost(move.from, move.team); 
	}
	
	private boolean isMoveFromEndZone(Move move)
	{
		return move.from == 26 || move.from == 27;
	}

	//muna að fara eins að hlutunum í REVERSE movement, að peðin séu sótt á END reitina
	//Move hlutir hafa aðgang að breytunum from, to, team og killed
	public void forwardMovement(Move move)
	{
		removePawn(move.from, move.team);
		if(containsTeam(move.to, otherTeam(move.team)))
			if(move.team == TEAM_WH) addPawn(0, TEAM_BL);
			else					 addPawn(25, TEAM_WH) ;
		
		addPawn(move.to, move.team); 
	}

	//Hér er gert ráð fyrir að notast sé við sama Move hlutinn og áður, því er move.from í raun áfangastaðurinn og move.to
	//er upphafsstaðurinn. Reyndar er spurning með að notast aðeins við eina Movement aðferð og láta notandann snúna hreyfingunni 
	//við. Það virkar þó ekki alveg þar sem Forward þarf ekki að spá í killed breytunni
	public void reverseMovement(Move move)
	{
		addPawn(move.from, move.team);
		removePawn(move.to, move.team);
		if(move.killed)
		{
			if(move.team == TEAM_WH){
				removePawn(0, TEAM_BL);
				addPawn(move.to, TEAM_BL);
			}
			else{
				removePawn(25, TEAM_WH);
				addPawn(move.to, TEAM_WH);
			}
		}
	}
	

	public int whoWon()
	{
		if(getWhiteEndZoneCount() == 15) return TEAM_WH;
		else if(getBlackEndZoneCount() == 15) return TEAM_BL;
		else								return TEAM_NONE;
	}

	public int countRemainingPawns(int team)
	{
		return (team == TEAM_WH)? 15 - getWhiteEndZoneCount() : 15 - getBlackEndZoneCount();
	}
	public int countRemainingSteps(int team)
	{
		int count = 0;
		for(int i = 0; i <= 25; i++)
			if(containsTeam(i, team))
			{
				int distance = (team == TEAM_WH)? i : 25 - i;
				count += (distance*getSquare(i).count(team));
			}
		
		return count;
	}
	public int getMultiplier(int losingTeam)
	{
		if(losingTeam == TEAM_WH && getWhiteEndZoneCount() > 0)      return 1;
		else if(losingTeam == TEAM_BL && getBlackEndZoneCount() > 0) return 1;
		else if(countStartingQuarter(losingTeam) == 0) 				 return 2;
		else														 return 3;
	}

	private void removePawn(int squarePos, int team)
	{
		getSquare(squarePos).removePawn();
	}

	private void addPawn(int squarePos, int team)
	{
		getSquare(squarePos).addPawn(team);
	}

	private int countStartingQuarter(int team)
	{
		int counter = 0;
		int start = (team == TEAM_WH)? 19 : 1;
		for(int i = start; i < start+6; i++)
			if(containsTeam(i, team))  counter += getSquare(i).count(team);
		return counter;
	}

	private boolean landsInboundsCase(int start, int destination, int team)
	{
		if(containsWall(destination, otherTeam(team)))                    return false;
		if(containsTeam(deadZone(team), team) && start != deadZone(team)) return false;
		else													    	  return true;
	}

	private boolean teamCanGoHome(int team)
	{
		int firstCheck = (team == TEAM_WH)? 7 : 0 ;
		int lastCheck = (team == TEAM_WH)? 25 : 18 ;
		for(int i = firstCheck; i <= lastCheck; i++)
			if(containsTeam(i, team))
				return false;

		return true;
	}

	private boolean checkIfOutMost(int from, int team)
	{
		int firstCheck = (team == TEAM_WH)? from + 1: 19 ;
		int lastCheck = (team == TEAM_WH)? 6 : from-1 ;
		
		for(int i = firstCheck; i <= lastCheck; i++)
			if(containsTeam(i, team)) 
				return false;

		return true;
	}

	public Square getSquare(int pos) 
	{
		return this.squares[pos];
	}
	
	public Square[] getSquares()
	{
		return this.squares;
	}
	
	private int getWhiteEndZoneCount()
	{
		return getSquare(26).count(TEAM_WH);
	}
	private int getBlackEndZoneCount()
	{
		return getSquare(27).count(TEAM_BL);
	}
	private void addToWhiteEndZone(){
		getSquare(26).addPawn(TEAM_WH);
	}
	private void addToBlackEndZone(){
		getSquare(27).addPawn(TEAM_BL);
	}
	private void removeFromWhiteEndZone()
	{
		getSquare(26).removePawn();
	}
	private void removeFromBlackEndZone()
	{
		getSquare(27).removePawn();
	}
	public boolean containsTeam(int pos, int team)
	{
		return getSquare(pos).count(team) > 0;
	}

	public boolean containsWall(int pos, int team)
	{
		return getSquare(pos).count(team) > 1;
	}

	private int deadZone(int team)
	{
		return (team == TEAM_BL)? 0 : 25;
	}
	private int otherTeam(int team)
	{
		return (team+1)%2;
	}
}
