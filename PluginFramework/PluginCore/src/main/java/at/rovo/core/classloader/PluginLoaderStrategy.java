package at.rovo.core.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import at.rovo.core.util.ClassFinder;
import at.rovo.core.util.IteratorEnumeration;

/**
 * <p>
 * Implements a strategy for loading plug-ins and all of their required classes
 * either as class files located directly in the plugin directory or contained
 * within a jar file.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class PluginLoaderStrategy implements IClassLoaderStrategy
{
	private static final Logger 
		LOGGER = Logger.getLogger(PluginLoaderStrategy.class.getName());
	
	/** The URL the class files could be found **/
	private URL urlClassPath = null;

	/**
	 * <p>
	 * Creates a new instance of this class.
	 * </p>
	 */
	public PluginLoaderStrategy()
	{

	}

	/**
	 * <p>
	 * Creates a new instance of this class and sets the {@link URL} the classes
	 * to load can be found.
	 * </p>
	 * 
	 * @param jarFile
	 *            The file or location the .class files can be found
	 */
	public PluginLoaderStrategy(URL jarFile)
	{
		this.urlClassPath = jarFile;
	}

	/**
	 * <p>
	 * Sets the file or location the .class files can be found in.
	 * </p>
	 * 
	 * @param newClassPath
	 *            The file or location the .class files can be found.
	 */
	public void setClassPath(URL newClassPath)
	{
		this.urlClassPath = newClassPath;
	}

	/**
	 * <p>
	 * Returns the {@link URL} of the file or directory that was specified to
	 * contains the .class files.
	 * </p>
	 * 
	 * @return The specified file or directory the .class files should be found.
	 */
	public URL getClassPath()
	{
		return urlClassPath;
	}

	@Override
	public byte[] findClassBytes(String className) throws IOException
	{
		if (className == null || className.equals(""))
			throw new IllegalArgumentException(
					"Name of class or jar-File to load is null or empty");

		byte[] classBytes = null;

		// load the class bytes from a jar file
		if (this.urlClassPath != null)
		{
			try (InputStream fis = this.findResourceAsStream(
					className.replace(".", "/")	+ ".class");)
			{
				classBytes = new byte[fis.available()];
				fis.read(classBytes);
			}
		}
		else if (className.endsWith(".class"))
		{
			// The .class-file isn't inside any jar-archive or the wrong
			// class-path was set via the constructor or setClassPath()-method
			File file = new File(className);

			if (!file.exists())
				throw new FileNotFoundException("Could not find: '"
							+ className + "'");
			
			try (FileInputStream fis = new FileInputStream(file))
			{
				classBytes = new byte[fis.available()];
				fis.read(classBytes);
			}
		}

		return classBytes;
	}

	/**
	 * <p>
	 * Converts the {@link URL} of the specified class-path to an absolute
	 * filename.
	 * </p>
	 * 
	 * @return The absolute filename corresponding to the URL specified in the
	 *         class path
	 */
	private String urlToFileName(String fileName)
	{
		// Windows and Linux differ drastically in how files have to be
		// accessed - so this is a not so beautiful workaround
		String osName = System.getProperty("os.name");

		if (osName.contains("Win"))
			// get rid of the leading 'file:/' part, afterward
			// replace / with \ and %20 with a blank space
			fileName = fileName.substring(6).replace("/", "\\").replace("%20",
					" ").replace("%2520", " ");
		else if (osName.contains("Linux") || osName.contains("Mac"))
			// Get rid of the leading 'file:' part as File-class seams to have
			// some problems with it
			fileName = fileName.substring(5);

		return fileName;
	}
	
	/**
	 * <p>
	 * Creates a URL from a given path and a resource name. The path can either
	 * be the absolute path of a jar archive or a base directory while the
	 * resource name should contain the relative path from the base directory or
	 * inside the jar archive.
	 * </p>
	 * 
	 * @param path
	 *            The absolute path of the container holding the resource. This
	 *            can either be a directory or a jar file
	 * @param name
	 *            The name of the resource including its relative path either
	 *            inside a base directory or inside an archive
	 * @return The URL of the resource
	 * 
	 * @throws MalformedURLException
	 *             If no valid URL could be generated for the given path and
	 *             resource name
	 */
	private URL createUrlOfEntry(String path, String name) throws MalformedURLException
	{
		StringBuilder url = new StringBuilder();
		int pos = 0;
		String base = path;
		if (base.contains("!"))
			base = base.substring(0, base.indexOf("!"));
		
		if (base.endsWith(".jar") || base.endsWith("zip"))
		{
			if (path.startsWith("jar:"))
			{
				// jar:...
				pos = "jar:".length();
			}
			url.append("jar:");
		}
		
		if (path.substring(pos, pos+"http://".length()).equals("http://"))
		{
			// jar:http://www... or
			// http://www...
			url.append("http://");
			pos = pos + "http://".length();
		}
		else if (path.substring(pos, pos+"https://".length()).equals("https://"))
		{
			// jar:https://www... or
			// https://www...
			url.append("https://");
			pos = pos + "https://".length();
		}
		else if (path.substring(pos, pos+"file:".length()).equals("file:"))
		{
			// jar:file:C:\\Users\\... or
			// file:C:\\Users\\...
			pos = pos + "file:".length();
			
			if (path.substring(pos,  pos+"/".length()).equals("/"))
			{
				// jar:file:/C:\\Users\\... or
				// file:/C:\\Users\\...
				pos = pos + 1;
			}
			url.append("file:/");
		}
		else if (path.subSequence(pos,  pos+"/".length()).equals("/"))
		{
			// /C:\\Users\\...
			pos = pos + 1;
			url.append("file:/");
		}
		else
		{
			// C:\\User\\...
			url.append("file:/");
		}
		
		
		// we have an archive file
		if (base.endsWith("jar") || base.endsWith("zip"))
		{
			url.append(base.substring(pos));
			
			
			url.append("!");
			if (!name.startsWith("/"))
				url.append("/");
			url.append(name);
		}
		// we have a directory
		else
		{
			if (path.endsWith("/"))
			{
				url.append(path.substring(pos, path.length()-1));
			}
			else
				url.append(path.substring(pos));
			
			if (!name.startsWith("/"))
				url.append("/");
			url.append(name);
		}
		
		LOGGER.log(Level.INFO, "Generated URL {0} of resource {1}", new Object[] { url.toString(),  });
		
		return new URL(url.toString());	
	}

	@Override
	public URL findResource(String resourceName) throws IOException
	{
		if (resourceName == null || resourceName.equals(""))
			throw new IllegalArgumentException(
					"Name of resource to load is null or empty");


		if (this.urlClassPath != null)
		{
			String fileName = this.urlToFileName(urlClassPath.toString());
			
			File file = new File(fileName);
			if (!file.exists())
				throw new FileNotFoundException("Could not find: '"
						+ urlClassPath + "'");
			
			if (file.isDirectory())
			{
				String name = fileName;
				if (!resourceName.startsWith("/") 
						|| !resourceName.startsWith("\\"))
					name = name+"/";
				File foundFile = new File(name+resourceName);
				if (foundFile != null && foundFile.exists())
					return foundFile.toURI().toURL();
				else
					throw new FileNotFoundException("Could not find requested resource: "
							+ resourceName);
			}
			else 
			{
				// we are inside a jar-File
				try(JarFile jarFile = new JarFile(file))
				{
					// lets get a reference to our .class-file
					ZipEntry zipEntry = jarFile.getEntry(resourceName);
					if (zipEntry == null)
					{
						jarFile.close();
						throw new FileNotFoundException("Could not find " 
								+ resourceName + " inside of "+urlClassPath);
					}
				}
				
				return this.createUrlOfEntry(fileName, resourceName);
			}	
		}
		else
		{
			File file = new File(resourceName);
			return file.toURI().toURL();
		}
	}
	
	@Override
	public Enumeration<URL> findResources(String resourceName) throws IOException
	{
		Set<URL> foundItems = new HashSet<>();
		
		if (resourceName == null || resourceName.equals(""))
			throw new IllegalArgumentException(
					"Name of resource to load is null or empty");

		if (this.urlClassPath != null)
		{
			String fileName = this.urlToFileName(urlClassPath.toString());
			
			File file = new File(fileName);
			if (!file.exists())
				throw new FileNotFoundException("Could not find: '"
						+ urlClassPath + "'");
			
			if (file.isDirectory())
			{
				// recursively step through the sub-directories and look for 
				// files matching the name
				List<File> foundFiles = 
						ClassFinder.findFileInDirectory(file, resourceName, true);
				
				for (File _file : foundFiles)
				{
					foundItems.add(_file.toURI().toURL());
				}
			}
			else 
			{
				// we are inside a jar-File
				List<String> foundFiles = 
						ClassFinder.scanJarFileForFiles(file, resourceName);
				for (String _file : foundFiles)
				{
					foundItems.add(this.createUrlOfEntry(fileName, _file));
				}
			}	
		}
		
		return new IteratorEnumeration<>(foundItems.iterator());
	}

	@Override
	public InputStream findResourceAsStream(String resourceName)
			throws IOException
	{
		if (this.urlClassPath != null)
		{
			String fileName = this.urlToFileName(urlClassPath.toString());

			InputStream stream = null;
			JarFile jarFile = null;
			try
			{
				File file = new File(fileName);
				if (!file.exists())
					throw new FileNotFoundException("Could not find: '"
							+ urlClassPath + "'");
				
				// check if our class path is a archive or a directory
				if (file.isDirectory())
				{
					String name = fileName;
					if (!resourceName.startsWith("/") 
							|| !resourceName.startsWith("\\"))
						name = name+"/";
					stream = new FileInputStream(name+resourceName);
					return stream;
				}
				else
				{
					// we are inside a jar-File
					jarFile = new JarFile(file);
					// lets get a reference to our .class-file
					ZipEntry zipEntry = jarFile.getEntry(resourceName);
					if (zipEntry == null)
					{
						jarFile.close();
						throw new FileNotFoundException("Could not find " 
								+ resourceName + " inside of "+urlClassPath);
					}
					// with our valid reference, we are now able to get the bytes
					// out of the jar-archive
					stream = jarFile.getInputStream(zipEntry);
					return stream;
				}
			}
			finally
			{
				if (stream != null)
				{
					try
					{
						stream.close();
					}
					catch (IOException e)
					{
						LOGGER.log(Level.SEVERE, "Couldn't close the stream of {0}!", 
								new Object[] { resourceName });
						e.printStackTrace();
					}
				}
				if (jarFile != null)
				{
					jarFile.close();
				}
			}
		}
		throw new IOException("No classpath has been specified");
	}

	@Override
	public String findLibraryPath(String libraryName)
	{
		// no native libraries currently supported by this class loader
		
		return null;
	}
}
