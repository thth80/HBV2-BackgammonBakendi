package project.model;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
 
public class DataCenter
{
	public class TrophyRow
	{
		private int id;
		private String sql;
		private int target;
		
		public TrophyRow(int id, int target, String query) 
		{																
			this.target = target;
			this.id = id;
			this.sql = query;
		}
		
	}
	
	private static DataCenter instance = new DataCenter();
	private static TrophyRow[] trophies = instance.createTheTrophyList();
	
	private static final String forwardStepCounter =
			"SELECT SUM(steps) "+
			"FROM (SELECT SUM(167-WinSteps) AS steps FROM GameResults WHERE winner='?' "
			+" UNION "+
			"SELECT SUM(167-lossSteps) AS steps FROM GameResults WHERE loser='?' )";
	
	private static final String tripleCounter = 
			 "SELECT COUNT(*) FROM GameResults WHERE winner='?' AND multiplier=3";
	private static final String doubleCounter = 
			 "SELECT COUNT(*) FROM GameResults WHERE winner='?' AND multiplier=3";
	
	private static final String ifWinrateSufficientThenPointCount = 
			"SELECT CASE WHEN CAST(winp.count as FLOAT)/(winp.count+lossp.count) > 0.5 THEN winp.count+lossp.count ELSE	0 END "+
			"FROM (SELECT SUM(points) AS count FROM GameResults WHERE winner = '?' AND loser = ''Hard Bot'') as winp, "+
			"(SELECT SUM(points) AS count FROM GameResults WHERE loser = '?' AND winner = ''Hard Bot'') as lossp";
			  
	private static final String countCenturies =
			"SELECT COUNT(*) FROM GameResults WHERE winner = '?' AND winSteps = 0 and lossSteps >= 100";
	
	private static final String countPawnsThatMadeIt = 
			"SELECT SUM(madeit) "+
			"FROM (SELECT SUM(15-winPawns) AS madeit FROM GameResults WHERE winner = '?' "+
			      "UNION  "+
			      "SELECT SUM(15-lossPawns) AS madeit FROM GameResults WHERE loser = '?')";
			  
	private static final String almostThereCounter = 
			"SELECT COUNT(*) FROM GameResults WHERE loser = '?' AND lossSteps = 1 ";
	
	private static final String counterOfCubeValues = 
			"SELECT COUNT(*) FROM GameResults WHERE (winner = '?' OR loser = '?') AND cube >= 16 ";
			  
	//Eitthvað vafasamt hérna
	private static final String count10PointWins = 
			"SELECT COUNT(*) FROM GameResults WHERE winner = '?' AND points >= 10";
	
	private static final String countGameBeatenPlayers =
			"SELECT count(distinct loser) FROM GameResults WHERE winner = '?'";
	
	private static final String countMaxCleanSheetAgainstSpecific =
			"SELECT CASE WHEN lossp.count > 0 OR lossp.count is NULL or winp.count is NULL THEN 0 ELSE winp.count END "+		
		        "FROM (SELECT SUM(points) AS count FROM GameResults WHERE winner = '?' AND loser = '?') as winp, "+
		        "(SELECT SUM(points) AS count FROM GameResults  WHERE loser = '?' AND winner = '?') as lossp ";
	
	private static final String accumulatedPointCount = 
			"SELECT sum(points) FROM GameResults WHERE winner = '?'";
	
	
	private DataCenter()
	{
		
	}
	//Byrjum með SELECT streng í row.sql.
	// UPDATE Trophies SET current = ( row.sql ) WHERE username = 'row.username' AND name = 'row.name '
	
	private static String createUpdateStatement(TrophyRow row, String sqlSelect, String username)
	{
		return "UPDATE Trophies SET current = ( "+sqlSelect+" ) WHERE username = ''"+username+"'' AND name = ''"+row.id+"'' ";
	}
	
    private static String replaceQuestionMarks(String trophyQuery, String username)
    {
    	return trophyQuery.replace("'?'", "''"+username+"''");
    }
    
    //INSERT INTO Trophies VALUES('username', 'name', 'descript', 0, target, 0, 'sql', 'imageUrl', isAccum) 
    private static String generateInsertForTrophyRow(TrophyRow row, String username)
    {
    	return  "INSERT INTO Trophies(username, id, current, completed ,target ) "
    			+ "VALUES('"+username+"',"+ row.id +", 0, 0,  "+row.target +")";
    }
    
    private static String generateSQLForTrophyRow(TrophyRow row, String username)
    {
    	String selectSql = replaceQuestionMarks(row.sql, username);
    	String updateSql = createUpdateStatement(row, selectSql, username);
    	return "UPDATE Trophies SET sql = '"+updateSql+"' WHERE username = '"+username+"' AND id = '"+row.id+"' ";
    }
	
	private TrophyRow[] createTheTrophyList()
	{
		TrophyRow[] trophies = new TrophyRow[20];
		int pointer = 0;
		
		trophies[pointer++] = new TrophyRow(0 ,500, forwardStepCounter);
		
		trophies[pointer++] = new TrophyRow(1,1000, forwardStepCounter);
		
		trophies[pointer++] = new TrophyRow(2 ,10, tripleCounter);
	
		trophies[pointer++] = new TrophyRow(3, 10, doubleCounter);
		
		trophies[pointer++] = new TrophyRow(4 ,10, ifWinrateSufficientThenPointCount);
		
		trophies[pointer++] = new TrophyRow(5 ,1, countCenturies);
		
		trophies[pointer++] = new TrophyRow(6, 5, countCenturies);
		
		trophies[pointer++] = new TrophyRow(7, 1, counterOfCubeValues);
		
		trophies[pointer++] = new TrophyRow(8,1, count10PointWins);
		
		trophies[pointer++] = new TrophyRow(9, 100, countPawnsThatMadeIt);
		
		trophies[pointer++] = new TrophyRow(10 ,500, countPawnsThatMadeIt);
		
		trophies[pointer++] = new TrophyRow(11 ,1, almostThereCounter);
		
		trophies[pointer++] = new TrophyRow(12 ,5, almostThereCounter);
		
		trophies[pointer++] = new TrophyRow(13 ,5, countGameBeatenPlayers);
		
		trophies[pointer++] = new TrophyRow(14 ,15, countGameBeatenPlayers);
		
		trophies[pointer++] = new TrophyRow(15,10, countMaxCleanSheetAgainstSpecific );
		
		trophies[pointer++] = new TrophyRow(16 ,20, countMaxCleanSheetAgainstSpecific);
		
		trophies[pointer++] = new TrophyRow(17 ,1, accumulatedPointCount);
		
		trophies[pointer++] = new TrophyRow(18 ,50, accumulatedPointCount);
		
		trophies[pointer++] = new TrophyRow(19 ,500, accumulatedPointCount);
		
		//NOTE TO SELF: Int deiling í SQL er eins og í java
		return trophies;
	}
	
    public static String attemptLogIn(String username, String password) throws Exception
    {
        Statement stmt = null; ResultSet rs = null;
        try {
            stmt = makeConnection();
            rs = stmt.executeQuery("SELECT password FROM Users WHERE username='"+username+"'"); 
            
            if(rs.next() && password.equals(rs.getString("password")))
            {
            	closeAll(stmt.getConnection(), stmt, rs);
            	return username;
            }
            else{
            	closeAll(stmt.getConnection(), stmt, rs);
            	return "";
            }
        }catch(Exception ignore){  
        	throw ignore;  }
     }
   
    public static String attemptSignUp(String username, String password) 
    {
        Statement stmt = null; ResultSet rs = null;    
        try {
            stmt = makeConnection();
            rs = stmt.executeQuery("SELECT count(*) as count FROM Users where username='"+username+"'"); 
            if(rs.next() && rs.getInt("count") == 0)
            {
            	stmt.executeUpdate("INSERT INTO Users VALUES('"+username+ "','" +password+ "', 0, 'novice')");
            	closeAll(stmt.getConnection(), stmt, rs);
            	return username;
            }
            else
            {
            	closeAll(stmt.getConnection(), stmt, rs);
            	return "";
            }
        }catch(Exception ignore){  
        	return null; 
        	}
     }
    
    public static boolean generateVersusStatsMessages(String username) 
    {																		
    	Statement stmt = null; ResultSet rs = null; int userPoi = -1; int oppoPoi = -1; int userPaw = -1; int oppoPaw = -1;
    	String getAllPlayed = "SELECT * FROM GameVersus WHERE (userOne = '"+username+"' OR userTwo = '"+username+"' ) AND userOnePoints+userTwoPoints > 0";
    	try 
    	{
            stmt = makeConnection();
            ArrayList<HashMap<String, String>> versusStats = new ArrayList();
            rs = stmt.executeQuery(getAllPlayed);
 
            while(rs.next())
            	versusStats.add(MSG.newVersusEntry(username,rs.getString("userOne"),rs.getString("userTwo"),rs.getInt("userOnePoints"), 
            			rs.getInt("userTwoPoints"), rs.getInt("pawnsSavedOne"), rs.getInt("pawnsSavedTwo")));
            	
            rs = stmt.executeQuery(DataCenter.generateOverallWinsAndLosses(username));
            userPoi = rs.getInt(1);
            oppoPoi = rs.getInt(2);
            userPaw = rs.getInt(3);
            oppoPaw = rs.getInt(4);
            
            closeAll(stmt.getConnection(), stmt, rs);
           
            UMS.storeLobbyMessage(username, MSG.newVersusEntry(username, username, "Overall", 
            		userPoi, oppoPoi, userPaw, oppoPaw));
            UMS.storeLobbyMessages(username, (HashMap<String, String>[])versusStats.toArray());
           
            return true;

        }catch(Exception ignore){  
        	return false;  }
    }
    
    public static boolean generateAllTrophyMessages(String username)
    {
    	Statement stmt = null; ResultSet rs = null;
    	try {
            stmt = makeConnection();
            ArrayList<HashMap<String, String>> trophies = new ArrayList();
            rs = stmt.executeQuery("SELECT id, current, target FROM Trophies WHERE username = '"+username+"'");
            while(rs.next())
            {
            	int current = (int)((rs.getInt("current")*1.0)/(rs.getInt("target"))*100.0);
            	trophies.add(MSG.trophyData(rs.getInt("id"), current));
            }
            closeAll(stmt.getConnection(), stmt, rs);
            
            UMS.storeLobbyMessages(username, (HashMap<String, String>[])trophies.toArray());
            return true;
        }
    	catch(Exception ignore){  
        	return false;  }
    }
    
    private static String generateSingleVersusInsert(String newUser, String otherUser)
    {
    	 return "INSERT INTO GameVersus VALUES('"+newUser+"', '"+otherUser+"', 0, 0, 0, 0)";
    }
    
    public static boolean createNewUserVersusEntries(String username) throws Exception            
    {
    	Statement stmt = null; ResultSet rs = null;
        String otherUsersQuery = "SELECT username FROM Users WHERE username <> '"+username+"'";
    	StringList otherUsers = new StringList();
        try 
        {
            stmt = makeConnection();
            rs = stmt.executeQuery(otherUsersQuery);
            while(rs.next())
            	otherUsers.addStringEntry(rs.getString("username"));
            
            for(String otherUser : otherUsers.toArray())
            	stmt.addBatch(DataCenter.generateSingleVersusInsert(username, otherUser));
            stmt.executeBatch();
            
            closeAll(stmt.getConnection(), stmt, rs);
            return true;
        }
        catch(Exception ignore){ 
        	ignore.getMessage(); 
        	throw ignore;
        	}
    }
    
    public static boolean createNewUserTrophyEntries(String username) throws Exception
    {
    	Statement stmt = null; ResultSet rs = null;
        try 
        {
            stmt = makeConnection();
            for(TrophyRow row: DataCenter.trophies)
            	stmt.addBatch(DataCenter.generateInsertForTrophyRow(row, username));  
            stmt.executeBatch();
            
            for(TrophyRow row: DataCenter.trophies)
            	stmt.addBatch(DataCenter.generateSQLForTrophyRow(row, username));
            stmt.executeBatch();
            
            closeAll(stmt.getConnection(), stmt, rs);
            return true;
        }
        catch(Exception ignore){ 
        	ignore.getMessage();
        	throw ignore;
        	}
    }
    
    public static void closeAll(Connection c, Statement s, ResultSet r)
    {
    	if(r != null)try {r.close();} catch(SQLException e){
			e.printStackTrace();
		}
    	if(s != null)try {s.close();} catch (SQLException e) {
			e.printStackTrace();
		}
    	if(r != null)try{c.close();} catch(SQLException e){
    		e.printStackTrace();}
    }
    
    private static String getCurrentDateTime()
    {
    	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date(System.currentTimeMillis());
        return sdfDate.format(now);
    }
    																				
    public static boolean storeSingleGameResults(String winner, String loser, int points, int cube, int multiplier, int winPawns,
    		int lossPawns,int winSteps, int lossSteps, int gameType) 
    {																												   
        Statement stmt = null;
        String time = DataCenter.getCurrentDateTime();
        String insert = "INSERT INTO GameResults VALUES('"+winner+"','"+loser+"',"+points+","+cube+","+multiplier+","+winPawns+","+lossPawns+
        				","+winSteps+","+lossSteps+","+gameType+",'"+time+"')";
        try 
        {
            stmt = makeConnection();
            stmt.executeUpdate(insert);
            closeAll(stmt.getConnection(), stmt, null);
            return true;
        }
        catch(Exception ignore){
             ignore.getMessage();
              return false;
            }
    }
    
    public static boolean updateGameVersus(String winner, String loser, int winnerPoints, int winPawnsSaved, int lossPawnsSaved)
    {
    	Statement stmt = null;
    	String userOneWinnerUpdate = "UPDATE GameVersus SET userOnePoints = userOnePoints + "+winnerPoints+", pawnsSavedOne = pawnsSavedOne + "+winPawnsSaved+
    							", pawnsSavedTwo = pawnsSavedTwo + "+lossPawnsSaved+"  WHERE userOne = '"+winner+"' AND userTwo = '"+loser+"'";
    	String userTwoWinnerUpdate = "UPDATE GameVersus SET userTwoPoints = userTwoPoints + "+winnerPoints+", pawnsSavedTwo = pawnsSavedTwo + "+winPawnsSaved+
				", pawnsSavedOne = pawnsSavedOne + "+lossPawnsSaved+"  WHERE userTwo = '"+winner+"' AND userOne = '"+loser+"'";
    	
    	String checkIfWinnerIsUserOne = "SELECT COUNT(*) FROM GameVersus WHERE userOne = '"+winner+"' AND userTwo = '"+loser+"'";
    	
    	try
    	{
    		stmt = makeConnection();
    		ResultSet isWinnerUserOne = stmt.executeQuery(checkIfWinnerIsUserOne);
    		
    		if(isWinnerUserOne.next() && isWinnerUserOne.getInt(1) > 0)
    			stmt.executeUpdate(userOneWinnerUpdate);
    		else
    			stmt.executeUpdate(userTwoWinnerUpdate);
    		
    		closeAll(stmt.getConnection(), stmt, null);
    		return true;
    	}
    	catch(Exception e){
    		e.getMessage();
    		return false;
    	}
    }
    
    private static String generateOverallWinsAndLosses(String username)
    {
    	return "SELECT winp.sum, lossp.sum, winp.pawns, lossp.pawns FROM" +
			   "(SELECT SUM(points) as sum, SUM(15-winPawns) as pawns FROM GameResults WHERE winner = '"+username+"' ) as winp, " +
		       "(SELECT SUM(points) as sum, SUM(15-winPawns) as pawns FROM GameResults WHERE loser = '"+username+"' ) as lossp ";
    }
    
    //erum með update statement ready, sækum row.sql í þær raðir með completed = 0, username = 'username' aka notFinishedTrophies 
    
    public static boolean checkForNewTrophies(String username) throws Exception
    {
    	Statement stmt = null; 
    	StringList updates = new StringList();
    	
    	String updatesToRun = "SELECT sql FROM Trophies WHERE username = '"+username+"' AND completed = 0";
    	
    	String findNewlyFinishedTrophies = 
    			"SELECT id FROM Trophies WHERE username = '"+username+"' AND completed = 0 AND current >= target ";
    	
    	String changeCompletedWhereNeeded = "UPDATE Trophies SET completed = 1 WHERE completed = 0 AND current >= target"; 
    	
    	try
    	{
    		stmt = makeConnection();
            ResultSet rs = stmt.executeQuery(updatesToRun);
            while(rs.next())
            	updates.addStringEntry(rs.getString("sql"));
            	
            for(String update: updates.toArray()) 
            	stmt.addBatch(update);
            stmt.executeBatch();
            
            rs = stmt.executeQuery(findNewlyFinishedTrophies);
            while(rs.next())
            	UMS.storeInGameMessage(username, MSG.presentTrophy(rs.getInt("id")));
            
            stmt.executeUpdate(changeCompletedWhereNeeded);
            closeAll(stmt.getConnection(), stmt, null);

            return true;
    	}
    	catch(Exception e)
    	{
    		e.getMessage();
    		throw e;
    	}
    }
    
    private static Statement makeConnection() throws ClassNotFoundException, SQLException
    {
    	Class.forName("org.sqlite.JDBC");
    	Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Aðalsteinn\\git\\BackGammon\\base.db");
        return conn.createStatement();
    }
    
    public static boolean storeMatchResults(String winner, String loser, int pointGoal, int winPoints, int lossPoints, int type) 
    {
    	Statement stmt = null;
    	String time = DataCenter.getCurrentDateTime();
    	String insert = "INSERT INTO MatchResults VALUES('"+winner+"','"+loser+"', "+pointGoal+","+winPoints+","+lossPoints+
    						", "+type+", '"+time+"' )";
    	try 
    	{
        	stmt = makeConnection();
            stmt.executeUpdate(insert); 
            closeAll(stmt.getConnection(), stmt, null);
            return true;
           
        }catch(Exception ignore)
    	{
            ignore.getMessage();
            return false;
        }
    }
    
    public static void deliverUserImage(String username) throws Exception //eyða þessu ef við viljum höndla hér
    {
        Statement stmt = null; String imageId = null; String title = null;;
        
        try{
        	stmt = makeConnection();
        	ResultSet rs = stmt.executeQuery("SELECT imageId,title FROM Users WHERE username='"+username+"'");
        	if(rs.next())
        	{
        		imageId = rs.getString("imageId");
        		title = rs.getString("title");
        	}
        	closeAll(stmt.getConnection(), stmt, rs);
        }catch(Exception e)
        {
        	
        }
        //UMS.storeLobbyMessage(username, MSG.userImageMessage(imageId, title));
    }
    
}












