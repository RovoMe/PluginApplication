package at.rovo.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * <code>ClassFinder</code> finds class files inside directories or jar files
 * and provides methods to find implementing classes of an interface.
 * </p>
 * <p>
 * It manages an own class-path by allowing a user to add directories and/or jar
 * files via {@link #addToClassPath(String)} to it.
 * </p>
 * <p>
 * Further, it provides methods to find classes in directories via
 * {@link #findClassFilesInDirectory(File, boolean)} or in jar files via
 * {@link #scanJarFileForClasses(File)}.
 * </p>
 * <p>
 * Implementations of a certain interface can be found via
 * {@link #findImplementingClassesInDirectories(Class, ClassLoader)} or
 * {@link #findImplementingClassesInJarFiles(Class, ClassLoader)}. If
 * implementations should be looked for in both directories and jar files
 * {@link #findImplementingClasses(Class, ClassLoader)} can be invoked.
 * </p>
 * <p>
 * Note that before finding classes in directories or jar files, directories or
 * jar files have to be added to the class path of this instance.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public enum ClassFinder
{
	/** Application of the enum singleton pattern **/
	INSTANCE;
	
	/** A set of directories to look for classes **/
	private Set<String> directories;
	/** A set of jar files to look for classes **/
	private Set<String> jarFiles;
	
	private ClassFinder()
	{
		this.directories = new HashSet<>();
		this.jarFiles = new HashSet<>();
		StringTokenizer tokenizer = new StringTokenizer(System.getProperty("java.class.path"), ";");
		while (tokenizer.hasMoreTokens())
		{
			String path = tokenizer.nextToken();
			if (path.endsWith(".jar"))
				this.jarFiles.add(path);
			else
				this.directories.add(path);
		}
	}
	
	/**
	 * <p>
	 * Adds a certain path to the currently managed set of directories or jar
	 * files to look for .class files.
	 * </p>
	 * 
	 * @param path
	 *            The path to the directory or jar file to add to the currently
	 *            managed class path
	 */
	public void addToClassPath(String path)
	{
		if (path.endsWith(".jar") && !this.jarFiles.contains(path))
			this.jarFiles.add(path);
		else if (!directories.contains(path))
			this.directories.add(path);
	}
	
	/**
	 * <p>
	 * Tries to find all implementing classes of a certain interface loaded by a
	 * specific {@link ClassLoader}
	 * </p>
	 * 
	 * @param iface
	 *            The interface classes have to implement to be listed
	 * @param loader
	 *            The class loader the implementing classes got loaded with
	 * @return A {@link List} of implementing classes for the provided
	 *         interface. If <em>iface</em> is not an interface null is returned
	 */
	public List<Class<?>> findImplementingClasses(Class<?> iface, ClassLoader loader)
	{
		if (iface.isInterface())
		{
			List<Class<?>> implementingClasses = new ArrayList<>();
			implementingClasses.addAll(this.findImplementingClassesInDirectories(iface, loader));
			implementingClasses.addAll(this.findImplementingClassesInJarFiles(iface, loader));
			return implementingClasses;
		}
		return null;
	}
	
	/**
	 * <p>
	 * Looks inside all jar files added previously to the instance' class-path
	 * for implementing classes of the provided interface.
	 * </p>
	 * 
	 * @param iface
	 *            The interface classes have to implement
	 * @param loader
	 *            The class loader the implementing classes got loaded with
	 * @return A {@link List} of implementing classes for the provided interface
	 *         inside jar files of the <em>ClassFinder</em>s class path
	 */
	public List<Class<?>> findImplementingClassesInJarFiles(Class<?> iface, ClassLoader loader)
	{
		List<Class<?>> implementingClasses = new ArrayList<>();
		for (String file : this.jarFiles)
		{
			// scan the jar file for all included classes
			for (String classFile : scanJarFileForClasses(new File(file)))
			{
				Class<?> clazz;
				try
				{
					// now try to load the class 
			        if (loader == null)
			          	clazz = Class.forName(classFile);
			        else
			           	clazz = Class.forName(classFile, false, loader);
			                
			        // and check if the class implements the provided interface
			        if (iface.isAssignableFrom(clazz) && !clazz.equals(iface))
			           	implementingClasses.add(clazz);
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		return implementingClasses;
	}
	
	/**
	 * <p>
	 * Looks inside all directories added previously to the instance' class-path
	 * for implementing classes of the provided interface.
	 * </p>
	 * 
	 * @param iface
	 *            The interface classes have to implement
	 * @param loader
	 *            The class loader the implementing classes got loaded with
	 * @return A {@link List} of implementing classes for the provided interface
	 *         inside jar files of the <em>ClassFinder</em>s class path
	 */
    public List<Class<?>> findImplementingClassesInDirectories(Class<?> iface, ClassLoader loader)
    {    	
        List<Class<?>> implementingClasses = new ArrayList<>();	
        
        // add all class files of the specified directories to the class files list 
        List<File> classFiles = new ArrayList<>();
        for (String path : this.directories)
        	classFiles.addAll(findClassFilesInDirectory(new File(path), true));
        
        // and test every class 
        for (File file : classFiles)
        {
            // get the relative path of the class file
            String relativePath = file.getAbsolutePath().substring(System.getProperty("user.dir").length());
            // path'es are either in 'a/b/c' or in 'a\\b\\c' notation, but we need
            // a.b.c for class files to be loaded, so convert them
            String classFile = this.convertPath(relativePath);
            
            Class<?> clazz;
            try
            {
                // now try to load the class 
                if (loader == null)
                	clazz = Class.forName(classFile);
                else
                	clazz = Class.forName(classFile, false, loader);
                
                // and check if the class implements the provided interface
                if (iface.isAssignableFrom(clazz) && !clazz.equals(iface))
                    implementingClasses.add(clazz);
            }
            catch (ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }
        
        return implementingClasses;
    }
    
	/**
	 * <p>
	 * Converts a path to a class file to a fully-qualified class name.
	 * </p>
	 * 
	 * @param path
	 *            The relative path of the class
	 * @return The fully-qualified class name
	 */
    private String convertPath(String path)
    {
        String classFile = path.replace(".class", "");
        classFile = classFile.replace("/", ".");
        classFile = classFile.replace("\\", ".");
        classFile = classFile.replace(".build.classes.", "");
        classFile = classFile.replace(".build.", "");
        
        return classFile;
    }
    
	/**
	 * <p>
	 * Finds class files in a specified directory and adds them to a
	 * {@link List} of found classes.
	 * </p>
	 * <p>
	 * If <em>includeSubDir</em> is set to true this method will traverse
	 * through sub directories.
	 * </p>
	 * 
	 * @param dir
	 *            The directory to look for .class files
	 * @param includeSubDir
	 *            true if sub directories should be traversed in order to find
	 *            class files
	 * @return A {@link List} of found class files within the directory
	 */
    public List<File> findClassFilesInDirectory(File dir, boolean includeSubDir)
    {
        List<File> classFiles = new ArrayList<>();
        if (dir.isDirectory())
            this.traverseDirectory(dir, classFiles, includeSubDir);
        return classFiles;
    }
    
	/**
	 * <p>
	 * Traverses the current directory in order to add found .class files to the
	 * provided {@link List} of already found class files.
	 * </p>
	 * <p>
	 * If <em>includeSubDir</em> is set to true this method will traverse
	 * through all sub directories and add .class files found to the list.
	 * </p>
	 * 
	 * @param directory
	 *            The directory to add .class files to <em>classFiles</em>
	 * @param classFiles
	 *            A {@link List} of found .class files within the directory
	 * @param includeSubDir
	 *            true if sub directories should be traversed in order to find
	 *            class files
	 */
    private void traverseDirectory(File directory, List<File> classFiles, boolean includeSubDir)
    {
        for (File file : directory.listFiles())
        {
            if (file.isDirectory() && includeSubDir)
                traverseDirectory(file, classFiles, includeSubDir);
            else if (file.getName().endsWith(".class"))
                classFiles.add(file);
        }
    }
    
	/**
	 * Scans a JAR file for .class-files and returns a {@link List} containing
	 * the full name of found classes (in the following form:
	 * packageName.className)
	 * 
	 * @param file
	 *            JAR-file which should be searched for .class-files
	 * @return Returns all found class-files with their full-name as a List of
	 *         Strings
	 */
	public static List<String> scanJarFileForClasses(File file)
	{
		if (file == null || !file.exists())
			return null;
		if (file.getName().endsWith(".jar"))
		{
			List<String> foundClasses = new ArrayList<String>();
			try (JarFile jarFile = new JarFile(file))
			{
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements())
				{
					JarEntry entry = entries.nextElement();
					if (entry.getName().endsWith(".class"))
					{
						String name = entry.getName();
						name = name.substring(0,name.lastIndexOf(".class"));
						if (name.indexOf("/")!= -1)
							name = name.replaceAll("/", ".");
						if (name.indexOf("\\")!= -1)
							name = name.replaceAll("\\", ".");
						foundClasses.add(name);
					}
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				return null;
			}
			return foundClasses;
		}
		return null;
	}
}
