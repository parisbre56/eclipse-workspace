import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */

/**
 * @author Parisbre56
 *
 */
public class URLDialog extends Dialog {
	Shell shell;
	URL result;

	private Text txtURLin;

	public URLDialog(Shell parent) {
		super(parent, 0);
	}

	public URLDialog(Shell parent, int style) {
		super(parent, style);
	}

	public URL open() {
		result = null;
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(236, 104);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));

		Label lblName = new Label(shell, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblName.setText("URL:");

		txtURLin = new Text(shell, SWT.BORDER);
		txtURLin
				.setMessage("Type the URL you want processed here.");
		txtURLin.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1,
				1));

		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (txtURLin.getText().trim().length() != 0) {
					try {
						if(!txtURLin.getText().trim().contains("http://www.bay12forums.com/smf/index.php?topic="))
						{
							throw new MalformedURLException("Did not contain a valid Bay12 topic url.");
						}
						result = new URL(txtURLin.getText().trim());
					}
					catch (MalformedURLException e1){
						MessageBox messageBox = new MessageBox(shell,
								SWT.ICON_ERROR);
						messageBox
								.setMessage("This is not a valid URL. Valid URLs must be of the form " 
											+ "\"http://www.bay12forums.com/smf/index.php?topic=<some_topic>\""
											+ "\n\nDEBUG: " + e1.getMessage());
						messageBox.setText("Invalid URL");
						messageBox.open();
					}
				}
				shell.dispose();
			}
		});
		btnOk.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		btnOk.setText("OK");
		new Label(shell, SWT.NONE);

		shell.setDefaultButton(btnOk);
		btnOk.setFocus();
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}
}
