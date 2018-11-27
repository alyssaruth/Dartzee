package burlton.dartzee.code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import burlton.dartzee.code.screen.HumanCreationDialog;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.screen.ai.AIConfigurationDialog;
import burlton.dartzee.code.ai.AbstractDartsModel;
import burlton.core.code.util.AbstractClient;
import burlton.desktopcore.code.util.DateUtil;
import burlton.desktopcore.code.util.DialogUtil;

public class PlayerEntity extends AbstractDartsEntity<PlayerEntity>
{
	private static final ImageIcon ICON_AI = new ImageIcon(PlayerEntity.class.getResource("/flags/aiFlag.png"));
	private static final ImageIcon ICON_HUMAN = new ImageIcon(PlayerEntity.class.getResource("/flags/humanFlag.png"));
	
	private String name = "";
	private int strategy = -1;
	private String strategyXml = "";
	private Timestamp dtDeleted = DateUtil.END_OF_TIME;
	private long playerImageId = -1;
	
	//Player match stuff
	private PlayerEntity matchedPlayer = null;
	private boolean autoMatched = false;
	private String newName = "";
	
	@Override
	public String getTableName() 
	{
		return "Player";
	}

	@Override
	public void populateFromResultSet(PlayerEntity player, ResultSet rs) throws SQLException
	{
		player.setName(rs.getString("Name"));
		player.setStrategy(rs.getInt("Strategy"));
		player.setStrategyXml(rs.getString("StrategyXml"));
		player.setDtDeleted(rs.getTimestamp("DtDeleted"));
		player.setPlayerImageId(rs.getLong("PlayerImageId"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException
	{
		statementStr = writeString(statement, i++, name, statementStr);
		statementStr = writeInt(statement, i++, strategy, statementStr);
		statementStr = writeString(statement, i++, strategyXml, statementStr);
		statementStr = writeTimestamp(statement, i++, dtDeleted, statementStr);
		statementStr = writeLong(statement, i++, playerImageId, statementStr);
		
		return statementStr;
	}

	@Override
	public String getCreateTableSqlSpecific() 
	{
		return "Name varchar(25) NOT NULL, "
				+ "Strategy int NOT NULL, "
				+ "StrategyXml varchar(1000) NOT NULL, "
				+ "DtDeleted timestamp NOT NULL, "
				+ "PlayerImageId int NOT NULL";
	}
	
	@Override
	public void addListsOfColumnsForIndexes(ArrayList<ArrayList<String>> v)
	{
		ArrayList<String> nameIndex = new ArrayList<>();
		nameIndex.add("Name");
		
		ArrayList<String> strategyDtDeletedIndex = new ArrayList<>();
		strategyDtDeletedIndex.add("Strategy");
		strategyDtDeletedIndex.add("DtDeleted");
		
		v.add(nameIndex);
		v.add(strategyDtDeletedIndex);
	}
	
	@Override
	public boolean createTable()
	{
		boolean createdTable = super.createTable();
		
		if (AbstractClient.devMode
		  && createdTable)
		{
			factoryAndSaveHuman("Alex", 1);
			factoryAndSaveHuman("Chris", 2);
		}
		
		return createdTable;
	}
	
	@Override
	public long getGameId()
	{
		return -1;
	}
	
	@Override
	public String toString() 
	{
		return name;
	}
	
	/**
	 * Helpers
	 */
	public boolean isHuman()
	{
		return strategy == -1;
	}
	public boolean isAi()
	{
		return strategy > -1;
	}
	public AbstractDartsModel getModel()
	{
		AbstractDartsModel model = AbstractDartsModel.factoryForType(strategy);
		model.readXml(strategyXml);
		return model;
	}
	public ImageIcon getAvatar()
	{
		if (playerImageId == -1)
		{
			return null;
		}
		
		return PlayerImageEntity.retrieveImageIconForId(playerImageId);
	}
	public ImageIcon getFlag()
	{
		return getPlayerFlag(isHuman());
	}
	
	public String getMatchDesc()
	{
		if (matchedPlayer != null)
		{
			String desc = "Automatched";
			if (!autoMatched)
			{
				desc = "Manually matched";
			}
			
			desc += " to " + matchedPlayer.getName() + " [" + matchedPlayer.getRowId() + "]";
			return desc;
		}
		else
		{
			String desc = "New player will be created";
			if (!newName.isEmpty())
			{
				desc += " with name " + newName;
			}
			
			return desc;
		}
	}
	
	public static ImageIcon getPlayerFlag(boolean human)
	{
		if (human)
		{
			return ICON_HUMAN;
		}
		
		return ICON_AI;
	}
	
	/**
	 * Retrieval methods
	 */
	public static ArrayList<PlayerEntity> retrievePlayers(String startingSql, boolean includeDeleted)
	{
		if (!includeDeleted)
		{
			if (!startingSql.isEmpty())
			{
				startingSql += " AND ";
			}
			startingSql += "DtDeleted = " + DateUtil.getEndOfTimeSqlString();
		}
		
		return new PlayerEntity().retrieveEntities(startingSql);
	}
	private static PlayerEntity retrieveForName(String name)
	{
		String whereSql = "Name = '" + name + "' AND DtDeleted = " + DateUtil.getEndOfTimeSqlString();
		ArrayList<PlayerEntity> players = new PlayerEntity().retrieveEntities(whereSql);
		if (players.isEmpty())
		{
			return null;
		}
		
		return players.get(0);
	}
	
	/**
	 * Creation/validation
	 */
	public static void createNewPlayer(boolean human)
	{
		boolean created = createAndSavePlayerIfValid(human);
		if (created)
		{
			ScreenCache.getPlayerManagementScreen().initialise();
		}
	}
	private static boolean createAndSavePlayerIfValid(boolean human)
	{
		if (human)
		{
			return createNewHuman();
		}
		
		return createNewAI();
	}
	private static boolean createNewHuman()
	{
		HumanCreationDialog dlg = ScreenCache.getHumanCreationDialog();
		dlg.init();
		dlg.setVisible(true);
		
		return dlg.getCreatedPlayer();
	}
	private static boolean createNewAI()
	{
		AIConfigurationDialog dialog = ScreenCache.getAIConfigurationDialog();
		dialog.init(null);
		dialog.setVisible(true);
		
		return dialog.getCreatedPlayer();
	}
	public static boolean isValidName(String name, boolean checkForExistence)
	{
		if (name == null
		  || name.isEmpty())
		{
			DialogUtil.showError("You must enter a name for this player.");
			return false;
		}
		
		int length = name.length();
		if (length < 3)
		{
			DialogUtil.showError("The player name must be at least 3 characters long.");
			return false;
		}
		
		if (length > 25)
		{
			DialogUtil.showError("The player name cannot be more than 25 characters long.");
			return false;
		}
		
		if (checkForExistence)
		{
			PlayerEntity existingPlayer = retrieveForName(name);
			if (existingPlayer != null)
			{
				DialogUtil.showError("A player with the name " + name + " already exists.");
				return false;
			}
		}
		
		return true;
	}
	public static PlayerEntity factoryAndSaveHuman(String name, long avatarId)
	{
		PlayerEntity entity = factoryCreate();
		
		entity.setName(name);
		entity.setPlayerImageId(avatarId);
		entity.saveToDatabase();
		entity.setRetrievedFromDb(true);
		
		return entity;
	}
	public static PlayerEntity factoryCreate()
	{
		PlayerEntity entity = new PlayerEntity();
		entity.assignRowId();
		
		return entity;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dtDeleted == null) ? 0 : dtDeleted.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + strategy;
		result = prime * result
				+ ((strategyXml == null) ? 0 : strategyXml.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PlayerEntity))
			return false;
		PlayerEntity other = (PlayerEntity) obj;
		if (dtDeleted == null) {
			if (other.dtDeleted != null)
				return false;
		} else if (!dtDeleted.equals(other.dtDeleted))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (strategy != other.strategy)
			return false;
		if (strategyXml == null) {
			if (other.strategyXml != null)
				return false;
		} else if (!strategyXml.equals(other.strategyXml))
			return false;
		return true;
	}

	/**
	 * Gets / sets
	 */
	public String getName() 
	{
		return name;
	}
	public void setName(String name) 
	{
		this.name = name;
	}
	public int getStrategy() 
	{
		return strategy;
	}
	public void setStrategy(int strategy) 
	{
		this.strategy = strategy;
	}
	public String getStrategyXml() 
	{
		return strategyXml;
	}
	public void setStrategyXml(String strategyXml) 
	{
		this.strategyXml = strategyXml;
	}
	public Timestamp getDtDeleted()
	{
		return dtDeleted;
	}
	public void setDtDeleted(Timestamp dtDeleted)
	{
		this.dtDeleted = dtDeleted;
	}
	public long getPlayerImageId() 
	{
		return playerImageId;
	}
	public void setPlayerImageId(long playerImageId) 
	{
		this.playerImageId = playerImageId;
	}

	/**
	 * Non db gets/sets
	 */
	public PlayerEntity getMatchedPlayer()
	{
		return matchedPlayer;
	}
	public void setMatchedPlayer(PlayerEntity matchedPlayer)
	{
		this.matchedPlayer = matchedPlayer;
	}
	public boolean isAutoMatched()
	{
		return autoMatched;
	}
	public void setAutoMatched(boolean autoMatched)
	{
		this.autoMatched = autoMatched;
	}
}
