package project.model;

public class Move {
	public int to, from, team;
	public boolean killed;
	public Move(int from, int to, int team){
		this.to = to;
		this.from = from;
		this.team = team;
		killed = false;
	}
	public Move(int from, int to, int team, boolean killed){
		this.to = to;
		this.from = from;
		this.team = team;
		this.killed = killed;
	}
	
	public static Move getMoveIfForward(int from, int to, int team)
	{
		if(to >= 26) 
			return new Move(from, to, team);
		else if(from >= 26) 
			return null;
		else if(team == 0)
			return (to < from)? new Move(from, to, team): null ;
		else
			return (to > from)? new Move(from, to, team) : null ;
	}
	
	public void setKilledToTrue()
	{
		this.killed = true;
	}
	public int getTo()
	{
		return to;
	}
	public int getFrom()
	{
		return from;
	}
	public boolean getKilled()
	{
		return killed;
	}
	public int getTeam()
	{
		return team;
	}
}
