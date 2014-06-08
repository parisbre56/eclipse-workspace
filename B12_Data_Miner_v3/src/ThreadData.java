import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ThreadData {
	public String topicAddress;
	public File file;

	public ThreadData(String address, String filename) throws IOException {
		topicAddress = address;
		file = new File(filename);
		if(file.exists())
		{
			file.delete();
		}
		file.createNewFile();
		file.deleteOnExit();
	}
	
	public String getData() throws IOException {
		FileInputStream fis=new FileInputStream(file);
		ObjectInputStream ois=new ObjectInputStream(fis);
		String temp=null;
		try {
			temp = (String) ois.readObject();
		} catch (ClassNotFoundException e) {
			//Should never happen
			Main.logger.info("Impossible error. Can't get string class from file.");
			e.printStackTrace();
			System.exit(-1);
		}
		ois.close();
		return temp;
	}
	
	public void setData(String data) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(data);
		oos.close();
	}
	
	public void deleteFile() {
		if(file.exists())
		{
			file.delete();
		}
	}
}