import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InstructionWindow {
	public Shell instructionShell;
	private Text txtInfo;

	public InstructionWindow(Shell parentShell) {
		instructionShell = new Shell(parentShell);
		instructionShell.setLayout(new FillLayout(SWT.HORIZONTAL));

		txtInfo = new Text(instructionShell, SWT.READ_ONLY | SWT.WRAP
				| SWT.V_SCROLL | SWT.MULTI);
		txtInfo.setText("Bay 12 Post Processor Version 2."
				+ Integer.toString(Main.version)
				+ " created by Parisbre56 for the Bay12 forums.\nIf you have any suggestions and/or questions write in this thread: http://www.bay12forums.com/smf/index.php?topic=131917.0\nwrite to this e-mail: parisbre56@gmail.com\nor send me a PM in the forum: http://www.bay12forums.com/smf/index.php?action=pm;sa=send;u=78376\n\nIf you login, remember to set your post per page count as high as possible to speed up te download speed.\nYour login should persist when you close and reopen the program, as long as the cookie has the same name and is in the same folder.\n\n<add more info>");

		instructionShell.open();
		instructionShell.forceActive();
	}
}
