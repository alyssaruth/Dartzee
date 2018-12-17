package burlton.dartzee.code.screen.stats.player;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import javax.swing.border.EtchedBorder;

import burlton.dartzee.code.bean.PlayerAvatar;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.stats.GameWrapper;

import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Info panel to appear above the statistics detailing the player and the filters that are in use.
 */
public class PlayerStatisticsFilterPanel extends JPanel
									   implements ActionListener
{
	public PlayerStatisticsFilterPanel()
	{
		FlowLayout flowLayout = (FlowLayout) getLayout();
		flowLayout.setHgap(0);
		add(lblAvatar);
		panel_4.setPreferredSize(new Dimension(150, 150));
		panel_4.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		add(panel_4);
		panel_4.setLayout(new MigLayout("", "[grow]", "[][][][][][]"));
		panel_4.add(verticalStrut, "cell 0 1,alignx center");
		panel_4.add(btnFilters, "cell 0 2,alignx center,aligny top");
		lblFilterDesc.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblFilterDesc, "cell 0 3,growx,aligny top");
		label.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_4.add(label, "flowy,cell 0 0,alignx center");
		lblDateFilter.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblDateFilter, "cell 0 4");
		lblXGames.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblXGames.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblXGames, "cell 0 5,growx,aligny top");
		lblAvatar.setReadOnly(true);
		panelX.setPreferredSize(new Dimension(45, 150));
		add(panelX);
		panelX.add(btnX);
		btnX.setPreferredSize(new Dimension(40, 40));

		btnFilters.addActionListener(this);
		btnX.addActionListener(this);
	}
	
	private final PlayerAvatar lblAvatar = new PlayerAvatar();
	private final JLabel lblFilterDesc = new JLabel("No filters");
	private final JButton btnFilters = new JButton("Filters...");
	private final JPanel panel_4 = new JPanel();
	private final JLabel lblXGames = new JLabel("X Games");
	private final JLabel label = new JLabel("<Player Name>");
	private final Component verticalStrut = Box.createVerticalStrut(20);
	private final JLabel lblDateFilter = new JLabel("");
	private final JButton btnX = new JButton("X");
	private final JPanel panelX = new JPanel();
	
	private PlayerStatisticsFilterDialog dlg = null;
	
	public void init(PlayerEntity player, int gameType, boolean comparison)
	{
		dlg = new PlayerStatisticsFilterDialog(gameType);
		
		lblAvatar.init(player, false);
		label.setText(player.getName());
		
		panelX.setVisible(comparison);
		if (comparison)
		{
			label.setForeground(Color.RED);
			lblFilterDesc.setForeground(Color.RED);
			lblDateFilter.setForeground(Color.RED);
		}
		
		//Reset the filters
		dlg.resetFilters();
	}
	
	public void update(ArrayList<GameWrapper> filteredGames)
	{
		lblFilterDesc.setText(dlg.getFiltersDesc());
		lblDateFilter.setText(dlg.getDateDesc());
		lblXGames.setText(filteredGames.size() + " Games");
	}
	
	public boolean includeGame(GameWrapper game)
	{
		return dlg.includeGameBasedOnFilters(game);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		Object src = arg0.getSource();
		if (src == btnFilters)
		{
			dlg.refresh();
			dlg.setLocationRelativeTo(this);
			dlg.setVisible(true);
		}
		else if (src == btnX)
		{
			ScreenCache.getScreen(PlayerStatisticsScreen.class).removeComparison();
		}
	}
}
