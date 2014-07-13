package com.suning.crawler.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;


public class LeftPanel extends JPanel {

	private JLabel statusLabel;
	private JTextArea statusValueTextField;
	private JLabel speedLabel;
	private JTextArea speedValueTextField;
	private JLabel seedLabel;
	private JTextArea seedValueTextField;
	
	
	
	public LeftPanel()
	{
		intializeSizeAndBorder();
		addControls();
		
	}

	

	private void intializeSizeAndBorder() {
		// TODO Auto-generated method stub
		Dimension dim  = getPreferredSize();
		dim.width = 300;
		setPreferredSize(dim);
	
		Border inner = BorderFactory.createTitledBorder("Crawler Statistics");
		Border outer = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		
		setBorder(BorderFactory.createCompoundBorder(outer, inner));
		
	}
	
	private void addControls() {
		// TODO Auto-generated method stub
		statusLabel = new JLabel("Status: ");
		statusValueTextField = new JTextArea(2, 20);
		JScrollPane one = new JScrollPane(statusValueTextField,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		Dimension dim = statusValueTextField.getPreferredSize();
//		dim.width = 40;		
//		statusValueTextField.setPreferredSize(dim);
		
		speedLabel = new JLabel("Speed: ");
		speedValueTextField = new JTextArea(2, 20);
		JScrollPane two = new JScrollPane(speedValueTextField,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		seedLabel = new JLabel("   Seed: ");
		seedValueTextField = new  JTextArea(2, 20);
		JScrollPane three = new JScrollPane(seedValueTextField,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		SpringLayout layout = new SpringLayout();
	    setLayout(layout);
	
	    add(statusLabel);
	    add(one);
	    layout.putConstraint(SpringLayout.WEST, statusLabel, 0, SpringLayout.WEST, this);
	    layout.putConstraint(SpringLayout.NORTH, statusLabel, 25, SpringLayout.NORTH, this);
	    layout.putConstraint(SpringLayout.NORTH, one, 25, SpringLayout.NORTH, this);
	    layout.putConstraint(SpringLayout.WEST, one, 0, SpringLayout.EAST, statusLabel); 
	    
	    add(seedLabel);
	    add(three);
	    layout.putConstraint(SpringLayout.WEST, seedLabel, 0, SpringLayout.WEST, this);
	    layout.putConstraint(SpringLayout.NORTH, seedLabel, 80, SpringLayout.NORTH, this);
	    layout.putConstraint(SpringLayout.NORTH, three, 80, SpringLayout.NORTH, this);
	    layout.putConstraint(SpringLayout.WEST, three, 0, SpringLayout.EAST, seedLabel);
	    
	    add(speedLabel);
	    add(two);
	    layout.putConstraint(SpringLayout.WEST, speedLabel, 0, SpringLayout.WEST, this);
	    layout.putConstraint(SpringLayout.NORTH, speedLabel, 135, SpringLayout.NORTH, this);
	    layout.putConstraint(SpringLayout.NORTH, two, 135, SpringLayout.NORTH, this);
	    layout.putConstraint(SpringLayout.WEST, two, 0, SpringLayout.EAST, speedLabel); 
		
		
		
	}


	public JTextArea getStatusValueTextField() {
		return statusValueTextField;
	}


	public JTextArea getSpeedValueTextField() {
		return speedValueTextField;
	}


	public JTextArea getSeedValueTextField() {
		return seedValueTextField;
	}
}
