package code.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import code.utils.DatabaseUtil;
import code.utils.SqlErrorConstants;
import object.HandyArrayList;
import util.AbstractClient;
import util.DateUtil;
import util.Debug;
import util.StringUtil;

public abstract class AbstractEntity<E extends AbstractEntity<E>>
					  implements SqlErrorConstants
{
	//statics
	private static final Object UNIQUE_ID_SYNCH_OBJECT = new Object();
	private static ConcurrentHashMap<String, Long> hmLastAssignedIdByTableName = new ConcurrentHashMap<>();
	
	//db fields
	private long rowId = -1;
	private Timestamp dtCreation = DateUtil.getSqlDateNow();
	private Timestamp dtLastUpdate = DateUtil.END_OF_TIME;
	
	//other variables
	private boolean retrievedFromDb = false;
	
	public abstract String getTableName();
	public abstract String getCreateTableSqlSpecific();
	public abstract void populateFromResultSet(E entity, ResultSet rs) throws SQLException;
	public abstract String writeValuesToStatement(PreparedStatement statement, int startIndex, String statementStr)  throws SQLException;
	
	public E factoryFromResultSet(ResultSet rs) throws SQLException
	{
		E ret = factory();
		ret.setRowId(rs.getLong("RowId"));
		ret.setDtCreation(rs.getTimestamp("DtCreation"));
		ret.setDtLastUpdate(rs.getTimestamp("DtLastUpdate"));
		
		populateFromResultSet(ret, rs);
		
		return ret;
	}
	public E factory()
	{
		try
		{
			return (E)getClass().newInstance();
		}
		catch (IllegalAccessException | InstantiationException iae)
		{
			Debug.stackTrace(iae);
			return null;
		}
	}
	
	/**
	 * Default method, override to index your table on creation
	 */
	public void addListsOfColumnsForIndexes(ArrayList<ArrayList<String>> indexes)
	{
		//Do nothing
		indexes.size();
	}
	
	public final boolean columnCanBeUnset(String columnName)
	{
		return getColumnsAllowedToBeUnset().contains(columnName);
	}
	protected ArrayList<String> getColumnsAllowedToBeUnset()
	{
		return new ArrayList<>();
	}
	
	public long assignRowId()
	{
		synchronized (UNIQUE_ID_SYNCH_OBJECT)
		{
			String tableName = getTableName();
			Long lastAssignedIdObj = hmLastAssignedIdByTableName.get(tableName);
			long lastAssignedId = -1;
			if (lastAssignedIdObj == null)
			{
				String query = "SELECT MAX(RowId) FROM " + getTableName();
				
				try (ResultSet rs = DatabaseUtil.executeQuery(query))
				{
					rs.next();
					lastAssignedId = rs.getInt(1);
				}
				catch (SQLException sqle)
				{
					Debug.logSqlException(query, sqle);
				}
			}
			else
			{
				lastAssignedId = lastAssignedIdObj.longValue();
			}
			
			rowId = lastAssignedId + 1;
			hmLastAssignedIdByTableName.put(tableName, rowId);
			
			return rowId;
		}
	}
	
	/**
	 * Alternate DB methods
	 */
	public HandyArrayList<E> retrieveEntitiesAlternateDb(String dbPath)
	{
		return retrieveEntities("", "", dbPath);
	}
 	
	public E retrieveEntity(String whereSql)
	{
		HandyArrayList<E> entities = retrieveEntities(whereSql);
		if (entities.size() > 1)
		{
			Debug.stackTrace("Retrieved " + entities.size() + " rows from " + getTableName() + ". Expected 1. WhereSQL [" + whereSql + "]");
		}
		
		if (entities.isEmpty())
		{
			return null;
		}
		
		return entities.firstElement();
	}
	
	public HandyArrayList<E> retrieveEntities()
	{
		return retrieveEntities("");
	}
	public HandyArrayList<E> retrieveEntities(String whereSql)
	{
		return retrieveEntities(whereSql, "");
	}
	public HandyArrayList<E> retrieveEntities(String whereSql, String alias)
	{
		return retrieveEntities(whereSql, alias, null);
	}
	public HandyArrayList<E> retrieveEntities(String whereSql, String alias, String dbPath)
	{
		String queryWithFrom = "FROM " + getTableName() + " " + alias;
		if (!whereSql.isEmpty())
		{
			queryWithFrom = queryWithFrom + " WHERE " + whereSql;
		}
		
		return retrieveEntitiesWithFrom(queryWithFrom, alias, dbPath);
	}
	public HandyArrayList<E> retrieveEntitiesWithFrom(String whereSqlWithFrom, String alias, String dbPath)
	{
		String query = "SELECT " + getColumnsForSelectStatement(alias) + " " + whereSqlWithFrom;
		
		HandyArrayList<E> ret = new HandyArrayList<>();
		
		try (ResultSet rs = DatabaseUtil.executeQuery(query, dbPath))
		{
			while (rs.next())
			{
				E entity = factoryFromResultSet(rs);
				entity.setRetrievedFromDb(true);
				ret.add(entity);
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(query, sqle);
		}
		
		return ret;
	}
	
	public E retrieveForId(long rowId)
	{
		return retrieveForId(rowId, true);
	}
	public E retrieveForId(long rowId, boolean stackTraceIfNotFound)
	{
		ArrayList<E> entities = retrieveEntities("RowId = " + rowId);
		if (entities.isEmpty())
		{
			if (stackTraceIfNotFound)
			{
				Debug.stackTrace("Failed to find " + getTableName() + " for ID " + rowId);
			}
			
			return null;
		}
		
		if (entities.size() > 1)
		{
			Debug.stackTrace("Found " + entities.size() + " " + getTableName() + " rows for ID " + rowId);
		}
		
		return entities.get(0);
	}
	
	public boolean deleteFromDatabase()
	{
		String sql = "DELETE FROM " + getTableName() + " WHERE RowId = " + rowId;
		return DatabaseUtil.executeUpdate(sql);
	}
	
	public void saveToDatabase()
	{
		saveToDatabase(DateUtil.getSqlDateNow());
	}
	public void saveToDatabase(Timestamp dtLastUpdate)
	{
		this.dtLastUpdate = dtLastUpdate;
		
		if (retrievedFromDb)
		{
			updateDatabaseRow();
		}
		else
		{
			insertIntoDatabase();
		}
	}
	
	private void updateDatabaseRow()
	{
		String updateQuery = buildUpdateQuery();
		
		try (Connection conn = DatabaseUtil.createDatabaseConnection();
		  PreparedStatement psUpdate = conn.prepareStatement(updateQuery))
		{
			updateQuery = writeTimestamp(psUpdate, 1, getDtCreation(), updateQuery);
			updateQuery = writeTimestamp(psUpdate, 2, getDtLastUpdate(), updateQuery);
			updateQuery = writeValuesToStatement(psUpdate, 3, updateQuery);
			updateQuery = writeLong(psUpdate, getColumnCount(), rowId, updateQuery);
			
			Debug.appendSql(updateQuery, AbstractClient.traceWriteSql);
			
			psUpdate.executeUpdate();
			
			int updateCount = psUpdate.getUpdateCount();
			if (updateCount == 0)
			{
				Debug.stackTrace("0 rows updated: " + updateQuery);
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(updateQuery, sqle);
		}
	}
	
	private String buildUpdateQuery()
	{
		//Some fun String manipulation
		String columns = getColumnsForSelectStatement();
		columns = columns.replaceAll("RowId, ", "");
		columns = columns.replaceAll(",", "=?,");
		columns += "=?";
		
		String query = "UPDATE " + getTableName()
					 + " SET " + columns
					 + " WHERE RowId=?";
		
		return query;
	}
	
	private void insertIntoDatabase()
	{
		String insertQuery = buildInsertQuery();
		
		try (Connection conn = DatabaseUtil.createDatabaseConnection();
		  PreparedStatement psInsert = conn.prepareStatement(insertQuery);)
		{
			insertQuery = writeLong(psInsert, 1, getRowId(), insertQuery);
			insertQuery = writeTimestamp(psInsert, 2, getDtCreation(), insertQuery);
			insertQuery = writeTimestamp(psInsert, 3, getDtLastUpdate(), insertQuery);
			insertQuery = writeValuesToStatement(psInsert, 4, insertQuery);
			
			Debug.appendSql(insertQuery, AbstractClient.traceWriteSql);
			
			psInsert.executeUpdate();
			
			//Set this so we can call save() again on the same java object and get the right behaviour
			retrievedFromDb = true;
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(insertQuery, sqle);
		}
	}
	
	private String buildInsertQuery()
	{
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("INSERT INTO ");
		sbInsert.append(getTableName());
		sbInsert.append(" VALUES (");
		
		for (int i=0; i<getColumnCount(); i++)
		{
			if (i > 0)
			{
				sbInsert.append(", ");
			}
			
			sbInsert.append("?");
		}
		
		sbInsert.append(")");
		return sbInsert.toString();
	}
	
	private int getColumnCount()
	{
		String columns = getColumnsForSelectStatement();
		return StringUtil.countOccurences(columns, ",") + 1;
	}
	
	public boolean createTable()
	{
		boolean createdTable = DatabaseUtil.createTableIfNotExists(getTableName(), getCreateTableColumnSql());
		if (createdTable)
		{
			createIndexes();
		}
		
		return createdTable;
	}
	public void createIndexes()
	{
		//Also create the indexes
		ArrayList<ArrayList<String>> indexes = new ArrayList<>();
		addListsOfColumnsForIndexes(indexes);
		
		for (int i=0; i<indexes.size(); i++)
		{
			ArrayList<String> columnsForIndex = indexes.get(i);
			createIndex(columnsForIndex);
		}
	}
	private void createIndex(ArrayList<String> columns)
	{
		String columnList = StringUtil.toDelims(columns, ",");
		String indexName = columnList.replaceAll(",", "_");
		
		String statement = "CREATE INDEX " + indexName + " ON " + getTableName() + "(" + columnList + ")";
		boolean success = DatabaseUtil.executeUpdate(statement);
		if (!success)
		{
			Debug.append("Failed to create index " + indexName + " on " + getTableName());
		}
	}
	
	public boolean addIntColumn(String columnName)
	{
		if (columnExists(columnName))
		{
			Debug.append("Not adding column " + columnName + " to " + getTableName() + " as it already exists");
			return false;
		}
		
		String sql = "ALTER TABLE " + getTableName() + " ADD COLUMN " + columnName + " INT NOT NULL DEFAULT -1";
		boolean addedColumn = DatabaseUtil.executeUpdate(sql);
		if (!addedColumn)
		{
			return false;
		}
		
		//We've added the column, now attempt to drop the default.
		String defaultSql = "ALTER TABLE " + getTableName() + " ALTER COLUMN " + columnName + " DEFAULT NULL";
		return DatabaseUtil.executeUpdate(defaultSql);
	}
	private boolean columnExists(String columnName)
	{
		columnName = columnName.toUpperCase();
		String tableName = getTableName().toUpperCase();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(1) ");
		sb.append("FROM sys.systables t, sys.syscolumns c ");
		sb.append("WHERE c.ReferenceId = t.TableId ");
		sb.append("AND t.TableName = '");
		sb.append(tableName);
		sb.append("' AND c.ColumnName = '");
		sb.append(columnName);
		sb.append("'");
		
		int count = DatabaseUtil.executeQueryAggregate(sb);
		return count > 0;
	}
	
	private String getCreateTableColumnSql()
	{
		return "RowId int PRIMARY KEY, DtCreation Timestamp NOT NULL, DtLastUpdate Timestamp NOT NULL, " + getCreateTableSqlSpecific();
	}
	
	public final ArrayList<String> getColumns()
	{
		ArrayList<String> columns = new ArrayList<>();
		
		String columnCreateSql = getCreateTableColumnSql();
		
		ArrayList<String> cols = StringUtil.getListFromDelims(columnCreateSql, ",");
		for (int i=0; i<cols.size(); i++)
		{
			String rawColumn = cols.get(i);
			String cleanColumn = getColumnNameFromCreateSql(rawColumn);
			
			columns.add(cleanColumn);
		}
		
		return columns;
	}
	
	private String getColumnsForSelectStatement()
	{
		return getColumnsForSelectStatement("");
	}
	private String getColumnsForSelectStatement(String alias)
	{
		StringBuilder sb = new StringBuilder();
		
		ArrayList<String> cols = getColumns();
		for (int i=0; i<cols.size(); i++)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			
			String column = cols.get(i);
			if (!alias.isEmpty())
			{
				column = alias + "." + column;
			}
			
			sb.append(column);
		}
		
		return sb.toString();
	}
	private String getColumnNameFromCreateSql(String col)
	{
		col = col.trim();
		col = col.replace("(", "");
		col = col.replace(")", "");
		
		int spaceIndex = col.indexOf(' ');
		col = col.substring(0, spaceIndex);
		
		return col;
	}
	
	/**
	 * Write to statement methods
	 */
	public String writeLong(PreparedStatement ps, int ix, long value, String statementStr) throws SQLException
	{
		ps.setLong(ix, value);
		return swapInValue(statementStr, value);
	}
	public String writeInt(PreparedStatement ps, int ix, int value, String statementStr) throws SQLException
	{
		ps.setInt(ix, value);
		return swapInValue(statementStr, value);
	}
	public String writeString(PreparedStatement ps, int ix, String value, String statementStr) throws SQLException
	{
		ps.setString(ix, value);
		return swapInValue(statementStr, value);
	}
	public String writeTimestamp(PreparedStatement ps, int ix, Timestamp value, String statementStr) throws SQLException
	{
		ps.setTimestamp(ix, value);
		return swapInValue(statementStr, value);
	}
	public String writeBlob(PreparedStatement ps, int ix, Blob value, String statementStr) throws SQLException
	{
		ps.setBlob(ix, value);
		String blobStr = "Blob (dataLength: " + value.length() + ")";
		return swapInValue(statementStr, blobStr);
	}
	public String writeBoolean(PreparedStatement ps, int ix, boolean value, String statementStr) throws SQLException
	{
		ps.setBoolean(ix, value);
		return swapInValue(statementStr, value);
	}
	private String swapInValue(String statementStr, Object value)
	{
		statementStr = statementStr.replaceFirst(Pattern.quote("?"), "" + value);
		return statementStr;
	}
	
	public static <E> HandyArrayList<E> makeFromEntityFields(ArrayList<AbstractEntity<?>> entities, String fieldName)
	{
		HandyArrayList<E> ret = new HandyArrayList<>();
		if (entities.isEmpty())
		{
			return ret;
		}
		
		try
		{
			String getMethod = "get" + fieldName;
			
			for (AbstractEntity<?> entity : entities)
			{
				Class<AbstractEntity<?>> c = (Class<AbstractEntity<?>>)entity.getClass();
				Method m = c.getMethod(getMethod, new Class[0]);
				E obj = (E)m.invoke(entity, new Object[0]);
				ret.add(obj);
			}
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
		{
			Debug.stackTrace(e, "Reflection error making field list [" + fieldName + "] for entities: " + entities);
		}
		
		return ret;	
		
		
	}
	
	public String getTableNameUpperCase()
	{
		return getTableName().toUpperCase();
	}
	
	/**
	 * Gets / Sets
	 */
	public long getRowId()
	{
		return rowId;
	}
	public void setRowId(long rowId)
	{
		this.rowId = rowId;
	}
	public Timestamp getDtCreation()
	{
		return dtCreation;
	}
	public void setDtCreation(Timestamp dtCreation)
	{
		this.dtCreation = dtCreation;
	}
	public Timestamp getDtLastUpdate()
	{
		return dtLastUpdate;
	}
	public void setDtLastUpdate(Timestamp dtLastUpdate)
	{
		this.dtLastUpdate = dtLastUpdate;
	}
	public boolean getRetrievedFromDb()
	{
		return retrievedFromDb;
	}
	public void setRetrievedFromDb(boolean retrievedFromDb)
	{
		this.retrievedFromDb = retrievedFromDb;
	}
}
