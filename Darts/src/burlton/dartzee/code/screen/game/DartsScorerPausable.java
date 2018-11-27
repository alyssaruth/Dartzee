package burlton.dartzee.code.screen.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public abstract class DartsScorerPausable extends DartsScorer
										  implements ActionListener
{
	private static final ImageIcon ICON_RESUME = new ImageIcon(DartsScorer.class.getResource("/buttons/resume.png"));
	private static final ImageIcon ICON_PAUSE = new ImageIcon(DartsScorer.class.getResource("/buttons/pause.png"));
	
	private GamePanelPausable<? extends DartsScorerPausable> parent = null;
	
	public DartsScorerPausable()
	{
		super();
		
		btnResume.setPreferredSize(new Dimension(30, 30));
		panelSouth.add(btnResume, BorderLayout.EAST);
		btnResume.setVisible(false);
		btnResume.setIcon(ICON_RESUME);

		btnResume.addActionListener(this);
	}
	
	private final JButton btnResume = new JButton("");

	/**
	 * Abstract Methods
	 */
	public abstract boolean playerIsFinished();
	
	public void toggleResume()
	{
		if (btnResume.getIcon() == ICON_PAUSE)
		{
			btnResume.setIcon(ICON_RESUME);
			finalisePlayerResult(finishPos);
		}
		else
		{
			btnResume.setIcon(ICON_PAUSE);
			lblResult.setText("");
			lblResult.setBackground(null);
		}
	}
	public void finalisePlayerResult(int finishPos)
	{
		this.finishPos = finishPos;
		
		if (!playerIsFinished())
		{
			lblResult.setText("Unfinished");
			btnResume.setVisible(true);
		}
		else
		{
			int dartCount = getTotalScore();
			lblResult.setText(dartCount + " Darts");
			btnResume.setVisible(false);
		}
		
		updateResultColourForPosition(finishPos);
	}
	
	@Override public void updatePlayerResult()
	{
		int dartCount = getTotalScore();
		if (dartCount == 0)
		{
			lblResult.setVisible(false);
		}
		else
		{
			lblResult.setVisible(true);
			lblResult.setText(dartCount + " Darts");
		}
	}
	
	public boolean getPaused()
	{
		return btnResume.getIcon() == ICON_RESUME;
	}
	
	public void setParent(GamePanelPausable<? extends DartsScorerPausable> parent)
	{
		this.parent = parent;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == btnResume)
		{
			if (getPaused())
			{
				parent.unpauseLastPlayer();
			}
			else
			{
				parent.pauseLastPlayer();
			}
			
			toggleResume();
		}
	}
}
