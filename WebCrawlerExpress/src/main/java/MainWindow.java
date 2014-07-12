import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;

import com.suning.crawler.core.CrawlerController;
import com.suning.crawler.gui.LowerPanel;


public class MainWindow extends JFrame {

	
	private LowerPanel lowerPanel;
	private LeftPanel leftPanel;
	private ToolBar toolbar;
	private CrawlerController controller;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					MainWindow window = new MainWindow(new CrawlerController());
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
	public MainWindow(CrawlerController controller) {
		super("Crawler Application");
		this.controller = controller;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setBounds(100, 100, 450, 300);
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
	
		lowerPanel = new LowerPanel();
		leftPanel = new LeftPanel();
		toolbar = new ToolBar();
		add(toolbar, BorderLayout.NORTH);
		add(lowerPanel, BorderLayout.CENTER);
		add(leftPanel, BorderLayout.WEST);
		//add(rightPanel, BorderLayout.EAST);
	}

}
