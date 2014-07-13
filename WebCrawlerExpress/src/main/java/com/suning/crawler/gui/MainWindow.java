package com.suning.crawler.gui;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.suning.crawler.core.CrawlerController;
import com.suning.crawler.helper.CrawlerStartEvent;
import com.suning.crawler.helper.ParamsListener;
import com.suning.crawler.helper.StringListener;


public class MainWindow extends JFrame {

	
	private LowerPanel lowerPanel;
	private LeftPanel leftPanel;
	private ToolBar toolbar;
	private RightPanel rightPanel;
	private CrawlerController controller;
	private String [] fileNames;
	private Thread controllerThread;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					MainWindow window = new MainWindow();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		super("Crawler Application");
		getNamesForTemplates();
		initialize();
		
	}

	private void getNamesForTemplates() {
		// TODO Auto-generated method stub
			
			String f =  this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			File directory = new File(f);
			fileNames = null;
			if(directory.isDirectory())
			{
				FilenameFilter textFilter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						String lowercaseName = name.toLowerCase();
						if (lowercaseName.contains("crawler")) {
							return true;
						} else {
							return false;
						}
					}
				};
				fileNames = directory.list(textFilter);
				for(int i=0; i< fileNames.length;i++) fileNames[i] = fileNames[i].replace(".xml", "");
				fileNames = Arrays.copyOf(fileNames, fileNames.length +1);
				fileNames[fileNames.length-1] = "   ";
				 Arrays.sort(fileNames);
				
			}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setBounds(100, 100, 450, 300);
		setSize(1000, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
	
		lowerPanel = new LowerPanel();
		leftPanel = new LeftPanel();
		toolbar = new ToolBar();
		rightPanel = new RightPanel(fileNames);
		add(toolbar, BorderLayout.NORTH);
		add(lowerPanel, BorderLayout.CENTER);
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
		
		
		rightPanel.setListener(new ParamsListener()
		{
			public void crawlerStartEventOccured(CrawlerStartEvent e)
			{
				String templateName = e.getTemplateName();
				int templateID = e.getTemplateID();
				lowerPanel.getTextArea().append("Template Name = " + templateName + "\n");
				lowerPanel.getTextArea().append("Template ID = " + templateID + "\n");
				controller = new CrawlerController(templateName, templateID);
				controller.addListener(new StringListener()
				{
					public void textEmitted(String text) {
						lowerPanel.getTextArea().append(text + "\n");	
					}
				});
				controllerThread = new Thread(controller);
				controllerThread.start();
			}
		});
	}

}
