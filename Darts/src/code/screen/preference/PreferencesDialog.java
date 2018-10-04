package code.screen.preference;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import code.screen.ScreenCache;
import code.screen.game.DartsGameScreen;
import code.utils.DartboardUtil;
import code.utils.DartsRegistry;
import util.ComponentUtil;
import util.Debug;

public final class PreferencesDialog extends JDialog
									 implements ActionListener,
									 			DartsRegistry
{
	public PreferencesDialog() 
	{
		setSize(500, 384);
		setTitle("Preferences");
		setResizable(false);
		setModal(true);
		
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("Dartboard", dartboardTab);
		
		tabbedPane.addTab("Scorer", scorerTab);
		tabbedPane.addTab("Misc", miscTab);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.add(btnOk);
		panel.add(btnRestoreDefaults);
		panel.add(btnCancel);
		
		btnOk.addActionListener(this);
		btnRestoreDefaults.addActionListener(this);
		btnCancel.addActionListener(this);
	}
	
	private final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	private final PreferencesPanelDartboard dartboardTab = new PreferencesPanelDartboard();
	private final PreferencesPanelScorer scorerTab = new PreferencesPanelScorer();
	private final PreferencesPanelMisc miscTab = new PreferencesPanelMisc();
	
	//Ok/cancel etc
	private final JButton btnOk = new JButton("Ok");
	private final JButton btnRestoreDefaults = new JButton("Restore Defaults");
	private final JButton btnCancel = new JButton("Cancel");
	
	public void init()
	{
		ArrayList<AbstractPreferencesPanel> panels = getPreferencePanels();
		for (int i=0; i<panels.size(); i++)
		{
			AbstractPreferencesPanel panel = panels.get(i);
			panel.refresh(false);
		}
	}
	
	private boolean valid()
	{
		ArrayList<AbstractPreferencesPanel> panels = getPreferencePanels();
		for (int i=0; i<panels.size(); i++)
		{
			AbstractPreferencesPanel panel = panels.get(i);
			if (!panel.valid())
			{
				tabbedPane.setSelectedComponent(panel);
				return false;
			}
		}
		
		return true;
	}
	
	private void save()
	{
		//Tell all the panels to save
		ArrayList<AbstractPreferencesPanel> panels = getPreferencePanels();
		for (int i=0; i<panels.size(); i++)
		{
			AbstractPreferencesPanel panel = panels.get(i);
			panel.save();
		}
		
		//Refresh all active screens in case we've changed appearance preferences
		DartboardUtil.resetCachedValues();
		
		ArrayList<DartsGameScreen> visibleScreens = ScreenCache.getDartsGameScreens();
		for (int i=0; i<visibleScreens.size(); i++)
		{
			DartsGameScreen gameScreen = visibleScreens.get(i);
			gameScreen.fireAppearancePreferencesChanged();
		}
	}
	
	private void resetPreferencesForSelectedTab()
	{
		Component selectedTab = tabbedPane.getSelectedComponent();
		if (!(selectedTab instanceof AbstractPreferencesPanel))
		{
			Debug.stackTrace("Called 'restore defaults' on unexpected component: " + selectedTab);
			return;
		}
		
		AbstractPreferencesPanel prefPanel = (AbstractPreferencesPanel)selectedTab;
		prefPanel.refresh(true);
	}
	
	private ArrayList<AbstractPreferencesPanel> getPreferencePanels()
	{
		return ComponentUtil.getAllChildComponentsForType(this, AbstractPreferencesPanel.class);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		Component source = (Component)arg0.getSource();
		if (source == btnOk)
		{
			if (valid())
			{
				save();
				dispose();
			}
		}
		else if (source == btnRestoreDefaults)
		{
			resetPreferencesForSelectedTab();
		}
		else if (source == btnCancel)
		{
			dispose();
		}
		else
		{
			Debug.stackTrace("Unnexpected actionPerformed");
			return;
		}
	}
}
