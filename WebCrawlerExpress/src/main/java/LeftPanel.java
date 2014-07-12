import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;


public class LeftPanel extends JPanel {

	private JLabel statusLabel;
	private JTextField statusValueTextField;
	private JLabel speedLabel;
	private JTextField speedValueTextField;
	private JLabel seedLabel;
	private JTextField seedValueTextField;
	
	
	public LeftPanel()
	{
		intializeSizeAndBorder();
		addControls();
		
	}

	

	private void intializeSizeAndBorder() {
		// TODO Auto-generated method stub
		Dimension dim  = getPreferredSize();
		dim.width = 200;
		setPreferredSize(dim);
	
		Border inner = BorderFactory.createTitledBorder("Crawler Statistics");
		Border outer = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		
		setBorder(BorderFactory.createCompoundBorder(outer, inner));
		
	}
	
	private void addControls() {
		// TODO Auto-generated method stub
		statusLabel = new JLabel("Status: ");
		statusValueTextField = new JTextField(10);
		
		speedLabel = new JLabel("Speed: ");
		speedValueTextField = new JTextField(10);
		
		seedLabel = new JLabel("Seed: ");
		seedValueTextField = new JTextField(10);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.weighty = .1;
		gc.fill = GridBagConstraints.NONE;
		gc.anchor = GridBagConstraints.LINE_END;
		
		add(statusLabel,gc);
		
		gc.gridx = 1;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(statusValueTextField,gc);
		
		gc.gridx = 0;
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		add(speedLabel,gc);
		
		
		gc.gridx = 1;
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		add(speedValueTextField,gc);
		
		gc.gridx = 0;
		gc.gridy = 2;
		gc.weightx = 1;
		gc.weighty = 2;
		gc.anchor = GridBagConstraints.FIRST_LINE_END;
		add(seedLabel,gc);
		
		gc.gridx = 1;
		gc.gridy = 2;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		add(seedValueTextField,gc);
		
		
	}
}
