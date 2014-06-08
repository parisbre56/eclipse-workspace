import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginWindow {

	public Shell loginShell;
	private Text txtUsernamein;
	private Label lblPassword;
	private Text txtPasswordin;
	private Button btnLogin;
	private URL url;
	private HttpURLConnection connection;

	public LoginWindow(Shell parentShell) {
		loginShell = new Shell(parentShell);
		loginShell.setSize(265, 139);
		loginShell.setText("Login");
		loginShell.setLayout(new GridLayout(2, false));

		Label lblUsername = new Label(loginShell, SWT.NONE);
		lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblUsername.setText("Email:");

		txtUsernamein = new Text(loginShell, SWT.BORDER);
		txtUsernamein.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false, 1, 1));

		lblPassword = new Label(loginShell, SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblPassword.setText("Password:");

		txtPasswordin = new Text(loginShell, SWT.BORDER | SWT.PASSWORD);
		txtPasswordin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));

		btnLogin = new Button(loginShell, SWT.NONE);
		btnLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.logger.info("DEBUG: Login button pressed");
				String urlParameters;
				String username;
				String password;
				try {
					username = URLEncoder.encode(txtUsernamein.getText(),
							"UTF-8");
					password = URLEncoder.encode(txtPasswordin.getText(),
							"UTF-8");
				} catch (UnsupportedEncodingException e3) {
					e3.printStackTrace();
					MessageBox messageBox = new MessageBox(loginShell,
							SWT.ICON_ERROR);
					messageBox
							.setMessage("Unable to encode the username and password in UTF-8. Please submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
					messageBox.setText("Login Error");
					messageBox.open();
					return;
				}
				urlParameters = "user=" + username + "&passwrd=" + password
						+ "&cookieneverexp=on&hash_passwrd=";
				try {
					url = new URL(
							"http://www.bay12forums.com/smf/index.php?action=login2");
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
					MessageBox messageBox = new MessageBox(loginShell,
							SWT.ICON_ERROR);
					messageBox
							.setMessage("The server url was malformed. Please submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
					messageBox.setText("Login Error");
					messageBox.open();
					return;
				}
				try {
					connection = (HttpURLConnection) url.openConnection();
				} catch (IOException e1) {
					e1.printStackTrace();
					MessageBox messageBox = new MessageBox(loginShell,
							SWT.ICON_ERROR);
					messageBox
							.setMessage("Unable to establish a connection to the server. Please check your internet connection and try again. If the problem persists submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
					messageBox.setText("Login Error");
					messageBox.open();
					return;
				}
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setInstanceFollowRedirects(false);
				try {
					connection.setRequestMethod("POST");
				} catch (ProtocolException e2) {
					e2.printStackTrace();
					MessageBox messageBox = new MessageBox(loginShell,
							SWT.ICON_ERROR);
					messageBox
							.setMessage("Unable to set the transmit protocol to POST. Please submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
					messageBox.setText("Login Error");
					messageBox.open();
					return;
				}
				connection
						.setRequestProperty("User-Agent",
								"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:24.0) Gecko/20100101 Firefox/24.0");
				connection
						.setRequestProperty("Accept",
								"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				connection.setRequestProperty("Accept-Language",
						"en-US,en;q=0.5");
				connection.setRequestProperty("Accept-Encoding",
						"gzip, deflate");
				connection.setRequestProperty("DNT", "1");
				connection
						.setRequestProperty("Referer",
								"http://www.bay12forums.com/smf/index.php?action=login");
				connection.setRequestProperty("Connection", "keep-alive");
				connection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length",
						Integer.toString(urlParameters.getBytes().length));

				connection.setUseCaches(false);

				DataOutputStream wr;
				try {
					wr = new DataOutputStream(connection.getOutputStream());
					wr.writeBytes(urlParameters);
					wr.flush();
					wr.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					MessageBox messageBox = new MessageBox(loginShell,
							SWT.ICON_ERROR);
					messageBox
							.setMessage("Unable to transmit the request to the server. Please check your internet connection and try again. If the problem persists submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
					messageBox.setText("Login Error");
					messageBox.open();
					return;
				}
				int responseCode;
				try {
					responseCode = connection.getResponseCode();
				} catch (IOException e1) {
					e1.printStackTrace();
					MessageBox messageBox = new MessageBox(loginShell,
							SWT.ICON_ERROR);
					messageBox
							.setMessage("Unable to get a response from the server. Please check your internet connection and try again. If the problem persists submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
					messageBox.setText("Login Error");
					messageBox.open();
					return;
				}
				Main.logger.info("DEBUG: Sending 'POST' request to URL : "
						+ url);
				Main.logger.info("DEBUG: Post parameters : " + urlParameters);
				Main.logger.info("DEBUG: Response Code:" + responseCode);
				Main.logger.info("DEBUG:=======Response header follows=======");
				Map<String, List<String>> map = connection.getHeaderFields();
				String cookieData = "";
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					Main.logger.info("DEBUG: " + entry.getKey() + ": "
							+ entry.getValue());
					if (entry.getKey() != null
							&& entry.getKey().contains("Set-Cookie")) {
						cookieData = "" + entry.getValue();
					}
				}
				Main.logger.info("DEBUG:=======Response header ends==========");

				MessageBox messageBox = null;
				switch (responseCode) {
				case 302:
					cookieData = cookieData.substring(
							cookieData.indexOf("SMFCookie5="),
							cookieData.indexOf(";",
									cookieData.indexOf("SMFCookie5=")));
					Main.logger.info("DEBUG: Cookie Data: " + cookieData);
					File cookieFile = new File(Main.cookiePath);
					Main.logger.info("DEBUG: Creating cookie file");
					if (cookieFile.exists()) {
						Main.logger.info("DEBUG: Cookie file already exists. It will be overwritten by the new file.");
						cookieFile.delete();
					}
					try {
						cookieFile.createNewFile();
						FileWriter fw = new FileWriter(cookieFile
								.getAbsoluteFile());
						BufferedWriter Cwr = new BufferedWriter(fw);
						Cwr.write(cookieData);
						Cwr.flush();
						Cwr.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						messageBox = new MessageBox(loginShell, SWT.ICON_ERROR);
						messageBox
								.setMessage("Unable to create the cookie file. Please make sure you have write access to the JAR file's parent directory and try again. If the problem persists submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
						messageBox.setText("Login Error");
						messageBox.open();
						return;
					}
					messageBox = new MessageBox(loginShell,
							SWT.ICON_INFORMATION);
					messageBox
							.setMessage("Login successful. Login cookie saved at "
									+ Main.cookiePath);
					messageBox.setText("Login Successful");
					messageBox.open();
					loginShell.dispose();
					break;
				case 200:
					messageBox = new MessageBox(loginShell, SWT.ICON_ERROR);
					messageBox
							.setMessage("Unable to login. Incorrect username or password.");
					messageBox.setText("Login Error");
					messageBox.open();
					break;
				default:
					messageBox = new MessageBox(loginShell, SWT.ICON_ERROR);
					messageBox
							.setMessage("Unexpected response code given from server. Please submit a bug report at http://www.bay12forums.com/smf/index.php?topic=131917");
					messageBox.setText("Login Error");
					messageBox.open();
					break;
				}
			}
		});
		btnLogin.setText("Login");
		new Label(loginShell, SWT.NONE);

		txtUsernamein.setFocus();
		loginShell.setDefaultButton(btnLogin);
		loginShell.open();
		loginShell.forceActive();
	}
}
