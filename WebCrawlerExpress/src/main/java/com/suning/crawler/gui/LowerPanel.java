package com.suning.crawler.gui;

import java.awt.BorderLayout;
import java.awt.TextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class LowerPanel extends JPanel {
	
	private TextArea textArea;
	
	public LowerPanel()
	{
		textArea = new TextArea();
		setLayout(new BorderLayout());
		add(new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
	}
	
	
	public TextArea getTextArea()
	{
		return textArea;
	}

}
