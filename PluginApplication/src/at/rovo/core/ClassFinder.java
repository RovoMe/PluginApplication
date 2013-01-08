package at.rovo.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder
{
	private static ClassFinder cf;
	private List<String> directories;
	private List<String> jarFiles;
	
	private ClassFinder()
	{
		this.directories = new ArrayList<String>();
		this.jarFiles = new ArrayList<String>();
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
	
	public static ClassFinder getInstance()
	{
		if (cf == null)
			cf = new ClassFinder();
		return cf;
	}
	
	public void addToClassPath(String path)
	{
//		System.out.println("[ClassFinder.addToClassPath] "+path);
		if (path.endsWith(".jar") && !this.jarFiles.contains(path))
			this.jarFiles.add(path);
		else if (!directories.contains(path))
			this.directories.add(path);
	}
	
	public List<Class<?>> findImplementingClasses(Class<?> _interface, ClassLoader loader)
	{
		List<Class<?>> implementingClasses = new ArrayList<Class<?>>();
		implementingClasses.addAll(this.findImplementingClassesInDirectories(_interface, loader));
		implementingClasses.addAll(this.findImplementingClassesInJarFiles(_interface, loader));
		return implementingClasses;
	}
	
	public List<Class<?>> findImplementingClassesInJarFiles(Class<?> _interface, ClassLoader loader)
	{
		List<Class<?>> implementingClasses = new ArrayList<Class<?>>();
		for (String file : this.jarFiles)
		{
			for (String classFile : scanJarFileForClasses(new File(file)))
			{
				try
				{
					Class<?> clazz;
			        if (loader == null)
			          	clazz = Class.forName(classFile);
			        else
			           	clazz = Class.forName(classFile, false, loader);
			                
			        if (_interface.isAssignableFrom(clazz) && !clazz.equals(_interface))
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
	
    public List<Class<?>> findImplementingClassesInDirectories(Class<?> _interface, ClassLoader loader)
    {    	
        List<Class<?>> implementingClasses = new ArrayList<Class<?>>();	
        List<File> classFiles = new ArrayList<File>();
        for (String path : this.directories)
        	classFiles.addAll(findClassFilesInDirectory(new File(path), true));
        
        for (File file : classFiles)
        {
            try
            {
                String relativePath = file.getAbsolutePath().substring(System.getProperty("user.dir").length());
                String classFile = this.convertPath(relativePath);
                
                Class<?> clazz;
                if (loader == null)
                	clazz = Class.forName(classFile);
                else
                	clazz = Class.forName(classFile, false, loader);
                
                if (_interface.isAssignableFrom(clazz) && !clazz.equals(_interface))
                {
                    implementingClasses.add(clazz);
                }
            }
            catch (ClassNotFoundException ex)
            {
                //ex.printStackTrace();
            }
        }
        
        return implementingClasses;
    }
    
    private String convertPath(String path)
    {
        String classFile = path.replace(".class", "");
        classFile = classFile.replace("/", ".");
        classFile = classFile.replace("\\", ".");
        classFile = classFile.replace(".build.classes.", "");
        classFile = classFile.replace(".build.", "");
        
        return classFile;
    }
    
    public List<File> findClassFilesInDirectory(File dir, boolean includeSubDir)
    {
        List<File> classFiles = new ArrayList<File>();
        if (dir.isDirectory())
            traverseDirectory(dir, classFiles, includeSubDir);
        return classFiles;
    }
    
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
	 * Scans a JAR file for .class-files and returns a List containing the 
	 * full name of found classes (in the following form: packageName.className)
	 * 
	 * @param file JAR-file which should be searched for .class-files
	 * @return Returns all found class-files with their full-name as a List of Strings
	 */
	public static List<String> scanJarFileForClasses(File file)
	{
		if (file == null || !file.exists())
			return null;
		if (file.getName().endsWith(".jar"))
		{
			List<String> foundClasses = new ArrayList<String>();
			JarFile jarFile = null;
			try
			{
				jarFile = new JarFile(file);
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
			finally
			{
				try
				{
					jarFile.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return foundClasses;
		}
		return null;
	}
}
