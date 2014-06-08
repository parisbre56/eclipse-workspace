import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */

/**
 * @author parisbre56
 * 
 */
public class Main {

	public static Display display;
	public static Integer version;
	public static Text txtFilein;
	private static LoginWindow loginWindow;
	private static InstructionWindow instructionWindow;
	private static FiltersWindow filtersWindow;
	public static File jarFile;
	public static String parentFolder;
	public static String cookiePath;
	public static Vector<String> nameList; //Holds the names of the posters to be included.
	public static Vector<URL> urlList; //Holds the URLs of the threads that need processing.
	private static Vector<Label> nameLbl; //Holds the names of the users. Used to display them.
	private static Vector<Label> urlLbl; //Holds the URLs. Used to display them.
	private static Vector<Button> nameBtn; //Holds the buttons for removing names.
	private static Vector<Button> urlBtn; //Holds the buttons for removing URLs.
	private static Composite nameComposite; //Holds name labels and buttons for display purposes.
	private static Composite URLcomposite; //Holds URL labels and buttons for display purposes.
	private static PostProcessor postProcessor; //This does all the work
	
	//Variables for options dialog.
	public static Integer maxRetries;
	public static Boolean HideTitlebar;
	public static Boolean HideProfilePics;
	public static Boolean HideModbar;
	public static Boolean HideSignatures;
	public static Integer PostsPerPage;
	public static Integer numOfThreads;

	public static Logger logger;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		version = 7;
		
		//Variables for options dialog.
		maxRetries = 3;
		HideTitlebar=false;
		HideProfilePics=false;
		HideModbar=false;
		HideSignatures=false;
		PostsPerPage=50;
		numOfThreads=5;
		//End of variables for option dialog.

		display = new Display();
		Display.setAppName("Bay 12 Thread Downloader");
		Display.setAppVersion(Integer.toString(version));

		loginWindow = null;
		instructionWindow = null;
		
		//Variables for display and storage of names.
		nameList = new Vector<String>();
		nameLbl = new Vector<Label>();
		nameBtn = new Vector<Button>();
		
		//Variables for display and storage of URLs.
		urlList = new Vector<URL>();
		urlLbl = new Vector<Label>();
		urlBtn = new Vector<Button>();

		final Shell mainShell = new Shell(display);
		mainShell.setSize(627, 400);
		mainShell.setText("Bay 12 Post Processor V3."
				+ Integer.toString(version));
		mainShell.setLayout(new GridLayout(3, false));

		Menu menu = new Menu(mainShell, SWT.BAR);
		mainShell.setMenuBar(menu);

		MenuItem mntmStuff = new MenuItem(menu, SWT.CASCADE);
		mntmStuff.setText("Stuff");

		Menu menu_1 = new Menu(mntmStuff);
		mntmStuff.setMenu(menu_1);

		MenuItem mntmLogin = new MenuItem(menu_1, SWT.NONE);
		mntmLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.logger.info("DEBUG: Login menu item selected");
				if (loginWindow == null) {
					loginWindow = new LoginWindow(mainShell);
					mainShell.setEnabled(false);
					while (!loginWindow.loginShell.isDisposed()) {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
					Main.logger.info("DEBUG: Login window closed");
					loginWindow = null;
					mainShell.setEnabled(true);
				} else {
					Main.logger.info("DEBUG: Login window already open, ignoring.");
				}
			}
		});
		mntmLogin.setText("Login");

		MenuItem mntmFilters = new MenuItem(menu_1, SWT.NONE);
		mntmFilters.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.logger.info("DEBUG: Filters menu item selected");
				if(filtersWindow == null) {
					filtersWindow = new FiltersWindow(mainShell);
					mainShell.setEnabled(false);
					while (!filtersWindow.filtersShell.isDisposed()) {
						if(!display.readAndDispatch()) {
							display.sleep();
						}
					}
					Main.logger.info("DEBUG: Filters window closed");
					filtersWindow = null;
					mainShell.setEnabled(true);
				} else {
					Main.logger.info("DEBUG: Filters window already open, ignoring.");
				}
				// TODO Give the user some basic filters/options
			}
		});
		mntmFilters.setText("Other Options");

		MenuItem mntmInstructions = new MenuItem(menu_1, SWT.NONE);
		mntmInstructions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.logger.info("DEBUG: Information menu item selected");
				if (instructionWindow == null) {
					instructionWindow = new InstructionWindow(mainShell);
					mainShell.setEnabled(false);
					while (!instructionWindow.instructionShell.isDisposed()) {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
					Main.logger.info("DEBUG: Instruction window closed");
					instructionWindow = null;
					mainShell.setEnabled(true);
				} else {
					Main.logger.info("DEBUG: Instruction window already open, ignoring.");
				}
			}
		});
		mntmInstructions.setText("Information");
		
				Label lblOutputFile = new Label(mainShell, SWT.NONE);
				lblOutputFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
						false, 1, 1));
				lblOutputFile.setText("Output File:");
		
				txtFilein = new Text(mainShell, SWT.BORDER);
				txtFilein.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
						1, 1));
		
				Button btnBrowse = new Button(mainShell, SWT.NONE);
				btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnBrowse.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Main.logger.info("DEBUG: Browse button pressed");
						FileDialog fileDialog = new FileDialog(mainShell, SWT.SAVE);
						fileDialog.setFileName(txtFilein.getText());
						fileDialog.setText("Select output file");
						String[] filterExt = { "*.html", "*.txt" ,"*.*" };
						fileDialog.setFilterExtensions(filterExt);
						String[] filterNam = { "HTML", "Plain Text", "All" };
						fileDialog.setFilterNames(filterNam);
						fileDialog.setFilterIndex(0);
						fileDialog.setOverwrite(true);
						fileDialog.setFilterPath(txtFilein.getText());
						String input = fileDialog.open();
						if (input != null) {
							txtFilein.setText(input);
						}
					}
				});
				btnBrowse.setText("Browse");
		
				Label lblUrl = new Label(mainShell, SWT.NONE);
				lblUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
						1, 1));
				lblUrl.setText("URLs:");
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(mainShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		
		URLcomposite = new Composite(scrolledComposite, SWT.NONE);
		URLcomposite.setLayout(new GridLayout(2, false));
		scrolledComposite.setContent(URLcomposite);
		scrolledComposite.setMinSize(URLcomposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Button btnAddUrl = new Button(mainShell, SWT.NONE);
		btnAddUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddUrl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.logger.info("DEBUG: Add URL button pressed");
				URLDialog urlDialog = new URLDialog(mainShell);
				urlDialog.setText("Add new URL");
				URL newURL = urlDialog.open();
				Main.logger.info("DEBUG: Add URL dialog closed");
				if (newURL != null) {
					Main.logger.info("DEBUG: URL given: " + newURL);
					addURL(newURL);
				}
			}
		});
		btnAddUrl.setText("Add URL");
		new Label(mainShell, SWT.NONE);
		new Label(mainShell, SWT.NONE);

		Label lblNames = new Label(mainShell, SWT.NONE);
		lblNames.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblNames.setText("Names:");

		ScrolledComposite sc = new ScrolledComposite(mainShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		nameComposite = new Composite(sc, SWT.NONE);
		sc.setContent(nameComposite);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		nameComposite.setLayout(new GridLayout(2, false));

		Button btnAddName = new Button(mainShell, SWT.NONE);
		btnAddName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.logger.info("DEBUG: Add Name button pressed");
				NameDialog nameDialog = new NameDialog(mainShell);
				nameDialog.setText("Add new Username");
				String newName = nameDialog.open();
				Main.logger.info("DEBUG: Add Name dialog closed");
				if (newName != null) {
					Main.logger.info("DEBUG: Name given: " + newName);
					addName(newName);
				}
			}
		});
		btnAddName.setText("Add Name");
		new Label(mainShell, SWT.NONE);
		new Label(mainShell, SWT.NONE);
		new Label(mainShell, SWT.NONE);
		mainShell.open();

		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI().getPath());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			MessageBox messageBox = new MessageBox(mainShell, SWT.ICON_ERROR);
			messageBox
					.setMessage("Unable to obtain the jar file's location. Please submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
			messageBox.setText("Initialisation Error");
			messageBox.open();
			return;
		}
		parentFolder = new String(jarFile.getParentFile().toURI().getPath());
		cookiePath = new String(parentFolder + "B12_PostProcessor_Cookie.txt");
		if(File.separatorChar=='\\')
		{
			parentFolder=parentFolder.substring(1);
		}
		txtFilein.setText((parentFolder + "B12_Output.html").replace('/', File.separatorChar));
		new Label(mainShell, SWT.NONE);
		
		Button btnStart = new Button(mainShell, SWT.NONE);
		btnStart.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.logger.info("DEBUG: Start button pressed");
				if (postProcessor == null) {
					postProcessor = new PostProcessor(mainShell);
					mainShell.setEnabled(false);
					postProcessor.process();
					postProcessor.ppShell.dispose();
					Main.logger.info("DEBUG: Post processor window closed");
					postProcessor = null;
					mainShell.setEnabled(true);
				} else {
					Main.logger.info("DEBUG: Post processor window already open, ignoring.");
				}
			}
		});
		btnStart.setText("Start");
		
		btnStart.setFocus();
		mainShell.setDefaultButton(btnStart);

		logger = Logger.getLogger("MyLog");
		FileHandler fh;
		try {
			fh = new FileHandler(parentFolder + "B12_PostProcessor.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Some debug input, used to test. Remember to comment this before releasing. Or maybe not, could be used as an example...
		//http://www.bay12forums.com/smf/index.php?topic=108083.0
		//http://www.bay12forums.com/smf/index.php?topic=135884.0
		//http://www.bay12forums.com/smf/index.php?topic=136608.0
		//http://www.bay12forums.com/smf/index.php?topic=106279.0
		//http://www.bay12forums.com/smf/index.php?topic=136397.0
		//http://www.bay12forums.com/smf/index.php?topic=136149.0
		//http://www.bay12forums.com/smf/index.php?topic=123854.0
		//piecewise
		try {
			addURL(new URL("http://www.bay12forums.com/smf/index.php?topic=123854.0"));
			addURL(new URL("http://www.bay12forums.com/smf/index.php?topic=136149.0"));
			addURL(new URL("http://www.bay12forums.com/smf/index.php?topic=136397.0"));
			addURL(new URL("http://www.bay12forums.com/smf/index.php?topic=106279.0"));
			addURL(new URL("http://www.bay12forums.com/smf/index.php?topic=108083.0"));
			addURL(new URL("http://www.bay12forums.com/smf/index.php?topic=135884.0"));
			addURL(new URL("http://www.bay12forums.com/smf/index.php?topic=136608.0"));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			MessageBox messageBox = new MessageBox(mainShell,
					SWT.ICON_ERROR);
			messageBox
					.setMessage("Impossible Error, should never happen."
								+ "\n\nDEBUG: " + e1.getMessage());
			messageBox.setText("Invalid URL / Impossible Error");
			messageBox.open();
		}
		//addName("piecewise");
		
		//Debug code ends here.
		
		mainShell.pack(true);

		while (!mainShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				nameComposite.layout();
				nameComposite.update();
				sc.layout();
				sc.update();
				display.sleep();
			}
		}
		display.dispose();
	}
	
	
	private static void addName(String newName)
	{
		if (!nameList.contains(newName)) {
			nameList.add(newName);
			nameLbl.add(new Label(nameComposite, SWT.NONE));
			nameLbl.lastElement().setText(newName);
			nameLbl.lastElement().setLayoutData(
					new GridData(SWT.FILL, SWT.CENTER, true, false,
							1, 1));
			nameBtn.add(new Button(nameComposite, SWT.NONE));
			nameBtn.lastElement().setLayoutData(
					new GridData(SWT.RIGHT, SWT.CENTER, false,
							false, 1, 1));
			nameBtn.lastElement().setText("Remove");
			nameBtn.lastElement().addSelectionListener(
					new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							Integer index = nameBtn
									.indexOf(e.widget);
							nameList.remove(nameList.get(index));
							nameLbl.get(index).dispose();
							nameLbl.remove(nameLbl.get(index));
							nameBtn.get(index).dispose();
							nameBtn.remove(nameBtn.get(index));
							nameComposite.setSize(nameComposite
									.computeSize(SWT.DEFAULT,
											SWT.DEFAULT));
						}
					});
			nameComposite.setSize(nameComposite.computeSize(
					SWT.DEFAULT, SWT.DEFAULT));
		}
	}
	
	private static void addURL(URL newURL)
	{
		if (!urlList.contains(newURL)) {
			urlList.add(newURL);
			urlLbl.add(new Label(URLcomposite, SWT.NONE));
			urlLbl.lastElement().setText(newURL.toString());
			urlLbl.lastElement().setLayoutData(
					new GridData(SWT.FILL, SWT.CENTER, true, false,
							1, 1));
			urlBtn.add(new Button(URLcomposite, SWT.NONE));
			urlBtn.lastElement().setLayoutData(
					new GridData(SWT.RIGHT, SWT.CENTER, false,
							false, 1, 1));
			urlBtn.lastElement().setText("Remove");
			urlBtn.lastElement().addSelectionListener(
					new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							Integer index = urlBtn
									.indexOf(e.widget);
							urlList.remove(urlList.get(index));
							urlLbl.get(index).dispose();
							urlLbl.remove(urlLbl.get(index));
							urlBtn.get(index).dispose();
							urlBtn.remove(urlBtn.get(index));
							URLcomposite.setSize(URLcomposite
									.computeSize(SWT.DEFAULT,
											SWT.DEFAULT));
						}
					});
			URLcomposite.setSize(URLcomposite.computeSize(
					SWT.DEFAULT, SWT.DEFAULT));
		}
	}
}
