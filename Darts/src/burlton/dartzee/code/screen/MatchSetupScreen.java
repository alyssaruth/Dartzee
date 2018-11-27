package burlton.dartzee.code.screen;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import burlton.dartzee.code.screen.game.DartsGameScreen;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import burlton.desktopcore.code.bean.RadioButtonPanel;
import burlton.dartzee.code.bean.PlayerSelector;
import burlton.dartzee.code.db.DartsMatchEntity;
import burlton.dartzee.code.db.GameEntity;
import burlton.dartzee.code.db.PlayerEntity;
import net.miginfocom.swing.MigLayout;
import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.XmlUtil;

/**
 * Follow-on from the Game Setup screen, to configure a whole match rather than just a single game.
 */
public final class MatchSetupScreen extends EmbeddedScreen
{
	private int gameType = -1;
	private String gameParams = null;
	
	public MatchSetupScreen() 
	{
		panelPlayers.setBorder(new TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panelPlayers, BorderLayout.CENTER);
		panelPlayers.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panelPlayers.add(panel_2, BorderLayout.SOUTH);
		
		panelPlayers.add(selector, BorderLayout.CENTER);
		
		
		panel_2.add(btnLaunchMatch);
		
		
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new MigLayout("", "[69px][101px][grow][45px][][][]", "[25px][grow]"));
		
		
		panel.add(rdbtnFirstTo, "flowy,cell 0 0,alignx left,aligny top");
		
		panel.add(spinnerWins, "flowx,cell 1 0,alignx left,aligny center");
		
		//Upping max legs because Chris is insane...
		//spinner.setModel(new SpinnerNumberModel(2, 2, 7, 1));
		spinnerWins.setModel(new SpinnerNumberModel(2, 2, 15, 1));
		panel.add(rdbtnPoints, "cell 0 1,alignx left,aligny center");
		spinnerGames.setModel(new SpinnerNumberModel(4, 2, 15, 1));
		
		panel.add(spinnerGames, "flowx,cell 1 1");
		
		panel.add(lblWins, "cell 1 0");
		
		panel.add(lblGames, "cell 1 1");
		
		panel.add(panelPointBreakdown, "cell 2 1,grow");
		panelPointBreakdown.setLayout(new MigLayout("", "[]", "[]"));
		
		panelPointBreakdown.add(lblst, "flowy,cell 0 0,alignx center");
		spinnerPoints1st.setModel(new SpinnerNumberModel(4, 0, 20, 1));
		panelPointBreakdown.add(spinnerPoints1st, "cell 0 0,alignx center");
		
		panelPointBreakdown.add(lb2nd, "flowy,cell 1 0,alignx center");
		spinnerPoints2nd.setModel(new SpinnerNumberModel(3, 0, 20, 1));
		panelPointBreakdown.add(spinnerPoints2nd, "cell 1 0,alignx center");
		
		panelPointBreakdown.add(lb3rd, "flowy,cell 2 0,alignx center");
		spinnerPoints3rd.setModel(new SpinnerNumberModel(2, 0, 20, 1));
		panelPointBreakdown.add(spinnerPoints3rd, "cell 2 0,alignx center");
		
		panelPointBreakdown.add(lb4th, "flowy,cell 3 0,alignx center");
		spinnerPoints4th.setModel(new SpinnerNumberModel(1, 0, 20, 1));
		panelPointBreakdown.add(spinnerPoints4th, "cell 3 0,alignx center");
		
		panel.addActionListener(this);
		btnLaunchMatch.addActionListener(this);
	}
	
	private final RadioButtonPanel panel = new RadioButtonPanel();
	private final JRadioButton rdbtnFirstTo = new JRadioButton("First to");
	private final JRadioButton rdbtnPoints = new JRadioButton("Points-based");
	private final JSpinner spinnerWins = new JSpinner();
	private final JPanel panelPlayers = new JPanel();
	private final PlayerSelector selector = new PlayerSelector();
	private final JButton btnLaunchMatch = new JButton("Launch Match");
	private final JSpinner spinnerGames = new JSpinner();
	private final JLabel lblWins = new JLabel("  wins");
	private final JLabel lblGames = new JLabel("  games  ");
	private final JSpinner spinnerPoints1st = new JSpinner();
	private final JLabel lblst = new JLabel("1st");
	private final JSpinner spinnerPoints2nd = new JSpinner();
	private final JLabel lb2nd = new JLabel("2nd");
	private final JSpinner spinnerPoints3rd = new JSpinner();
	private final JLabel lb3rd = new JLabel("3rd");
	private final JSpinner spinnerPoints4th = new JSpinner();
	private final JLabel lb4th = new JLabel("4th");
	private final JPanel panelPointBreakdown = new JPanel();

	private void launchMatch()
	{
		HandyArrayList<PlayerEntity> players = selector.getSelectedPlayers();
		
		DartsMatchEntity match = factoryMatch();
		match.setPlayers(players);
		match.setGameType(gameType);
		match.setGameParams(gameParams);
		
		DartsGameScreen.launchNewMatch(match);
	}
	private DartsMatchEntity factoryMatch()
	{
		if (rdbtnFirstTo.isSelected())
		{
			int games = (int)spinnerWins.getValue();
			return DartsMatchEntity.factoryFirstTo(games);
		}
		else
		{
			int games = (int)spinnerGames.getValue();
			return DartsMatchEntity.factoryPoints(games, getPointsXml());
			
		}
	}
	private String getPointsXml()
	{
		Document doc = XmlUtil.factoryNewDocument();
		Element rootElement = doc.createElement("MatchParams");
		rootElement.setAttribute("First", "" + (int)spinnerPoints1st.getValue());
		rootElement.setAttribute("Second", "" + (int)spinnerPoints2nd.getValue());
		rootElement.setAttribute("Third", "" + (int)spinnerPoints3rd.getValue());
		rootElement.setAttribute("Fourth", "" + (int)spinnerPoints4th.getValue());
		
		doc.appendChild(rootElement);
		return XmlUtil.getStringFromDocument(doc);
		
	}
	
	@Override
	public String getScreenName()
	{
		return "Match Setup (" + GameEntity.getTypeDesc(gameType, gameParams) + ")";
	}
	
	@Override
	public EmbeddedScreen getBackTarget()
	{
		return ScreenCache.getScreen(GameSetupScreen.class);
	}

	@Override
	public void initialise(){}
	public void init(HandyArrayList<PlayerEntity> selectedPlayers, int gameType, String gameParams)
	{
		this.gameType = gameType;
		this.gameParams = gameParams;
		
		selector.init(selectedPlayers);
		
		toggleComponents();
	}
	
	private void toggleComponents()
	{
		spinnerWins.setEnabled(rdbtnFirstTo.isSelected());
		lblWins.setEnabled(rdbtnFirstTo.isSelected());
		
		spinnerGames.setEnabled(rdbtnPoints.isSelected());
		lblGames.setEnabled(rdbtnPoints.isSelected());
		lblst.setEnabled(rdbtnPoints.isSelected());
		lb2nd.setEnabled(rdbtnPoints.isSelected());
		lb3rd.setEnabled(rdbtnPoints.isSelected());
		lb4th.setEnabled(rdbtnPoints.isSelected());
		spinnerPoints1st.setEnabled(rdbtnPoints.isSelected());
		spinnerPoints2nd.setEnabled(rdbtnPoints.isSelected());
		spinnerPoints3rd.setEnabled(rdbtnPoints.isSelected());
		spinnerPoints4th.setEnabled(rdbtnPoints.isSelected());
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (panel.isEventSource(arg0))
		{
			toggleComponents();
		}
		else if (arg0.getSource() == btnLaunchMatch)
		{
			if (selector.valid())
			{
				launchMatch();
			}
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}

}
