package at.rovo.core.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PluginLoaderStrategy implements IClassLoaderStrategy
{
	private URL urlClassPath = null;

	public PluginLoaderStrategy()
	{
		
	}
	
	public PluginLoaderStrategy(URL jarFile)
	{
		this.urlClassPath = jarFile;
	}
	
	public void setClassPath(URL newClassPath)
	{
		this.urlClassPath = newClassPath;
	}
	
	public URL getClassPath()
	{
		return urlClassPath;
	}
	
	@Override
	public byte[] findClassBytes(String className)
	{
		if (className == null || className.equals(""))
			throw new IllegalArgumentException("Name of class or jar-File to load is null or empty");
		
		byte[] classBytes = null;

		// A jar-file doesn't get loaded all by itself, URLClassLoader does a pretty job
		// therefore. First, we have to locate our jar-file by using the urlClassPath-field
		// provided by our constructor or via the setClassPath()-method. Second, after having
		// a valid reference to our jar-file we have to find our class inside the jar-archive.
		// All entries inside the archive are of type ZipEntry, together with the getInputStream
		// of JarFile we are now able to get the needed bytes out of the jar-archive
		if (this.urlClassPath != null)
		{
			// Windows and Linux differ drastically in how files have to be
			// accessed - so this is a not so beautiful workaround
			String osName = System.getProperty("os.name");
			String fileName = urlClassPath.toString();
			if (osName.contains("Win"))
				// get rid of the leading 'file:/' part, afterward 
				// replace / with \ and %20 with a blank space
				fileName = fileName.substring(6).replace("/", "\\").replace("%20", " ").replace("%2520", " ");
			else if (osName.contains("Linux"))
				// Get rid of the leading 'file:' part as File-class seams to have 
				// some problems with it
				fileName = fileName.substring(5);
			
			try
			{
				File file = new File(fileName);
				if (!file.exists())
					throw new FileNotFoundException("Could not find: '"+urlClassPath+"'");
				// we are inside a jar-File
				JarFile jarFile = new JarFile(file);
				// lets get a reference to our .class-file
				ZipEntry zipEntry = jarFile.getEntry(className.replace(".", "/")+".class");
				if (zipEntry == null)
					throw new FileNotFoundException("Could not find "+className+" inside of "+urlClassPath);
				// with our valid reference, we are now able to get the bytes out of the jar-archive
				InputStream fis = jarFile.getInputStream(zipEntry);
				classBytes = new byte[fis.available()];
				fis.read(classBytes);
				
				fis.close();
				jarFile.close();
				file = null;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else if (className.endsWith(".class"))
		{
			// The .class-file isn't inside any jar-archive or the wrong classpath was set
			// via the constructor or setClassPath()-method
			File file = new File(className);
			try
			{
				if (!file.exists())
					throw new FileNotFoundException("Could not find: '"+className+"'");
					FileInputStream fis = new FileInputStream(file);
				classBytes = new byte[fis.available()];
				fis.read(classBytes);
				
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return classBytes;
	}

	@Override
	public URL findResourceURL(String resourceName)
	{
		if (resourceName == null || resourceName.equals(""))
			throw new IllegalArgumentException("Name of resource to load is null or empty");

		URL resourceURL = null;
		
		try
		{
			if (urlClassPath != null)
			{
				
			}
			else
			{
				File file = new File(resourceName);
				resourceURL = file.toURI().toURL();		
			}
		}
		catch (IOException ioE)
		{
			ioE.printStackTrace();
		}
		
		return resourceURL;
	}

	@Override
	public Enumeration<URL> findResourcesEnum(String resourceName)
	{
		
		return null;
	}

	@Override
	public String findLibraryPath(String libraryName)
	{
		
		return null;
	}

}
