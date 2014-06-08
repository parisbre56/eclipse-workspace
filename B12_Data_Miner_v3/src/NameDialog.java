import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NameDialog extends Dialog {
	Shell shell;
	String result;

	private Text txtNamein;

	public NameDialog(Shell parent) {
		super(parent, 0);
	}

	public NameDialog(Shell parent, int style) {
		super(parent, style);
	}

	public String open() {
		result = null;
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(236, 104);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));

		Label lblName = new Label(shell, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblName.setText("Name:");

		txtNamein = new Text(shell, SWT.BORDER);
		txtNamein
				.setMessage("Type the names of the user whose posts you want to  see here.");
		txtNamein.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1,
				1));

		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (txtNamein.getText().trim().length() != 0) {
					result = txtNamein.getText().trim();
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
