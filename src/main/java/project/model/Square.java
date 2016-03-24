package project.model;

public class Square {
	private static final int TEAM_WH = 0, TEAM_BL = 1, TEAM_NONE = 2;
	private int team;
	private int count;
	private Square(int team, int count)
	{
		this.team = team;
		this.count = count;
	}

	static Square createEmptySquare()
	{
		return new Square(TEAM_NONE, 0);
	}
	static Square createBlackSquare(int count)
	{
		//villutékk ef count < 1?
		return new Square(TEAM_BL, count);
	}
	static Square createWhiteSquare(int count)
	{
		return new Square(TEAM_WH, count);
	}
	static Square createRandomBlack(int max)
	{
		int count = (int)(Math.random()*(max+1));
		return (count > 0)? new Square(TEAM_NONE, 0) : new Square(TEAM_BL, count) ;
	}
	static Square createRandomWhite(int max)
	{
		int count = (int)(Math.random()*(max+1));
		return (count > 0)? new Square(TEAM_NONE, 0) : new Square(TEAM_WH, count) ;
	}

	void addPawn(int addedPawnTeam)
	{
		if(this.team != addedPawnTeam)
		{
			this.count = 1;
			this.team = addedPawnTeam;
		}
		else
			this.count++;
	}

	void removePawn()
	{
		this.count--;
		if(this.count == 0) this.team = TEAM_NONE;
	}

	int countBlackPawns()
	{
		return (this.team == TEAM_BL)? this.count : 0 ;
	}
	int countWhitePawns()
	{
		return (this.team == TEAM_WH)? this.count : 0 ;
	}
	int count(int team)
	{
		return (this.team == team)? this.count: 0;
	}
	int count()
	{
		return this.count;
	}
	int getTeam()
	{
		return this.team;
	}
}
