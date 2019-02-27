package burlton.dartzee.code.db;

import burlton.dartzee.code.utils.DartsDatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
		List<VersionEntity> entities = new VersionEntity().retrieveEntities("1 = 1");
		if (entities.isEmpty())
		{
			return null;
		}
		
		return entities.get(0);
	}
}
