package project.model;

public class DicePair {
	private int d1, d2;
	private boolean[] usageStatuses;

	public DicePair(int diceOne, int diceTwo)
	{
		this.d1 = diceOne;
		this.d2 = diceTwo;
		this.usageStatuses = new boolean[4];
		for(int i = 0; i < 4; i++){ 
			this.usageStatuses[i] = false;		}
	}
	public DicePair()
	{
		this.d1 = 1;
		this.d2 = 1;
		this.usageStatuses = new boolean[4];
		for(int i = 0; i < 4 ; i++) usageStatuses[i] = false;
	}
	
	public int first()
	{
		return this.d1;
	}
	public int second()
	{
		return this.d2;
	}

	public void throwSingle()
	{
		this.d1 = 1 + (int)(Math.random()*6);
	}
	public int getStartingThrow()
	{
		return d1;
	}

	public void markAsUsed(int value) 
	{
		if(isPaired())
		{
			for(int i = 0; i < usageStatuses.length; i++){
				if(!usageStatuses[i]){ usageStatuses[i] = true; break; }
			}
		}
		else if(d1 == value) 	 usageStatuses[0] = true;
		else if(d2 == value) usageStatuses[1] = true;
		else if(d1 > value)  usageStatuses[0] = true;
		else if(d2 > value)	 usageStatuses[1] = true;
	}
	
	public void markAllAsUnused()
	{
		for(int i = 0; i < usageStatuses.length; i++)
			usageStatuses[i] = false;
	}

	public void markAsUnused(int value)
	{
		if(isPaired())
		{
			for(int i = 0; i < usageStatuses.length; i++)
			{
				if(usageStatuses[i])
				{ 
					usageStatuses[i] = false; 
					break; 
				}
			}
		}
		else if(d1 == value) 	usageStatuses[0] = false;
		else if(d2 == value)usageStatuses[1] = false;
		else if(d1 > value) usageStatuses[0] = false;
		else if(d2 > value) usageStatuses[1] = false;
	}

	public int[] getUnusedValues()
	{
		if(isPaired())
		{
			for(int i = 0; i < usageStatuses.length; i++)
				if(!usageStatuses[i])
					return new int[]{this.d1};

			return new int[0];
		}

		int counter = 0;
		if(!usageStatuses[0]) counter++;
		if(!usageStatuses[1]) counter++;
		int[] vals = new int[counter];
		counter = 0;
		if(!usageStatuses[0]) vals[counter++] = d1;
		if(!usageStatuses[1]) vals[counter] = d2;  

		return vals;
	}

	public void rollDice()
	{
		this.d1 = 1 + (int)(Math.random()*6);
		this.d2 = 1 + (int)(Math.random()*6);
		/*if(Math.random()> 0.4)
			this.d1 = this.d2 = 6;
		else if(Math.random() > 0.7)
			this.d1 = this.d2 = 5;
		else
			this.d1 = this.d2 = 4;	*/
	}

	public boolean isPaired()
	{
		return d1 == d2;
	}

	public void flipDice()
	{
		int temp = this.d1;
		this.d1 = this.d2;
		this.d2 = temp;
	}

	public int getWorkingDice(int movesLeft) //frá 1-4 moves
	{
		if(isPaired()) return d1;
		else if(movesLeft == 2) return d2;
		else if(movesLeft == 1) return d1;
		else					return 42;
	}
}
