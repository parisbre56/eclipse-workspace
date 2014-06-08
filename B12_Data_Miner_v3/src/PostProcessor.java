import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PostProcessor {

	private Label prLabel;
	private String cookie;
	public Shell ppShell;
	private Text txtOutput;
	private ProgressBar prBar;
	public Integer threadNum; // The number of threads the app will launch to
								// download URLs
	private AtomicInteger pagesProcessed; // Thread safe integer used to update
											// the progress bar.
	private Vector<AtomicBoolean> dataTaken;
	private Integer totalPages; // Total number of pages used to update progress
								// bar. among other things
	private String threadAddress;
	private Integer firstPageNumber;
	private Integer postsPerPage; //Number of posts in an input page NOT an output page. Posts in an output page are given by Main.PostsPerPage
	private Integer lastPageNumber;
	private ConcurrentLinkedQueue<String> outQueue;
	private Vector<Vector<ThreadData>> totalData;
	private Vector<String> toDownload;
	private String inputFileText;
	private File outFile;
	private Long startTime;
	private String outFolderText;
	private File outFolder;
	private String firstPartText;
	private String lastPartText;
	private String tempS;
	private Integer NumOfPosts;
	private String outFolderTrue;
	private ProgressBar progressBar2;
	private Label prLabel2;
	private Integer currentFThread; //The number of the thread currently being processed.
	private Vector<URL> urlList;
	private Integer urlListSize;

	public PostProcessor(Shell mainShell) {
		startTime = System.currentTimeMillis();
		NumOfPosts=0;
		threadNum = Main.numOfThreads;
		totalData = new Vector<Vector<ThreadData>>();
		pagesProcessed = new AtomicInteger(0);
		currentFThread = 0;
		toDownload = new Vector<String>();
		totalPages = 1;
		outQueue = new ConcurrentLinkedQueue<String>();
		urlList = Main.urlList;
		urlListSize=urlList.size();
		firstPartText=null;
		inputFileText = Main.txtFilein.getText();
		outFile = new File(inputFileText);
		outFolderTrue = outFile.getParentFile().toURI().getPath(); //This is the folder where the output html or txt files are put.
		outFolderText = outFile.getParentFile().toURI().getPath(); //This is the folder where downloaded images are put.
		if (outFile.getName().contains(".")) {
			outFolderText += outFile.getName().substring(0,
					outFile.getName().lastIndexOf('.'));
		} else {
			outFolderText += outFile.getName();
		}
		outFolderText += '/';
		outFolder = new File(outFolderText);

		ppShell = new Shell(mainShell);
		ppShell.setText("Processing...");
		ppShell.setLayout(new GridLayout(1, false));

		txtOutput = new Text(ppShell, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP
				| SWT.V_SCROLL | SWT.MULTI);
		txtOutput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				txtOutput.setSelection(txtOutput.getCharCount()); // Makes the
																	// text
																	// autoscroll
				Main.display.update();
				/*
				 * while(Main.display.readAndDispatch()) { //Do Nothing }
				 */// Unnecessary, since the updater thread should take care of
					// that
			}
		});
		txtOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		prBar = new ProgressBar(ppShell, SWT.NONE);
		prBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		prBar.setSelection(prBar.getMinimum());
		prBar.getMaximum();
		prBar.getMinimum();
		prBar.setSelection((pagesProcessed.get() * (prBar.getMaximum() - prBar
				.getMinimum())) / totalPages);

		prLabel = new Label(ppShell, SWT.NONE);
		prLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1));
		prLabel.setText(Integer.toString(pagesProcessed.get())
				+ '/'
				+ totalPages
				+ ", "
				+ Float.toString((new Float(
						((double) pagesProcessed.get() / (double) totalPages))) * 100)
				+ '%');
		
		progressBar2 = new ProgressBar(ppShell, SWT.NONE);
		progressBar2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		progressBar2.setSelection(0);
		
		prLabel2 = new Label(ppShell, SWT.NONE);
		prLabel2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1));
		prLabel2.setText(currentFThread+"/"+urlListSize+", "+ Float.toString((new Float(
				((double) currentFThread / (double) urlListSize))) * 100) +"%");
	}

	public void process() {
		// Start a separate thread to do all the work while this one updates the
		// display...
		txtOutput.append("Launching display updater thread...\n");
		ppShell.open();
		Thread mainThread = new Thread(new Runnable() {
			@Override
			public void run() {
				newOutln("Checking if data output folder exists...");
				if (!outFolder.exists()) {
					newOutln("Creating data output folder...");
					if (!outFolder.mkdir()) {
						openMessage(
								"Unable to create data output folder \""
										+ outFolderText
										+ "\". Check if you have write permission and try again. If the problem persists, please submit a bug report.",
								"Error", true);
						return;
					}
				}
				newOutln("Done!");
				// Check for cookie
				cookie = null;
				newOut("Searching for cookie file in \"" + Main.cookiePath
						+ "\"...");
				File cookiefile = new File(Main.cookiePath);
				if (cookiefile.exists()) {
					newOutln("Found!\nAttempting to read cookie data...");
					FileReader fr;
					try {
						fr = new FileReader(cookiefile.getAbsoluteFile());
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						openMessage(
								"Unable to open input stream from cookie file \""
										+ Main.cookiePath
										+ "\". Make sure you have read permission and try again. If the problem persists, please submit a bug report.",
								"Error", true);
						return;
					}
					BufferedReader br = new BufferedReader(fr);
					try {
						cookie = br.readLine();
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
						openMessage(
								"Unable to read from cookie file \""
										+ Main.cookiePath
										+ "\". Make sure you have read permission and try again. If the problem persists, please submit a bug report.",
								"Error", true);
						return;
					}
					newOutln("Cookie data loaded and ready to use.");
				} else {
					newOutln("Cookie file not found.\nWill sign in as Guest.");
				}
				newOutln("Checking for write permission in target directory...");
				if (outFile.exists()) {
					outFile.delete();
					newOutln("Old file deleted.");
				}
				try {
					outFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
					openMessage(
							"Unable to create file \""
									+ outFile.toURI().getPath()
									+ "\". Ensure you have write permissions and try again. If the problem persists, please submit a bug report.",
							"Error", true);
					return;
				}
				newOutln("New file created.");
				BufferedWriter bw;
				try {
					FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
					bw = new BufferedWriter(fw);
				} catch (IOException e1) {
					e1.printStackTrace();
					openMessage(
							"Unable to open output stream to \""
									+ outFile.toURI().getPath()
									+ "\". Ensure you have write permissions and try again. If the problem persists, please submit a bug report.",
							"Error", true);
					return;
				}
				newOutln("Output stream opened. Ready to write to file.");
				// Test the page given...
				newOutln("Connecting to first URL from list to obtain info.");
				URL testUrl;
				testUrl = urlList.firstElement();
				newOutln("URL is: "+testUrl);
				HttpURLConnection testConn;
				try {
					testConn = (HttpURLConnection) testUrl.openConnection();
				} catch (IOException e) {
					e.printStackTrace();
					openMessage(
							"Unable to open connection to \""
									+ testUrl
									+ "\". Check your internet connection and try again. If the problem persists, please submit a bug report.",
							"Error", true);
					try {
						bw.close();
					} catch (IOException e1) {
						Main.logger.info("DEBUG: Unable to close bw after an error.");
						e1.printStackTrace();
					}
					return;
				}
				if (cookie != null) {
					testConn.setRequestProperty("Cookie", cookie);
				}
				newOut("Connection established, downloading...");
				BufferedReader br;
				tempS = null;
				StringBuilder tempB = new StringBuilder();
				try {
					br = new BufferedReader(new InputStreamReader(testConn
							.getInputStream()));
					while ((tempS = br.readLine()) != null) {
						tempB.append(tempS + '\n');
					}
					tempS = tempB.toString();
				} catch (IOException e) {
					e.printStackTrace();
					openMessage("Input error while downloading data.", "Error", true);
					return;
				}
				newOutln("Done!");
				if (cookie != null) {
					if (tempS
							.contains("\n\t\t\t<li id=\"name\">Welcome, <em>Guest</em></li>\n")) {
						newOutln("Unable to login with current cookie. If the problem persists, please submit a bug report.\nWill now continue as Guest...");
						openMessage(
								"Unable to login with current cookie. If the problem persists, please submit a bug report.\n\nWill now continue as Guest...",
								"Warning", false);
					} else if (tempS
							.contains("\n\t\t\t<li id=\"name\">Hello <em>")) {
						String tempUsername = tempS.substring(
								tempS.indexOf("\n\t\t\t<li id=\"name\">Hello <em>")
										+ "\n\t\t\t<li id=\"name\">Hello <em>"
												.length(),
								tempS.indexOf(
										"</em></li>",
										tempS.indexOf("\n\t\t\t<li id=\"name\">Hello <em>")));
						newOutln("Logged in successfully. Username: \""
								+ tempUsername + '"');
					} else {
						newOutln("Unsure if login was successful. Please submit a bug report.");
						openMessage(
								"Unsure if login was successful. Please submit a bug report.",
								"Warning", false);
					}
				}
				if (!tempS.contains("\n\t<link rel=\"canonical\" href=\"")) {
					newOutln("This is not a valid thread. Exiting...");
					openMessage(
							"This is not a valid thread. Exiting...\nIf you think this is an error, please submit a bug report.",
							"Error", true);
					try {
						bw.close();
					} catch (IOException e1) {
						Main.logger.info("DEBUG: Unable to close bw after an error.");
						e1.printStackTrace();
					}
					return;
				}
				threadAddress = tempS.substring(tempS
						.indexOf("\n\t<link rel=\"canonical\" href=\"")
						+ "\n\t<link rel=\"canonical\" href=\"".length());
				threadAddress = threadAddress.substring(0,
						threadAddress.indexOf("\" />"));
				newOutln("Canonical thread address is: \"" + threadAddress
						+ '"');
				firstPageNumber = Integer.decode(threadAddress
						.substring(threadAddress.lastIndexOf('.') + 1));
				newOutln("First page first post number is: "
						+ Integer.toString(firstPageNumber));
				threadAddress = threadAddress.substring(0,
						threadAddress.lastIndexOf('.'));
				postsPerPage = Integer.decode(tempS.substring(
						tempS.indexOf(
								'.',
								tempS.indexOf("</strong>] <a class=\"navPages\" href=\"http://www.bay12forums.com/smf/index.php?topic=")
										+ "</strong>] <a class=\"navPages\" href=\"http://www.bay12forums.com/smf/index.php?topic="
												.length()) + 1,
						tempS.indexOf(
								'"',
								tempS.indexOf("</strong>] <a class=\"navPages\" href=\"http://www.bay12forums.com/smf/index.php?topic=")
										+ "</strong>] <a class=\"navPages\" href=\"http://www.bay12forums.com/smf/index.php?topic="
												.length())))
						- firstPageNumber;
				newOutln("There are " + Integer.toString(postsPerPage)
						+ " posts per page");
				
				
				newOutln("Starting to process the URL list...");
				
				
				
				for(currentFThread=0;currentFThread<urlListSize;++currentFThread)
				{
					pagesProcessed.set(0);
					Vector<ThreadData> tempData = processUrlItem(urlList.get(currentFThread));
					if(tempData!=null)
					{
						totalData.add(tempData);
					}
					else
					{
						try {
							bw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
					}
				}
				
				
				
				if(toDownload.size()>0)
				{
					//Start downloading images and other external data
					dataTaken = new Vector<AtomicBoolean>();
					for (Integer i = 0; i < toDownload.size(); ++i) {
						dataTaken.add(new AtomicBoolean(false));
					}
					Integer tempPages = totalPages;
					pagesProcessed.set(0);
					totalPages = toDownload.size();
					for (Integer i = 0; i < threadNum; ++i) {
						Thread tempThread = new Thread(new Runnable() {
							@Override
							public void run() {
								newOutln("Starting "
										+ Thread.currentThread().getName());
								Integer retries = 0;
								while (true) { // Keep doing this until there's
												// nothing left to process
									Integer dataPos = 0;
									// Find an not-taken address and start
									// processing it
									for (AtomicBoolean b : dataTaken) {
										if (b.compareAndSet(false, true)) {
											break;
										}
										++dataPos;
									}
									// If there are no addresses left, return
									if (dataPos == dataTaken.size()) {
										break;
									}
									// Establish a connection
									newOutln("Connecting to \""
											+ toDownload.get(dataPos) + "\"");
									URL url;
									try {
										url = new URL(toDownload.get(dataPos));
									} catch (MalformedURLException e) {
										e.printStackTrace();
										dataTaken.get(dataPos).set(false);
										Main.logger.info("DEBUG: Thread \""
														+ Thread.currentThread()
																.getName()
														+ "\" stopped due to a malformed url exception.");
										newOutln("DEBUG: Thread \""
												+ Thread.currentThread().getName()
												+ "\" stopped due to a malformed url exception.");
										return;
									}
									ReadableByteChannel rbc;
									FileOutputStream fos;
									try {
										rbc = Channels.newChannel(url.openStream());
									} catch (IOException e) {
										e.printStackTrace();
										dataTaken.get(dataPos).set(false);
										newOutln("Unable to establish connection to \""
												+ toDownload.get(dataPos)
												+ "\". Retrying...");
										++retries;
										if (retries > Main.maxRetries) {
											newOutln("Reached max number of retries. Giving up on downloading "
													+ toDownload.get(dataPos));
											if (dataTaken.get(dataPos)
													.compareAndSet(false, true)) {
												pagesProcessed.incrementAndGet();
											}
										}
										continue;
									}
									Integer tempIndex=toDownload.get(dataPos).lastIndexOf('.');
									File tempfile;
									if(toDownload.get(dataPos).lastIndexOf('/')>tempIndex)
									{
										tempIndex=toDownload.get(dataPos).lastIndexOf('/');
										tempfile = new File(outFolderText
												+ dataPos);
									}
									else
									{
										tempfile = new File(outFolderText
											+ dataPos
											+ toDownload.get(dataPos).substring(tempIndex));
									}
									if (tempfile.exists()) {
										tempfile.delete();
									}
									try {
										fos = new FileOutputStream(tempfile);
									} catch (FileNotFoundException e) {
										e.printStackTrace();
										dataTaken.get(dataPos).set(false);
										newOutln("Unable to open output stream to \""
												+ tempfile.toURI().getPath() + '"');
										return;
									}
									try {
										fos.getChannel().transferFrom(rbc, 0,
												Long.MAX_VALUE);
									} catch (IOException e) {
										e.printStackTrace();
										dataTaken.get(dataPos).set(false);
										newOutln("Unable to download data from \""
												+ toDownload.get(dataPos)
												+ "\". Retrying...");
										++retries;
										if (retries > Main.maxRetries) {
											newOutln("Reached max number of retries. Giving up on downloading "
													+ toDownload.get(dataPos));
											if (dataTaken.get(dataPos)
													.compareAndSet(false, true)) {
												pagesProcessed.incrementAndGet();
												retries=0;
											}
										}
										continue;
									}
									while (true) {
										try {
											fos.close();
										} catch (IOException e) {
											e.printStackTrace();
											dataTaken.get(dataPos).set(false);
											newOutln("Unable to close output stream to\""
													+ tempfile.toURI().getPath()
													+ "\", retrying...");
											++retries;
											if (retries > Main.maxRetries) {
												newOutln("Reached max number of retries. Giving up on closing output stream to "
														+ tempfile.toURI()
																.getPath());
												if (dataTaken.get(dataPos)
														.compareAndSet(false, true)) {
													pagesProcessed
															.incrementAndGet();
												}
												break;
											}
											continue;
										}
										break;
									}
									retries = 0;
									pagesProcessed.incrementAndGet();
								}
								newOutln(Thread.currentThread().getName()
										+ " has finished downloading data");
							}
						});
						tempThread.setName("Data Downloader Thread "
								+ Integer.toString(i));
						tempThread.start();
					}
					while (pagesProcessed.get() < totalPages) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
							Main.logger.info("DEBUG: Main processor thread received interrupt.");
						}
					}
					totalPages = tempPages;
				}
				
				
				
				
				
				pagesProcessed.set(0);
				totalPages=NumOfPosts;
				currentFThread=urlListSize-urlList.size();
				newOutln("Writing data to file \"" + inputFileText + "\"...");
				if(!outFile.toURI().getPath().contains(".html")) {
					//If this is a text file...
					try {
						pagesProcessed.set(0);
						bw.write(firstPartText);
						while((tempS=getMostRecentText())!=null)
						{
							bw.write("\n"+tempS);
							pagesProcessed.incrementAndGet();
							currentFThread=urlListSize-urlList.size();
						}
						bw.write(lastPartText);
					} catch (IOException e1) {
						e1.printStackTrace();
						openMessage(
								"Unable to write data to file \""
										+ inputFileText
										+ "\". Check if you have write permission and try again. If the problm persists, please submit a bug report."
										+ "\n DEBUG:" + e1.getMessage(),
								"Error", true);
						while (true) {
							Integer i = 0;
							try {
								bw.flush();
								bw.close();
							} catch (IOException e) {
								e.printStackTrace();
								if (++i == 3) {
									openMessage(
											"Unable to close the output stream. Please submit a bug report.",
											"Error", true);
									return;
								}
								newOutln("Error closing output stream. Retrying...");
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e2) {
									e2.printStackTrace();
									Main.logger.info("DEBUG: Main processor thread received interrupt.");
								}
								continue;
							}
							break;
						}
						return;
					} catch (ParseException e) {
						e.printStackTrace();
						openMessage(
								"Unable to properly parse the date, please submit a bug report."
										+ "\n DEBUG:" + e.getMessage(),
								"Error", true);
						while (true) {
							Integer i = 0;
							try {
								bw.flush();
								bw.close();
							} catch (IOException e1) {
								e.printStackTrace();
								if (++i == 3) {
									openMessage(
											"Unable to close the output stream. Please submit a bug report.",
											"Error", true);
									return;
								}
								newOutln("Error closing output stream. Retrying...");
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e2) {
									e2.printStackTrace();
									Main.logger.info("DEBUG: Main processor thread received interrupt.");
								}
								continue;
							}
							break;
						}
						return;
					}
					while (true) {
						Integer i = 0;
						try {
							bw.flush();
							bw.close();
						} catch (IOException e) {
							e.printStackTrace();
							if (++i == 3) {
								openMessage(
										"Unable to close the output stream. Please submit a bug report.",
										"Error", true);
								return;
							}
							newOutln("Error closing output stream. Retrying...");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
								Main.logger.info("DEBUG: Main processor thread received interrupt.");
							}
							continue;
						}
						break;
					}
				}
				else
				{
					//If this is an HTML file
					currentFThread=0;
					urlListSize=NumOfPosts;
					
					try {
						bw.close(); //We won't be needing that other file...
						outFile.delete(); //Your services are no longer required...
					} catch (IOException e) {
						openMessage("Error closing test file. Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
						e.printStackTrace();
						return;
					}
					
					//Initialize some vars...
					Integer outfileNum=0;
					if(((int) roundUp(NumOfPosts, Main.PostsPerPage))==0)
					{
						openMessage("Error should never happen 1. Send a bug report.", "Impossible Error", true);
						return;
					}
					else
					{
						totalPages=(int) roundUp(NumOfPosts, Main.PostsPerPage);
					}
					
					String inputFileTextClean=outFile.getName().substring(0,
							outFile.getName().lastIndexOf('.'));
					
					
					newOutln("Splitting output to "+Long.toString(totalPages)+" files.");
					
					for(outfileNum=0;outfileNum<totalPages;++outfileNum)
					{
						//Initialize some vars for the loop
						String tempFirstPart;//Holds the modified first part.
						String tempLastPart;//Holds the modified last part.
						String pageString;//Used for navigating pages
						Integer postsWritten=0;//Number of posts written in an output file. Used for switching to the next page.
						
						
						
						//Make sure the output file exists and is open and ready to write...
						File tempOutFile = new File(outFolderTrue + inputFileTextClean + "_" + outfileNum + ".html");
						newOutln("Opening output file \""+tempOutFile.toURI().getPath()+"\"...");
						if (tempOutFile.exists()) {
							tempOutFile.delete();
							newOutln("Old file deleted.");
						}
						try {
							tempOutFile.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
							openMessage(
									"Unable to create file \""
											+ tempOutFile.toURI().getPath()
											+ "\". Ensure you have write permissions and try again. If the problem persists, please submit a bug report.",
									"Error", true);
							return;
						}
						try {
							FileWriter fw = new FileWriter(tempOutFile.getAbsoluteFile());
							bw = new BufferedWriter(fw);
						} catch (IOException e1) {
							e1.printStackTrace();
							openMessage(
									"Unable to open output stream to \""
											+ tempOutFile.toURI().getPath()
											+ "\". Ensure you have write permissions and try again. If the problem persists, please submit a bug report.",
									"Error", true);
							return;
						}
						newOutln("Output stream opened. Ready to write to file \""+tempOutFile.toURI().getPath()+"\".");
						
						
						pageString = "[<strong>"+outfileNum.toString()+"</strong>]";
		
						if(outfileNum-1>=0)
						{
							//Put the previous page num
							pageString = "<a class=\"navPages\" href=\""
									+ inputFileTextClean + "_" + (outfileNum-1) +".html" 
									+"\">"+ (outfileNum-1) +"</a> " + pageString;
						}
						if(outfileNum-2>=0)
						{
							//Put the second previous page num
							pageString = "<a class=\"navPages\" href=\""
									+ inputFileTextClean + "_" + (outfileNum-2) +".html" 
									+"\">"+ (outfileNum-2) +"</a> " + pageString;
						}
						if(outfileNum>=4)
						{
							//Put the three dots trick after the first part.
							pageString = " <span onmouseover=\"this.style.cursor = 'pointer';\" onclick=\"expandPages(this,'"
									+ inputFileTextClean + "_%1$d.html',1,"+ (outfileNum-2) +",1);\" style=\"font-weight: bold; cursor: pointer;\">...</span> "
									+ pageString;
						}
						if(outfileNum>=3)
						{
							//Put the first page first.
							pageString = " <a class=\"navPages\" href=\""
									+ inputFileTextClean + "_0.html" 
									+"\">0</a>" + pageString;
						}
						if(outfileNum+1<=totalPages-1)
						{
							//Next page
							pageString = pageString
									+" <a class=\"navPages\" href=\""
									+ inputFileTextClean + "_" + (outfileNum+1) +".html" 
									+"\">"+ (outfileNum+1) +"</a>";
						}
						if(outfileNum+2<=totalPages-1)
						{
							//Page after next page
							pageString = pageString
									+" <a class=\"navPages\" href=\""
									+ inputFileTextClean + "_" + (outfileNum+2) +".html" 
									+"\">"+ (outfileNum+2) +"</a>";
						}
						if(outfileNum<=totalPages-5)
						{
							//Put the three dots trick after this part.
							pageString = pageString
									+" <span onmouseover=\"this.style.cursor = 'pointer';\" onclick=\"expandPages(this,'"
									+ inputFileTextClean + "_%1$d.html',"+ (outfileNum+3) +","+ (totalPages-1) +",1);\" style=\"font-weight: bold; cursor: pointer;\">...</span>";
						}
						if(outfileNum<=totalPages-4)
						{
							pageString = pageString
									+" <a class=\"navPages\" href=\""
									+ inputFileTextClean + "_" + (totalPages-1) +".html" 
									+"\">"+ (totalPages-1) +"</a>";
						}
						//We have removed a div near the endline while editing. Put it back in.
						pageString = " " + pageString + "</div>";
						
						
						
						//Change first and last part...
						tempFirstPart=firstPartText.substring(0,
								firstPartText.indexOf("Pages:")+"Pages:".length())
								+ pageString
								+ firstPartText.substring(firstPartText.indexOf("\n", firstPartText.indexOf("Pages:")+"Pages:".length()));
						tempLastPart=lastPartText.substring(0,
								lastPartText.indexOf("Pages:")+"Pages:".length())
								+ pageString
								+ lastPartText.substring(lastPartText.indexOf("\n", lastPartText.indexOf("Pages:")+"Pages:".length()));
						
						//Write first part to outFile
						try {
							bw.write(tempFirstPart);
						} catch (IOException e) {
							openMessage("Error writing to output file \""+tempOutFile.toURI().getPath()+"\". Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
							e.printStackTrace();
							try {
								bw.close();
							} catch (IOException e1) {
								openMessage("Error closing \""+tempOutFile.toURI().getPath()+"\". Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
								tempOutFile.toURI().getPath();
								e1.printStackTrace();
							}
							return;
						}

						//Get Main.PostsPerPage posts and put them in the outfile.
						while(true)
						{	
							try {
								if((tempS=getMostRecentHTML())!=null) {
									bw.write("\n"+tempS);
									currentFThread++;
									postsWritten++;
								}
								else
								{
									break;
								}
							} catch (IOException e) {
								openMessage("Error writing to output file \""+tempOutFile.toURI().getPath()+"\". Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
								e.printStackTrace();
								try {
									bw.close();
								} catch (IOException e1) {
									openMessage("Error closing \""+tempOutFile.toURI().getPath()+"\". Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
									tempOutFile.toURI().getPath();
									e1.printStackTrace();
								}
								return;
							} catch (ParseException e) {
								openMessage("Error parsing date. Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
								e.printStackTrace();
								try {
									bw.close();
								} catch (IOException e1) {
									openMessage("Error closing \""+tempOutFile.toURI().getPath()+"\". Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
									tempOutFile.toURI().getPath();
									e1.printStackTrace();
								}
								return;
							}
							
							if(postsWritten>=Main.PostsPerPage)
							{
								break;
							}
						}
						
						//Write last part to outFile and close it.
						try {
							bw.write(tempLastPart);
							bw.close();
						} catch (IOException e) {
							openMessage("Error writing to output file \""+tempOutFile.toURI().getPath()+"\". Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
							e.printStackTrace();
							try {
								bw.close();
							} catch (IOException e1) {
								openMessage("Error closing \""+tempOutFile.toURI().getPath()+"\". Please submit a bug report.\nDEBUG: "+e.getMessage(), "Error", true);
								tempOutFile.toURI().getPath();
								e1.printStackTrace();
							}
							return;
						}
						
						pagesProcessed.incrementAndGet();//Update progress bar.
					}
	
				}
				
				newOutln("Runtime: "
						+ Float.toString((new Float(System.currentTimeMillis()
								- startTime))
								/ new Float(1000)) + " seconds");
				openMessage(
						"Processing completed succesfully. Output written at \""
								+ inputFileText + '"', "Completed Succesfully",
						true);
			}
		});
		mainThread.setName("Main Processor thread");
		mainThread.start();
		while (!ppShell.isDisposed()) {
			if (outQueue.size() > 0) {
				txtOutput.append(outQueue.remove());
			}
			if (!Main.display.readAndDispatch()) {
				// TODO Add here anything that needs to be updated.
				Float tempPP = new Float(pagesProcessed.get());
				prBar.setSelection(((tempPP.intValue() * (prBar.getMaximum() - prBar
						.getMinimum())) / totalPages));
				prLabel.setText(Integer.toString(tempPP.intValue())
						+ '/'
						+ totalPages
						+ ", "
						+ Float.toString((tempPP / new Float(
								(double) totalPages)) * 100) + '%');
				progressBar2.setSelection((int)	(((new Float(
						((double) currentFThread / (double) urlListSize))) * (prBar.getMaximum() - prBar
								.getMinimum()))+((tempPP / new Float(
								(double) totalPages)))*((prBar.getMaximum() - prBar
										.getMinimum())/urlListSize)));
				prLabel2.setText(currentFThread+"/"+urlListSize+", "+ Float.toString(((new Float(
						((double) currentFThread / (double) urlListSize))) * 100)+((tempPP / new Float(
								(double) totalPages)))*(100/urlListSize)) +"%");
				Main.display.sleep();
			}
		}
	}

	protected String getMostRecentHTML() throws IOException, ParseException {
		String dataString=null;
		String retString=null;
		int strIndex=-1;
		int oldestIndex = -1;
		Date oldestDate = new Date(Long.MAX_VALUE);
		String timeString=null;
		Date tempDate=null;
		Vector<ThreadData> vec=null;
		
		int vectorIndex = 0;
		try {
			while(vectorIndex<totalData.size())
			{
				vec = totalData.get(vectorIndex);
				if (vec.size()<=0)
				{
					//If this URL data container is empty, remove it
					totalData.remove(vectorIndex);
					continue;
				}
				
				dataString=vec.firstElement().getData();
				strIndex=dataString.indexOf("<strong>Reply #",strIndex);
				strIndex=dataString.indexOf("on:</strong>",strIndex);
				if(strIndex<0)
				{
					//If this page data container is empty, remove it.
					vec.firstElement().deleteFile();
					vec.remove(0);
					totalData.set(vectorIndex, vec);
					continue;
				}
				strIndex=strIndex+"on:</strong>".length();
				
				timeString=dataString.substring(strIndex, dataString.indexOf("</div>", strIndex));
				timeString=timeString.replace("\t", "");
				timeString=timeString.replace("\n", "");
				timeString=timeString.replace("&", "");
				timeString=timeString.replace("#187;", "");
				timeString=timeString.replace(",", "");
				try {
					tempDate=new SimpleDateFormat("EEE dd-MM-yyyy HH:mm:ss",Locale.ENGLISH).parse(timeString.trim());
				} catch (ParseException e) {
					e.printStackTrace();
					Main.logger.severe("ERROR parsing the date in the getMostRecentHTML function.");
				    throw new ParseException(e.getMessage()+"\n ERROR parsing the date in the getMostRecentHTML function.", e.getErrorOffset());
				}
				if(tempDate.before(oldestDate))
				{
					//If this is older, keep the vector index.
					oldestDate=tempDate;
					oldestIndex=vectorIndex;
				}
				
				++vectorIndex;
			}
		
			if(oldestIndex<0)
			{
				//If all are empty, return null and finish.
				return null;
			}
			
			//Find the retString
			vec=totalData.get(oldestIndex);
			dataString=vec.firstElement().getData();
			strIndex=dataString.indexOf("\t\t\t\t</div>\n\t\t\t</div>\n\t\t</div>");
			if(strIndex<0)
			{
				//If this is the end of file
				retString=dataString;
				vec.firstElement().deleteFile();
				vec.remove(0);
			}
			else
			{
				strIndex=strIndex+"\t\t\t\t</div>\n\t\t\t</div>\n\t\t</div>".length();
				retString=dataString.substring(0,strIndex);
				dataString=dataString.substring(strIndex);
				vec.firstElement().setData(dataString);
			}
			totalData.set(oldestIndex, vec);
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
			Main.logger.severe("ERROR writing data to or reading data from tempfiles in getMostRecentDate function.");
			throw new IOException(e1.getMessage()+"\n ERROR writing data to or reading data from tempfiles in getMostRecentDate function.");
		}
		 
		return retString;
	}

	String getMostRecentText() throws ParseException, IOException {
		
		String dataString=null;
		String retString=null;
		int strIndex=-1;
		int oldestIndex = -1;
		Date oldestDate = new Date(Long.MAX_VALUE);
		String timeString=null;
		Date tempDate=null;
		Vector<ThreadData> vec=null;
		
		int vectorIndex = 0;
		try {
			while(vectorIndex<totalData.size())
			{
				vec = totalData.get(vectorIndex);
				if (vec.size()<=0)
				{
					//If this URL data container is empty, remove it
					totalData.remove(vectorIndex);
					continue;
				}
				
				dataString=vec.firstElement().getData();
				strIndex=dataString.indexOf("==NEW POST==");
				strIndex=dataString.indexOf("Time:",strIndex);
				strIndex=dataString.indexOf("on:",strIndex);
				if(strIndex<0)
				{
					//If this page data container is empty, remove it.
					vec.firstElement().deleteFile();
					vec.remove(0);
					totalData.set(vectorIndex, vec);
					continue;
				}
				strIndex=strIndex+"on:".length();
				
				timeString=dataString.substring(strIndex, dataString.indexOf("\n", strIndex));
				timeString=timeString.replace("\t", "");
				timeString=timeString.replace("\n", "");
				timeString=timeString.replace("&", "");
				timeString=timeString.replace("#187;", "");
				timeString=timeString.replace("»", "");
				timeString=timeString.replace(",", "");
				try {
					tempDate=new SimpleDateFormat("EEE dd-MM-yyyy HH:mm:ss",Locale.ENGLISH).parse(timeString.trim());
				} catch (ParseException e) {
					e.printStackTrace();
					Main.logger.severe("ERROR parsing the date in the getMostRecentText function.");
				    throw new ParseException(e.getMessage()+"\n ERROR parsing the date in the getMostRecentText function.", e.getErrorOffset());
				}
				if(tempDate.before(oldestDate))
				{
					//If this is older, keep the vector index.
					oldestDate=tempDate;
					oldestIndex=vectorIndex;
				}
				
				++vectorIndex;
			}
		
			if(oldestIndex<0)
			{
				//If all are empty, return null and finish.
				return null;
			}
			
			//Find the retString
			vec=totalData.get(oldestIndex);
			dataString=vec.firstElement().getData();
			strIndex=dataString.indexOf("==NEW POST==",dataString.indexOf("==NEW POST==")+"==NEW POST==".length());
			if(strIndex<0)
			{
				//If this is the end of file
				retString=dataString.substring(dataString.indexOf("==NEW POST=="));
				vec.firstElement().deleteFile();
				vec.remove(0);
			}
			else
			{
				retString=dataString.substring(dataString.indexOf("==NEW POST=="),strIndex);
				dataString=dataString.substring(strIndex);
				vec.firstElement().setData(dataString);
			}
			totalData.set(oldestIndex, vec);
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
			Main.logger.severe("ERROR writing data to or reading data from tempfiles in getMostRecentText function.");
			throw new IOException(e1.getMessage()+"\n ERROR writing data to or reading data from tempfiles in getMostRecentText function.");
		}
		 
		return retString;
	}

	private void newOut(String text) {
		outQueue.add(text);
	}

	private void newOutln(String text) {
		outQueue.add(text + "\n");
	}

	@SuppressWarnings("unused")
	private void newOutln() {
		outQueue.add("\n");
	}
	
	public static long roundUp(long num, long divisor) {
	    int sign = (num > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
	    return sign * (abs(num) + abs(divisor) - 1) / abs(divisor);
	}

	private void openMessage(final String input, final String title,
			final Boolean disposesMain) {
		Main.display.asyncExec(new Runnable() {
			@Override
			public void run() {
				final Shell messageBox = new Shell(ppShell);
				messageBox.setLayout(new GridLayout(1, false));
				Label messageBoxLabel = new Label(messageBox, SWT.WRAP);
				messageBoxLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						true, true, 1, 1));
				messageBoxLabel.setText(input);
				messageBox.setText(title);
				Button messageBoxButton = new Button(messageBox, SWT.NONE);
				messageBoxButton.setText("OK");
				messageBoxButton.setLayoutData(new GridData(SWT.CENTER,
						SWT.CENTER, false, false, 1, 1));
				messageBoxButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						messageBox.close();
					}
				});
				messageBoxButton.setFocus();
				messageBox.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (disposesMain) {
							ppShell.close();
						}
					}
				});
				messageBox.setDefaultButton(messageBoxButton);
				messageBox.pack();
				messageBox.open();
			}
		});
	}
	
	private Vector<ThreadData> processDataNormal(Vector<ThreadData> passedData) throws IOException
	{
		Vector<ThreadData> returnData = passedData;
		Boolean processFirstPart = false;
		
		// Get the first and last part.
		newOutln("Processing downloaded data...");
		// Remember!!! "\n\t\t<div class=\"bordercolor\">\n"+1 marks the end of the beginning of a page
		// Remember!!! "\n\t\t\t<a id="stuff"></a>" can give you a post's number
		// Use "\n<a id="top"></a>/n<a id="msg4562693"></a>" for the first post
		// Remember!!! "\n\t</form>\n</div>\n<a id=\"lastPost\"></a>" marks the beginning of the end of a page
		try {
			tempS = returnData.firstElement().getData();
		} catch (IOException e2) {
			openMessage("Error reading from temp file \""+returnData.firstElement().file.toURI().getPath()+"\"\nCheck if you have read permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
			e2.printStackTrace();
			throw e2;
		}
		Integer tempSi = tempS.indexOf("<li id=\"time\" class=\"smalltext floatright\">")+"<li id=\"time\" class=\"smalltext floatright\">".length();
		String todayString=tempS.substring(tempSi,tempS.indexOf(",", tempSi));
		todayString=todayString.replace("\t", "");
		todayString=todayString.replace("\n", "");
		
		if(firstPartText==null) //If the first part data haven't been initialised yet.
		{
			processFirstPart = true;
			firstPartText = tempS.substring(
					0,
					tempS.indexOf("\n\t\t<div class=\"bordercolor\">\n") + 1);
			try {
				tempS = returnData.lastElement().getData();
			} catch (IOException e2) {
				openMessage("Error reading from temp file \""+returnData.lastElement().file.toURI().getPath()+"\"\nCheck if you have read permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
				e2.printStackTrace();
				throw e2;
			}
			lastPartText = tempS.substring(
					tempS.lastIndexOf("\n\t</form>\n</div>\n<a id=\"lastPost\"></a>") + 1,
					tempS.length());
		}
		
		// Process data
		pagesProcessed.set(0);
		for (Integer i = 0; i < returnData.size(); ++i) {
			try {
				tempS = returnData.get(i).getData();
			} catch (IOException e2) {
				openMessage("Error reading from temp file \""+returnData.get(i).file.toURI().getPath()+"\"\nCheck if you have read permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
				e2.printStackTrace();
				throw e2;
			}
			//Remove first and last part
			tempS = tempS.substring(
					tempS.indexOf("\n\t\t<div class=\"bordercolor\">\n") + 1,
					tempS.lastIndexOf("\n\t</form>\n</div>\n<a id=\"lastPost\"></a>"));
			// Need to replace that to get proper borders for the first post of each page
			tempS = tempS
					.replace(
							"\n\t\t\t<div class=\"clearfix windowbg largepadding\">",
							"\n\t\t\t<div class=\"clearfix topborder windowbg largepadding\">");
			//Start erasing the posts of users we don't want
			Integer index = 0;
			Integer nextIndex = tempS.indexOf(
					"\n\t\t<div class=\"bordercolor\">\n", index);
			do {
				nextIndex = tempS
						.indexOf(
								"\n\t\t<div class=\"bordercolor\">\n",
								index
										+ "\n\t\t<div class=\"bordercolor\">\n"
												.length());
				String nameS = tempS.substring(
						tempS.indexOf(
								"title=\"View the profile of ",
								index)
								+ "title=\"View the profile of "
										.length(),
						tempS.indexOf(
								'"',
								tempS.indexOf(
										"title=\"View the profile of ",
										index)
										+ "title=\"View the profile of "
												.length()));
				if (Main.nameList.size() > 0 && !Main.nameList.contains(nameS)) {
					if (nextIndex != -1) {
						tempS = tempS.substring(0, index)
								+ tempS.substring(nextIndex,
										tempS.length());
						nextIndex = index;
					} else {
						tempS = tempS.substring(0, index);
					}
				}
				else {
					++NumOfPosts;
				}
			} while ((index = nextIndex) != -1);
			//Find data that needs to be downloaded in this page
			index = 0;
			while ((index = tempS.indexOf("src=\"", index)) != -1) {
				index += "src=\"".length();
				Integer end = tempS.indexOf('"', index);
				String data = tempS.substring(index, end);
				String fulldata = data;
				String type = data.substring(data.lastIndexOf('.'));
				data = data.substring(0, data.lastIndexOf('.'));
				if (type.contains("?")) {
					type = type.substring(0, type.indexOf("?"));
				}
				if(fulldata.indexOf('/')>fulldata.indexOf('.')&&!data.contains(outFolderText
						.substring(outFolderText.lastIndexOf('/',
								outFolderText.length() - 2))))
				{
					data=fulldata;
				}
				else
				{
					data = data + type;
				}
				if (!data.contains(outFolderText
						.substring(outFolderText.lastIndexOf('/',
								outFolderText.length() - 2)))) {
					if ((!toDownload.contains(data))) {
						toDownload.add(data);
						String debug = outFolderText.substring(outFolderText
								.lastIndexOf('/',
										outFolderText.length() - 2))
								+ Integer.toString(toDownload.size() - 1)
								+ type;
						debug = '.' + debug;
						tempS = tempS.replace(fulldata, debug);
					} else {
						String debug = outFolderText.substring(outFolderText
								.lastIndexOf('/',
										outFolderText.length() - 2))
								+ Integer.toString(toDownload
										.indexOf(data)) + type;
						debug = '.' + debug;
						tempS = tempS.replace(fulldata, debug);
					}
				}
			}
			
			try {
				//Replace the today part of the posts.
				tempS=tempS.replace("</strong> <strong>Today</strong> at", "</strong> "+todayString);
				returnData.get(i).setData(tempS);
			} catch (IOException e) {
				e.printStackTrace();
				openMessage("Error writing to temp file \""+returnData.get(i).file.toURI().getPath()+"\"\nCheck if you have write permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
				throw e;
			}
			pagesProcessed.incrementAndGet();
		}
		
		if(processFirstPart==true)
		{
			//Find data that needs to be downloaded in the first part
			tempS = firstPartText;
			//Get the css file so that the colors get right
			String debug2 = tempS.substring(
					tempS.indexOf("href=\"") + "href=\"".length(),
					tempS.indexOf('"',
							tempS.indexOf("href=\"") + "href=\"".length()));
			String type2 = debug2.substring(debug2.lastIndexOf('.'));
			debug2 = debug2.substring(0, debug2.lastIndexOf('.'));
			if (type2.contains("?")) {
				type2 = type2.substring(0, type2.indexOf("?"));
			}
			debug2 = debug2 + type2;
			if(!toDownload.contains(debug2))
			{
				toDownload.add(debug2);
			}
			tempS = tempS.replace(
					tempS.substring(
							tempS.indexOf("href=\"") + "href=\"".length(),
							tempS.indexOf('"', tempS.indexOf("href=\"")
									+ "href=\"".length())),
					'.'
							+ outFolderText.substring(outFolderText
									.lastIndexOf('/',
											outFolderText.length() - 2))
							+ Integer.toString(toDownload.indexOf(debug2))
							+ toDownload.get(toDownload.indexOf(debug2)).substring(
									toDownload.lastElement().lastIndexOf(
											'.')));
			Integer index = 0;
			while ((index = tempS.indexOf("src=\"", index)) != -1) {
				index += "src=\"".length();
				Integer end = tempS.indexOf('"', index);
				String data = tempS.substring(index, end);
				String fulldata = data;
				String type = data.substring(data.lastIndexOf('.'));
				data = data.substring(0, data.lastIndexOf('.'));
				if (type.contains("?")) {
					type = type.substring(0, type.indexOf("?"));
				}
				data = data + type;
				if (!data.contains(outFolderText.substring(outFolderText
						.lastIndexOf('/', outFolderText.length() - 2)))) {
					if ((!toDownload.contains(data))) {
						toDownload.add(data);
						String debug = outFolderText
								.substring(outFolderText.lastIndexOf('/',
										outFolderText.length() - 2))
								+ Integer.toString(toDownload.size() - 1)
								+ type;
						debug = '.' + debug;
						tempS = tempS.replace(fulldata, debug);
					} else {
						String debug = outFolderText
								.substring(outFolderText.lastIndexOf('/',
										outFolderText.length() - 2))
								+ Integer.toString(toDownload.indexOf(data))
								+ type;
						debug = '.' + debug;
						tempS = tempS.replace(fulldata, debug);
					}
				}
			}
			firstPartText = tempS;
			//Find data that needs to be downloaded in the last part
			tempS = lastPartText;
			index = 0;
			while ((index = tempS.indexOf("src=\"", index)) != -1) {
				index += "src=\"".length();
				Integer end = tempS.indexOf('"', index);
				String data = tempS.substring(index, end);
				String fulldata = data;
				String type = data.substring(data.lastIndexOf('.'));
				data = data.substring(0, data.lastIndexOf('.'));
				if (type.contains("?")) {
					type = type.substring(0, type.indexOf("?"));
				}
				data = data + type;
				if (!data.contains(outFolderText.substring(outFolderText
						.lastIndexOf('/', outFolderText.length() - 2)))) {
					if ((!toDownload.contains(data))) {
						toDownload.add(data);
						String debug = outFolderText
								.substring(outFolderText.lastIndexOf('/',
										outFolderText.length() - 2))
								+ Integer.toString(toDownload.size() - 1)
								+ type;
						debug = '.' + debug;
						tempS = tempS.replace(fulldata, debug);
					} else {
						String debug = outFolderText
								.substring(outFolderText.lastIndexOf('/',
										outFolderText.length() - 2))
								+ Integer.toString(toDownload.indexOf(data))
								+ type;
						debug = '.' + debug;
						tempS = tempS.replace(fulldata, debug);
					}
				}
			}
			lastPartText = tempS;
		}
		
		return returnData;
	}
	
	private Vector<ThreadData> processDataText(Vector<ThreadData> passedData) throws IOException {
		Vector<ThreadData> returnData = passedData;
		
		
		// Get the first and last part.
		newOutln("Processing downloaded data...");
		// Remember!!! "\n\t\t<div class=\"bordercolor\">\n"+1 marks the end of the beginning of a page
		// Remember!!! "\n\t\t\t<a id="stuff"></a>" can give you a post's number
		// Use "\n<a id="top"></a>/n<a id="msg4562693"></a>" for the first post
		// Remember!!! "\n\t</form>\n</div>\n<a id=\"lastPost\"></a>" marks the beginning of the end of a page
		try {
			tempS = returnData.firstElement().getData();
		} catch (IOException e2) {
			openMessage("Error reading from temp file \""+returnData.firstElement().file.toURI().getPath()+"\"\nCheck if you have read permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
			e2.printStackTrace();
			throw e2;
		}
		//Get some basic info
		Integer tempSi = tempS.indexOf("<li id=\"time\" class=\"smalltext floatright\">")+"<li id=\"time\" class=\"smalltext floatright\">".length();
		String todayString=tempS.substring(tempSi,tempS.indexOf(",", tempSi));
		todayString=todayString.replace("\t", "");
		todayString=todayString.replace("\n", "");
		if(firstPartText==null) {
			firstPartText = "Time of creation: "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date());
		}
		firstPartText+= "\nThread "+ currentFThread +" Name: "+tempS.substring(tempS.indexOf("<title>")+"<title>".length(),tempS.indexOf("</title>"));
		firstPartText+= "\nFirst Page Number: "+Integer.toString(firstPageNumber);
		firstPartText+= "\nLast Page Number: "+Integer.toString(lastPageNumber);
		//We don't need anything from the last part, so keep it empty
		lastPartText = "===END OF FILE===";
		//Start processing all pages
		pagesProcessed.set(0);
		for (Integer i = 0; i < returnData.size(); ++i, pagesProcessed.incrementAndGet()) {
			String outputS="";
			try {
				tempS = returnData.get(i).getData();
			} catch (IOException e2) {
				openMessage("Error reading from temp file \""+returnData.get(i).file.toURI().getPath()+"\"\nCheck if you have read permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
				e2.printStackTrace();
				throw e2;
			}
			//Remove first and last part
			tempS = tempS.substring(
					tempS.indexOf("\n\t\t<div class=\"bordercolor\">\n") + 1,
					tempS.lastIndexOf("\n\t</form>\n</div>\n<a id=\"lastPost\"></a>"));
			//Start writing post data
			Integer index = 0;
			Integer nextIndex = tempS.indexOf("\n\t\t<div class=\"bordercolor\">\n", index);
			do {
				nextIndex = tempS.indexOf("\n\t\t<div class=\"bordercolor\">\n",index + "\n\t\t<div class=\"bordercolor\">\n".length());
				String nameS = tempS.substring(tempS.indexOf("title=\"View the profile of ",index)+ "title=\"View the profile of ".length(),tempS.indexOf('"',tempS.indexOf("title=\"View the profile of ",index)+ "title=\"View the profile of ".length()));
				if (Main.nameList.size()>0&&!Main.nameList.contains(nameS)) {
					if (nextIndex != -1) {
						tempS = tempS.substring(0, index)
								+ tempS.substring(nextIndex,
										tempS.length());
						nextIndex = index;
					} else {
						tempS = tempS.substring(0, index);
					}
				} else {
					++NumOfPosts;
					outputS+= "\n==NEW POST==";
					String titleS=tempS.substring(tempS.indexOf("rel=\"nofollow\">",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index))+"rel=\"nofollow\">".length(),
							tempS.indexOf("</a>", tempS.indexOf("rel=\"nofollow\">",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index))+"rel=\"nofollow\">".length()));
					String timeS;
					if(tempS.indexOf("<strong> on:</strong>",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index))>=0 && 
							tempS.indexOf("<strong> on:</strong>",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index))
							< tempS.indexOf("<strong>Reply ",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index)))
					{
						timeS=tempS.substring(tempS.indexOf("<strong> on:</strong>",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index))+"<strong>".length(),
								tempS.indexOf("</div>",tempS.indexOf("<strong> on:</strong>",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index))+"<strong>".length()));
						timeS="<strong>Reply #0"+timeS;
					}
					else
					{
						timeS=tempS.substring(tempS.indexOf("<strong>Reply ",tempS.indexOf("\n\t\t\t\t\t\t\t<h5 id=",index))+"<strong>".length());
						timeS=timeS.substring(0,timeS.indexOf("</div>"));
						timeS="<strong>"+timeS;
					}
					String textS=tempS.substring(tempS.indexOf(">",tempS.indexOf("<div class=",tempS.indexOf("\n\t\t\t\t\t<div class=\"post\">",index)+"\n\t\t\t\t\t<div class=\"post\">".length())+1)+1,
							tempS.indexOf("</div>\n\t\t\t\t\t</div>",tempS.indexOf(">",tempS.indexOf("<div class=",tempS.indexOf("\n\t\t\t\t\t<div class=\"post\">",index)+"\n\t\t\t\t\t<div class=\"post\">".length())+1)));
					Source source = new Source(textS);
					OutputDocument output = new OutputDocument(source);
					for(Element el : source.getAllElements("div"))
					{
						if(el.getAttributeValue("class")!=null&&el.getAttributeValue("class").equalsIgnoreCase("spoilerbody"))
						{
							output.replace(el.getStartTag(),el.getStartTag().toString()+"<br />------<br />");
							output.replace(el.getEndTag(),el.getEndTag().toString()+"<br />---Spoiler End---<br />");
						}
					}
					for(Element el : source.getAllElements("blockquote"))
					{
						output.replace(el.getStartTag(),el.getStartTag().toString()+"<br />------<br />");
						output.replace(el.getEndTag(),el.getEndTag().toString()+"<br />---Quote End---<br />");
					}
					textS=output.toString();
					textS=textS.replace("<br />", "/|:||:/=='/.,.,.Parisbre56.,.,./'==/:||:|/");
					textS=(new Source(textS)).getTextExtractor().toString();
					textS=textS.replace("/|:||:/=='/.,.,.Parisbre56.,.,./'==/:||:|/", "\r\n");
					timeS=(new Source(timeS)).getTextExtractor().toString();
					timeS=timeS.replace("Today at", todayString);
					outputS+= "\nThread: "+currentFThread;
					outputS+= "\nTitle: "+titleS;
					outputS+= "\nTime: "+timeS;
					outputS+= "\nUser: "+nameS;
					outputS+= "\n---------";
					outputS+= "\n"+textS+"\n";
				}
			} while ((index = nextIndex) != -1);
			try {
				returnData.get(i).setData(outputS);
			} catch (IOException e2) {
				openMessage("Error writing to temp file \""+returnData.get(i).file.toURI().getPath()+"\"\nCheck if you have write permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
				e2.printStackTrace();
				throw e2;
			}
		}
		
		return returnData;
	}
	

	private Vector<ThreadData> processUrlItem (URL testUrl) 
	{
		final Vector<ThreadData> returnData = new Vector<ThreadData>();
		
		dataTaken=new Vector<AtomicBoolean>();
		newOutln("Opening test conection to "+testUrl);
		HttpURLConnection testConn;
		try {
			testConn = (HttpURLConnection) testUrl.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			openMessage(
					"Unable to open connection to \""
							+ testUrl
							+ "\". Check your internet connection and try again. If the problem persists, please submit a bug report.",
					"Error", true);
			return null;
		}
		if (cookie != null) {
			testConn.setRequestProperty("Cookie", cookie);
		}
		newOut("Connection established, downloading...");
		BufferedReader br;
		tempS = null;
		StringBuilder tempB = new StringBuilder();
		try {
			br = new BufferedReader(new InputStreamReader(testConn
					.getInputStream()));
			while ((tempS = br.readLine()) != null) {
				tempB.append(tempS + '\n');
			}
			tempS = tempB.toString();
		} catch (IOException e) {
			e.printStackTrace();
			openMessage("Input error while downloading data.", "Error", true);
			return null;
		}
		newOutln("Done!");
		if (!tempS.contains("\n\t<link rel=\"canonical\" href=\"")) {
			newOutln("This is not a valid thread. Exiting...");
			openMessage(
					"This is not a valid thread. Exiting...\nIf you think this is an error, please submit a bug report.",
					"Error", true);
			return null;
		}
		threadAddress = tempS.substring(tempS
				.indexOf("\n\t<link rel=\"canonical\" href=\"")
				+ "\n\t<link rel=\"canonical\" href=\"".length());
		threadAddress = threadAddress.substring(0,
				threadAddress.indexOf("\" />"));
		newOutln("Canonical thread address is: \"" + threadAddress
				+ '"');
		firstPageNumber = Integer.decode(threadAddress
				.substring(threadAddress.lastIndexOf('.') + 1));
		newOutln("First page first post number is: "
				+ Integer.toString(firstPageNumber));
		threadAddress = threadAddress.substring(0,
				threadAddress.lastIndexOf('.'));
		
		lastPageNumber = Integer.decode(tempS.substring(
				tempS.lastIndexOf(
						'.',
						tempS.indexOf("</a> </div>\n\t<div class=\"nav floatright\">")) + 1,
				tempS.indexOf(
						'"',
						tempS.lastIndexOf(
								'.',
								tempS.indexOf("</a> </div>\n\t<div class=\"nav floatright\">")))));
		newOutln("Last page first post number is: "
				+ Integer.toString(lastPageNumber));
		totalPages = ((lastPageNumber - firstPageNumber) / postsPerPage) + 1;
		newOutln("Total number of pages that have to be processed: "
				+ Integer.toString(totalPages));
		newOutln("Prepearing to download text data...");
		for (Integer i = firstPageNumber, j = 0; i <= lastPageNumber; i = i
				+ postsPerPage, ++j) {
			try {
				returnData.add(new ThreadData(threadAddress + '.' + i, Main.parentFolder+"/"+currentFThread+"_"+Integer.toString(j)+".temp"));
			} catch (IOException e) {
				e.printStackTrace();
				openMessage("Error creating temp file \""+Main.parentFolder+"/"+currentFThread+"_"+Integer.toString(j)+".temp"+"\"\nCheck if you have write permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
				return null;
			}
			dataTaken.add(new AtomicBoolean(false));
		}
		for (Integer i = 0; i < threadNum; ++i) {
			Thread tempThread = new Thread(new Runnable() {
				@Override
				public void run() {
					newOutln(Thread.currentThread().getName()
							+ " starting...");
					while (true) { // Keep doing this until there's
									// nothing left to process
						Main.logger.info("New loop");
						Integer dataPos = 0;
						// Find an not-taken address and start
						// processing it
						for (AtomicBoolean b : dataTaken) {
							if (b.compareAndSet(false, true)) {
								break;
							}
							++dataPos;
						}
						// If there are no addresses left, return
						if (dataPos == dataTaken.size()) {
							Main.logger.info("Done");
							break;
						}
						Main.logger.info("Chose data point "+Integer.toString(dataPos));
						// Establish a connection
						URL url;
						try {
							url = new URL(
									returnData.get(dataPos).topicAddress);
						} catch (MalformedURLException e) {
							e.printStackTrace();
							dataTaken.get(dataPos).set(false);
							Main.logger.info("DEBUG: Thread \""
											+ Thread.currentThread()
													.getName()
											+ "\" stopped due to a malformed url exception.");
							return;
						}
						HttpURLConnection conn;
						BufferedReader br;
						try {
							conn = (HttpURLConnection) url
									.openConnection();
							if (cookie != null) {
								conn.setRequestProperty("Cookie",
										cookie);
							}
							br = new BufferedReader(
									new InputStreamReader(conn
											.getInputStream()));
						} catch (IOException e) {
							e.printStackTrace();
							dataTaken.get(dataPos).set(false);
							newOutln("Unable to establish connection to \""
									+ returnData.get(dataPos).topicAddress
									+ "\". Retrying...");
							continue;
						}
						newOutln("Connection established to \""
								+ returnData.get(dataPos).topicAddress
								+ "\". Downloading...");
						String temp;
						StringBuilder tempSB=new StringBuilder();
						Main.logger.info("Downloading data");
						try {
							while ((temp = br.readLine()) != null) {
								tempSB.append(temp + "\n");
							}
						} catch (IOException e) {
							e.printStackTrace();
							dataTaken.get(dataPos).set(false);
							newOutln("Connection to \""
									+ returnData.get(dataPos).topicAddress
									+ "\" failed while downloading. Retrying...");
							tempSB = new StringBuilder();
							continue;
						}
						try {
							returnData.get(dataPos).setData(tempSB.toString());
						} catch (IOException e2) {
							openMessage("Error writing to temp file \""+returnData.lastElement().file.toURI().getPath()+"\"\nCheck if you have write permissions and try again.\nIf the problem persists, please submit a bug report.", "Error", true);
							e2.printStackTrace();
							return;
						}
						Main.logger.info("Data finished downloading");
						pagesProcessed.incrementAndGet();
					}
					newOutln(Thread.currentThread().getName()
							+ " has finished downloading data.");
				}
			});
			tempThread.setName("Downloader Thread "
					+ Integer.toString(i));
			tempThread.start();
		}
		// Wait for all the threads to finish
		while (pagesProcessed.get() != totalPages) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Main.logger.info("DEBUG: Main Thread received interrupt while sleeping.");
			}
		}
		
		Vector<ThreadData> returnDataTrue = null;
		if(outFile.toURI().getPath().contains(".html")) {
			try {
				returnDataTrue=processDataNormal(returnData);
			} catch (IOException e3) {
				newOutln("IOError during execution of the \"processDataNormal()\" method.");
				e3.printStackTrace();
				return null;
			}
		}
		else
		{
			try {
				returnDataTrue=processDataText(returnData);
			} catch (IOException e3) {
				newOutln("IOError during execution of the \"processDataNormal()\" method.");
				e3.printStackTrace();
				return null;
			}
		}
		
		if (returnDataTrue==null || returnDataTrue.size()==0)
		{
			return null;
		}
		else
		{
			return returnDataTrue;
		}
	}
}
