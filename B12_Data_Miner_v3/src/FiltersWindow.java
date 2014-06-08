import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DisposeEvent;


public class FiltersWindow {
	public Shell filtersShell;
	private Text txtMaxretries;
	private Text txtPostsperpage;
	private Text txtNumofthreads;
	private Button btnHideModbar;
	private Button btnHideSignatures;
	private Button btnHideProfilePictures;
	private Button btnHideTitlebar;
	
	public FiltersWindow (Shell parentShell)
	{
		filtersShell = new Shell(parentShell);
		filtersShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Main.HideModbar=btnHideModbar.getSelection();
				Main.HideSignatures=btnHideSignatures.getSelection();
				Main.HideProfilePics=btnHideProfilePictures.getSelection();
				Main.HideTitlebar=btnHideTitlebar.getSelection();
				if(txtMaxretries.getText().length()!=0)
					Main.maxRetries=Integer.decode(txtMaxretries.getText());
				else
					Main.maxRetries=0;
				if(txtNumofthreads.getText().length()!=0)
					Main.numOfThreads=Integer.decode(txtNumofthreads.getText());
				else
					Main.numOfThreads=0;
				if(txtPostsperpage.getText().length()!=0)
					Main.PostsPerPage=Integer.decode(txtPostsperpage.getText());
				else
					Main.PostsPerPage=0;
			}
		});
		filtersShell.setSize(547, 358);
		filtersShell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite = new Composite(filtersShell, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setSize(filtersShell.getSize());
		
		Text lblExplanationtext = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		lblExplanationtext.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblExplanationtext.setText("Various options. <insert_more_text_here>\r\nThe checkboxes do not work properly for now, but you can change the options in the text boxes. ");
		
		Label label_0 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_0 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_label_0.widthHint = filtersShell.getSize().x - 10;
		label_0.setLayoutData(gd_label_0);
		
		btnHideModbar = new Button(composite, SWT.CHECK);
		btnHideModbar.setText("Hide Modbar");
		btnHideModbar.setSelection(Main.HideModbar);
		
		btnHideSignatures = new Button(composite, SWT.CHECK);
		btnHideSignatures.setText("Hide Signatures");
		btnHideSignatures.setSelection(Main.HideSignatures);
		new Label(composite, SWT.NONE);
		
		btnHideProfilePictures = new Button(composite, SWT.CHECK);
		btnHideProfilePictures.setText("Hide Profile Pictures");
		btnHideProfilePictures.setSelection(Main.HideProfilePics);
		
		btnHideTitlebar = new Button(composite, SWT.CHECK);
		btnHideTitlebar.setText("Hide Titlebar");
		btnHideTitlebar.setSelection(Main.HideTitlebar);
		new Label(composite, SWT.NONE);
		
		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1);
		gd_label.widthHint = filtersShell.getSize().x - 10;
		label.setLayoutData(gd_label);
		
		Text lblPostsPerPage = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		lblPostsPerPage.setText("Posts Per Page:");
		
		txtPostsperpage = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.CANCEL);
		txtPostsperpage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtPostsperpage.setText(Main.PostsPerPage.toString());
		//Ensures that only numbers are entered
		txtPostsperpage.addVerifyListener(new VerifyListener() {  
		    @Override  
		    public void verifyText(final VerifyEvent event) {  
		    	for (char c : event.text.toCharArray()) {
	                if (!Character.isDigit(c))
	                { 
	                    event.doit = false;  // disallow the action
	                    return;
	                }    
		    	}
		    }
		});
		
		Text lblExplain = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		lblExplain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		lblExplain.setText("Number of posts per output page.");
		
		Label label_1 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_label_1.widthHint = filtersShell.getSize().x - 10;
		label_1.setLayoutData(gd_label_1);
		
		Text lblMaxRetries = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		lblMaxRetries.setText("Max Retries:");
		
		txtMaxretries = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.CANCEL);
		txtMaxretries.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtMaxretries.setText(Main.maxRetries.toString());
		//Ensures that only numbers are entered
		txtMaxretries.addVerifyListener(new VerifyListener() {  
		    @Override  
		    public void verifyText(final VerifyEvent event) {  
		    	for (char c : event.text.toCharArray()) {
	                if (!Character.isDigit(c))
	                { 
	                    event.doit = false;  // disallow the action
	                    return;
	                }    
		    	}
		    }
		});
		
		Text lblExplain_1 = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		lblExplain_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		lblExplain_1.setText("How many times a downloader should try downloading before giving up.");
		
		Label label_2 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_label_2.widthHint = filtersShell.getSize().x - 10;
		label_2.setLayoutData(gd_label_2);
		
		Text lblNumberOfProcessor = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		lblNumberOfProcessor.setText("Number of Processor threads:");
		
		txtNumofthreads = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.CANCEL);
		txtNumofthreads.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtNumofthreads.setText(Main.numOfThreads.toString());
		//Ensures that only numbers are entered
		txtNumofthreads.addVerifyListener(new VerifyListener() {  
		    @Override  
		    public void verifyText(final VerifyEvent event) {  
		    	for (char c : event.text.toCharArray()) {
	                if (!Character.isDigit(c))
	                { 
	                    event.doit = false;  // disallow the action
	                    return;
	                }    
		    	}
		    }
		});
		
		Text lblExplain_2 = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		lblExplain_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		lblExplain_2.setText("The number of threads that will download data. A number close to 5 should be good. Higher numbers might increase speed but too high numbers might cause your connections to fail. Lower numbers will decrese download speed but will also decrease the chances of your connection failing. Plus, higher numbers mean higher server loads for the forums which might cause 504 errors for all.");
		
		Label label_3 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_label_3.widthHint = filtersShell.getSize().x - 10;
		label_3.setLayoutData(gd_label_3);
		
		filtersShell.open();
		filtersShell.forceActive();
	}
}
