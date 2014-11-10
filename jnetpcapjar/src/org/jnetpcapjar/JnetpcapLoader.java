/*******************************************************************************
* Copyright (c) 2011-2012 mchr3k
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* mchr3k - initial API and implementation
*******************************************************************************/
package org.jnetpcapjar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.eclipse.jdt.internal.jarinjarloader.RsrcURLStreamHandlerFactory;

/** 
 * @author mchr3k initial API and implementation
 * @author Parisbre56 Modified SWTJar src to enable the loading of the appropriate jnetpcap jar and library
 */
public class JnetpcapLoader
{
 /** This attribute is declared in the ant build file
  * and states the main class of the application being built
  */
public static final String JNETPCAPJAR_MAIN_CLASS = "jnetpcapjar-TargetMainClass";

  private static String sTargetMainClass = null;

  public static void main(String[] args) throws Throwable
  {
    try
    {
      loadConfig();
      loadNativeLibrary();
      ClassLoader cl = getSWTClassloader();
      Thread.currentThread().setContextClassLoader(cl);
      try
      {
        try
        {
          //System.err.println("Launching UI ...");
          Class<?> c = Class.forName(sTargetMainClass, true, cl);
          Method main = c.getMethod("main", new Class[]{args.getClass()});
          main.invoke((Object)null, new Object[]{args});
        }
        catch (InvocationTargetException ex)
        {
          Throwable th = ex.getCause();
          if (th instanceof UnsatisfiedLinkError)
          {
            UnsatisfiedLinkError linkError = (UnsatisfiedLinkError)th;
            String errorMessage = "(UnsatisfiedLinkError: " + linkError.getMessage() + ")";
            String arch = getArch();
            if ("32".equals(arch))
            {
              errorMessage += "\nTry adding '-d64' to your command line arguments";
            }
            else if ("64".equals(arch))
            {
              errorMessage += "\nTry adding '-d32' to your command line arguments";
            }
            throw new JnetpcapLoadFailed(errorMessage);
          }
          else if ((th.getMessage() != null) &&
                    th.getMessage().toLowerCase().contains("invalid thread access"))
          {
            String errorMessage = "(SWTException: Invalid thread access)";
            errorMessage += "\nTry adding '-XstartOnFirstThread' to your command line arguments";
            throw new JnetpcapLoadFailed(errorMessage);
          }
          else
          {
            throw th;
          }
        }
      }
      catch (ClassNotFoundException ex)
      {
        throw new JnetpcapLoadFailed("Failed to find main class: " + sTargetMainClass);
      }
      catch (NoSuchMethodException ex)
      {
        throw new JnetpcapLoadFailed("Failed to find main method");
      }
    }
    catch (JnetpcapLoadFailed ex)
    {
      String reason = ex.getMessage();
      System.err.println("Launch failed: " + reason);
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JOptionPane.showMessageDialog(null, reason, "Launching UI Failed", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  /**
  *  This method is responsible for extracting resource files from within the .jar to the temporary directory.
  *  @param filePath The file path relative to the root directory within the .jar from which to extract the file.
  *  @return A file object to the extracted file
 * @throws JnetpcapLoadFailed If an error occurred
  **/
  public static File extract(String filePath) throws JnetpcapLoadFailed
  {
      try
      {
          File f = File.createTempFile("jnetpcapjar_", "_"+filePath);
          try (FileOutputStream resourceOS = new FileOutputStream(f)) {
	          byte[] byteArray = new byte[1024];
	          int i;
	          InputStream classIS = JnetpcapLoader.class.getClassLoader().getResourceAsStream(filePath);
	          //While the input stream has bytes
	          while ((i = classIS.read(byteArray)) > 0) 
	          {
	        	  //Write the bytes to the output stream
	              resourceOS.write(byteArray, 0, i);
	          }
	          //Close streams to prevent errors
	          classIS.close();
	          resourceOS.close();
	          return f;
          }
      }
      catch (Exception e)
      {
          throw new JnetpcapLoadFailed("Unable to extract native library"+filePath+"to temp folder from jar file");
      }
  }

  private static void loadNativeLibrary() throws IOException, JnetpcapLoadFailed {
	System.load(extract(getJnetpcapNativeLibraryName()).getCanonicalPath());
}

private static Manifest getJnetpcapLoaderManifest() throws IOException
  {
    Class<?> clazz = JnetpcapLoader.class;
    String className = clazz.getSimpleName() + ".class";
    String classPath = clazz.getResource(className).toString();
    if (!classPath.startsWith("jar"))
    {
      // Class not from JAR
      return null;
    }
	String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                            "/META-INF/MANIFEST.MF";
      return new Manifest(new URL(manifestPath).openStream());
  }

  private static void loadConfig() throws JnetpcapLoadFailed
  {
    try
    {
      Manifest m = getJnetpcapLoaderManifest();
      if (m == null)
      {
        throw new JnetpcapLoadFailed("Failed to find jnetpcapjar manifest");
      }

      Attributes mainAttributes = m.getMainAttributes();
      String mainClass = mainAttributes.getValue(JNETPCAPJAR_MAIN_CLASS);
      if (mainClass != null)
      {
        sTargetMainClass = mainClass;
      }

      if ((sTargetMainClass == null))
      {
        throw new JnetpcapLoadFailed("Failed to load jnetpcapjar config from manifest");
      }
    }
    catch (IOException ex)
    {
      throw new JnetpcapLoadFailed("Error when loading swtjar config: " + ex.getMessage());
    }
  }

  private static String getArch()
  {
    // Detect 32bit vs 64 bit
    String jvmArch = System.getProperty("os.arch").toLowerCase();
    String arch = (jvmArch.contains("64") ? "64" : "32");
    return arch;
  }

  private static String getJnetpcapJarName() throws JnetpcapLoadFailed
  {
    // Detect OS
    String osName = System.getProperty("os.name").toLowerCase();
    String jnetpcapFileNameOsPart = osName.contains("win") ? "windows" : osName
        .contains("mac") ? "osx" : osName.contains("linux")
        || osName.contains("nix") ? "linux" : "";
    if ("".equals(jnetpcapFileNameOsPart))
    {
      throw new JnetpcapLoadFailed("Unknown OS name: " + osName);
    }

    // Detect 32bit vs 64 bit
    String jnetpcapFileNameArchPart = getArch();

    // Generate final filename
    String jnetpcapJarFileName = "jnetpcap" + "_" +
                         jnetpcapFileNameOsPart + "_" +
                         jnetpcapFileNameArchPart +
                         ".jar";
    return jnetpcapJarFileName;
  }
  
  private static String getJnetpcapNativeLibraryName() throws JnetpcapLoadFailed
  {
    // Detect OS
    String osName = System.getProperty("os.name").toLowerCase();
    String jnetpcapFileNameOsPart = osName.contains("win") ? "windows" : osName
        .contains("mac") ? "osx" : osName.contains("linux")
        || osName.contains("nix") ? "linux" : "";
    String jnetpcapFileNameOsSuffixPart = osName.contains("win") ? "dll" : osName
            .contains("mac") ? "kext" : osName.contains("linux")
            || osName.contains("nix") ? "so" : "";
    if ("".equals(jnetpcapFileNameOsPart)||"".equals(jnetpcapFileNameOsSuffixPart))
    {
      throw new JnetpcapLoadFailed("Unknown OS name: " + osName);
    }

    // Detect 32bit vs 64 bit
    String jnetpcapFileNameArchPart = getArch();

    // Generate final filename
    String jnetpcapNativeLibraryFileName = "libjnetpcap" + "_" +
                         jnetpcapFileNameOsPart + "_" +
                         jnetpcapFileNameArchPart + "." +
                         jnetpcapFileNameOsSuffixPart;
    return jnetpcapNativeLibraryFileName;
  }

  private static ClassLoader getSWTClassloader() throws JnetpcapLoadFailed
  {
    String swtFileName = getJnetpcapJarName();
    try
    {
      URLClassLoader cl = (URLClassLoader)JnetpcapLoader.class.getClassLoader();
      URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(cl));
      Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addUrlMethod.setAccessible(true);

      URL swtFileUrl = new URL("rsrc:" + swtFileName);
      //System.err.println("Using SWT Jar: " + swtFileName);
      addUrlMethod.invoke(cl, swtFileUrl);

      return cl;
    }
    catch (Exception exx)
    {
      throw new JnetpcapLoadFailed(exx.getClass().getSimpleName() + ": " + exx.getMessage());
    }
  }

  private static class JnetpcapLoadFailed extends Exception
  {
    private static final long serialVersionUID = 1L;

    JnetpcapLoadFailed(String xiMessage)
    {
      super(xiMessage);
    }
  }
}
