package burlton.dartzee.code.utils;

import com.sun.rowset.CachedRowSetImpl;
import burlton.core.code.util.AbstractClient;
import burlton.core.code.util.Debug;
import burlton.desktopcore.code.util.DialogUtil;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.Properties;

/**
 * Generic derby helper methods
 */
public class DatabaseUtil implements SqlErrorConstants
{
	public static final String DATABASE_NAME = "jdbc:derby:Darts";
	public static final String DATABASE_NAME_WITH_CREATE = DATABASE_NAME + ";create=true";
	
	public static final String DATABASE_FILE_PATH = System.getProperty("user.dir") + "\\Databases";
	
	
	public static Connection createDatabaseConnection() throws SQLException
	{
		return createDatabaseConnection(DATABASE_FILE_PATH, DATABASE_NAME_WITH_CREATE);
	}
	private static Connection createDatabaseConnection(String dbFilePath, String dbName) throws SQLException
	{
		//TODO - Try dbName as "jdbc:derby:\\Desktop\blah\blah\Darts\\Databases".
		if (dbFilePath == null)
		{
			dbFilePath = DATABASE_FILE_PATH;
		}
		
		Properties p = System.getProperties();
		p.setProperty("derby.system.home", dbFilePath);
		
		Properties props = new Properties();
		props.put("user", "administrator");
        props.put("password", "wallace");
		
		return DriverManager.getConnection(dbName, props);
	}
	
	public static boolean executeUpdate(String statement)
	{
		try
		{
			executeUpdateUncaught(statement);
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(statement, sqle);
			return false;
		}
		
		return true;
	}
	public static void executeUpdateUncaught(String statement) throws SQLException
	{
		Debug.appendSql(statement, AbstractClient.traceWriteSql);
		
		try (Connection conn = createDatabaseConnection();
		  Statement s = conn.createStatement();)
		{
			s.execute(statement);
		}
	}
	
	public static ResultSet executeQuery(StringBuilder sb)
	{
		return executeQuery(sb.toString());
	}
	public static ResultSet executeQuery(String query)
	{
		return executeQuery(query, null);
	}
	public static ResultSet executeQuery(String query, String dbPath)
	{
		long startMillis = System.currentTimeMillis();
		CachedRowSet crs = null;
		
		try (Connection conn = DatabaseUtil.createDatabaseConnection(dbPath, DATABASE_NAME_WITH_CREATE);
		  Statement s = conn.createStatement();
		  ResultSet rs = s.executeQuery(query);)
		{
			crs = new CachedRowSetImpl();
			crs.populate(rs);
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(query, sqle);
		}
		
		long totalMillis = System.currentTimeMillis() - startMillis;
		
		Debug.appendSql("(" + totalMillis + "ms) " + query, AbstractClient.traceReadSql);
		
		//No query should take longer than 5 seconds really...
		if (totalMillis > AbstractClient.SQL_TOLERANCE_QUERY)
		{
			Debug.stackTraceNoError("SQL query took longer than " + AbstractClient.SQL_TOLERANCE_QUERY + " millis: " + query);
		}
		
		return crs;
	}
	
	public static int executeQueryAggregate(StringBuilder sb)
	{
		return executeQueryAggregate(sb.toString());
	}
	public static int executeQueryAggregate(String sql)
	{
		try (ResultSet rs = executeQuery(sql);)
		{
			rs.next();
			return rs.getInt(1);
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sql, sqle);
			return -1;
		}
	}
	
	public static void doDuplicateInstanceCheck()
	{
		try (Connection conn = createDatabaseConnection())
		{
			
		}
		catch (SQLException sqle)
		{
			SQLException next = sqle.getNextException();
			if (next != null
			  && next.getMessage().contains("Another instance of Derby may have already booted the database"))
			{
				Debug.stackTraceSilently(sqle);
				DialogUtil.showError("Database already in use - Dartzee will now exit.");
				System.exit(1);
			}
			else
			{
				Debug.stackTrace(sqle);
			}
		}
	}
	
	public static boolean createTableIfNotExists(String tableName, String columnSql)
	{
		String statement = "CREATE TABLE " + tableName + "(" + columnSql + ")";
		
		try
		{
			DatabaseUtil.executeUpdateUncaught(statement);
			Debug.append("Created " + tableName + " table.");
		}
		catch (SQLException sqle)
		{
			String state = sqle.getSQLState();
			if (state.equals(TABLE_ALREADY_EXISTS))
			{
				Debug.append(tableName + " table already exists");
			}
			else
			{
				Debug.logSqlException(statement, sqle);
			}
			
			return false;
		}
		
		return true;
	}
	
	public static String createTempTable(String tableName, String colStr)
	{
		long millis = System.currentTimeMillis();
		tableName = "zzTmp_" + tableName + millis;
		
		boolean success = createTableIfNotExists(tableName, colStr);
		if (success)
		{
			return tableName;
		}
		
		return null;
	}
	
	public static boolean dropTable(String tableName)
	{
		String sql = "DROP TABLE " + tableName;
		return executeUpdate(sql);
	}
	
	public static boolean testConnection(String dbPath)
	{
		try (Connection conn = createDatabaseConnection(dbPath, DATABASE_NAME_WITH_CREATE)) {}
		catch (Throwable t)
		{
			Debug.append("Failed to establish test connection for path " + dbPath);
			Debug.stackTraceSilently(t);
			return false;
		}
		
		Debug.append("Successfully created test connection to " + dbPath);
		return true;
	}
	public static boolean shutdownDerby()
	{
		try (Connection conn = createDatabaseConnection(DATABASE_FILE_PATH, "jdbc:derby:;shutdown=true"))
		{
			
		}
		catch (SQLException sqle)
		{
			String msg = sqle.getMessage();
			if (msg.contains("shutdown"))
			{
				//Derby ALWAYS throws an exception on shutdown.
				return true;
			}
			
			Debug.stackTrace(sqle);
		}
		
		return false;
	}
}
