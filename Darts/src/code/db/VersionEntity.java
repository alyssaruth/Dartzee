package code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import code.utils.DartsDatabaseUtil;

public class VersionEntity extends AbstractEntity<VersionEntity>
{
	private int version = DartsDatabaseUtil.DATABASE_VERSION;
	
	@Override
	public String getTableName() 
	{
		return "Version";
	}

	@Override
	public String getCreateTableSqlSpecific() 
	{
		return "Version int";
	}

	@Override
	public void populateFromResultSet(VersionEntity version, ResultSet rs) throws SQLException 
	{
		version.setVersion(rs.getInt("Version"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException 
	{
		statementStr = writeInt(statement, i, version, statementStr);
		return statementStr;
	}
	
	public int getVersion()
	{
		return version;
	}
	public void setVersion(int version)
	{
		this.version = version;
	}
	
	public static VersionEntity retrieveCurrentDatabaseVersion()
	{
		ArrayList<VersionEntity> entities = new VersionEntity().retrieveEntities("1 = 1");
		if (entities.isEmpty())
		{
			return null;
		}
		
		VersionEntity entity = entities.get(0);
		return entity;
	}
}
