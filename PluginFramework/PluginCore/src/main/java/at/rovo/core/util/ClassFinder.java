package at.rovo.core.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <code>ClassFinder</code> finds class files inside directories or jar files and provides methods to find implementing
 * classes of an interface.
 * <p/>
 * It manages an own class-path by allowing a user to add directories and/or jar files via {@link
 * #addToClassPath(String)} to it.
 * <p/>
 * Further, it provides methods to find classes in directories via {@link #findClassFilesInDirectory(File, boolean)} or
 * in jar files via {@link #scanJarFileForClasses(File)}.
 * <p/>
 * Implementations of a certain interface can be found via {@link #findImplementingClassesInDirectories(Class,
 * ClassLoader)} or {@link #findImplementingClassesInJarFiles(Class, ClassLoader)}. If implementations should be looked
 * for in both directories and jar files {@link #findImplementingClasses(Class, ClassLoader)} can be invoked.
 * <p/>
 * Note that before finding classes in directories or jar files, directories or jar files have to be added to the class
 * path of this instance.
 *
 * @author Roman Vottner
 */
public final class ClassFinder
{
    /** Application of the WeakSingleton pattern **/
    private static WeakReference<ClassFinder> REFERENCE;

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
            {
                this.jarFiles.add(path);
            }
            else
            {
                this.directories.add(path);
            }
        }
    }

    /**
     * Returns a new instance of the the class finder if none existed.
     * <p/>
     * This singleton method uses WeakReferences to enable unloading of the singleton if no strong reference is pointing
     * to the singleton.
     *
     * @return The sole instance of the class finder
     */
    public static ClassFinder getInstance()
    {
        if (REFERENCE == null)
        {
            synchronized (ClassFinder.class)
            {
                if (REFERENCE == null)
                {
                    ClassFinder instance = new ClassFinder();
                    REFERENCE = new WeakReference<>(instance);
                    return instance;
                }
            }
        }
        ClassFinder instance = REFERENCE.get();
        if (instance != null)
        {
            return instance;
        }

        synchronized (ClassFinder.class)
        {
            instance = new ClassFinder();
            REFERENCE = new WeakReference<>(instance);
            return instance;
        }
    }

    /**
     * Adds a certain path to the currently managed set of directories or jar files to look for .class files.
     *
     * @param path
     *         The path to the directory or jar file to add to the currently managed class path
     */
    public void addToClassPath(String path)
    {
        if (path.endsWith(".jar") && !this.jarFiles.contains(path))
        {
            this.jarFiles.add(path);
        }
        else if (!directories.contains(path))
        {
            this.directories.add(path);
        }
    }

    /**
     * Tries to find all implementing classes of a certain interface loaded by a specific {@link ClassLoader}
     *
     * @param iface
     *         The interface classes have to implement to be listed
     * @param loader
     *         The class loader the implementing classes got loaded with
     *
     * @return A {@link List} of implementing classes for the provided interface. If <em>iface</em> is not an interface
     * null is returned
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
     * Looks inside all jar files added previously to the instance' class-path for implementing classes of the provided
     * interface.
     *
     * @param iface
     *         The interface classes have to implement
     * @param loader
     *         The class loader the implementing classes got loaded with
     *
     * @return A {@link List} of implementing classes for the provided interface inside jar files of the
     * <em>ClassFinder</em>s class path
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
                    {
                        clazz = Class.forName(classFile);
                    }
                    else
                    {
                        clazz = Class.forName(classFile, false, loader);
                    }

                    // and check if the class implements the provided interface
                    if (iface.isAssignableFrom(clazz) && !clazz.equals(iface))
                    {
                        implementingClasses.add(clazz);
                    }
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
     * Looks inside all directories added previously to the instance' class-path for implementing classes of the
     * provided interface.
     *
     * @param iface
     *         The interface classes have to implement
     * @param loader
     *         The class loader the implementing classes got loaded with
     *
     * @return A {@link List} of implementing classes for the provided interface inside jar files of the
     * <em>ClassFinder</em>s class path
     */
    public List<Class<?>> findImplementingClassesInDirectories(Class<?> iface, ClassLoader loader)
    {
        List<Class<?>> implementingClasses = new ArrayList<>();

        // add all class files of the specified directories to the class files list 
        List<File> classFiles = new ArrayList<>();
        for (String path : this.directories)
        {
            classFiles.addAll(findClassFilesInDirectory(new File(path), true));
        }

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
                {
                    clazz = Class.forName(classFile);
                }
                else
                {
                    clazz = Class.forName(classFile, false, loader);
                }

                // and check if the class implements the provided interface
                if (iface.isAssignableFrom(clazz) && !clazz.equals(iface))
                {
                    implementingClasses.add(clazz);
                }
            }
            catch (ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }

        return implementingClasses;
    }

    /**
     * Converts a path to a class file to a fully-qualified class name.
     *
     * @param path
     *         The relative path of the class
     *
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
     * Finds class files in a specified directory and adds them to a {@link List} of found classes.
     * <p/>
     * If <em>includeSubDir</em> is set to true this method will traverse through sub directories.
     *
     * @param dir
     *         The directory to look for .class files
     * @param includeSubDir
     *         true if sub directories should be traversed in order to find class files
     *
     * @return A {@link List} of found class files within the directory
     */
    public static List<File> findClassFilesInDirectory(File dir, boolean includeSubDir)
    {
        return findFileInDirectory(dir, ".class", includeSubDir);
    }

    /**
     * Finds class files in a specified directory and adds them to a {@link List} of found classes.
     * <p/>
     * If <em>includeSubDir</em> is set to true this method will traverse through sub directories.
     *
     * @param dir
     *         The directory to look for .class files
     * @param fileName
     *         The name of the file to look for
     * @param includeSubDir
     *         true if sub directories should be traversed in order to find class files
     *
     * @return A {@link List} of found class files within the directory
     */
    public static List<File> findFileInDirectory(File dir, String fileName, boolean includeSubDir)
    {
        List<File> classFiles = new ArrayList<>();
        if (dir.isDirectory())
        {
            traverseDirectory(dir, classFiles, fileName, includeSubDir);
        }
        return classFiles;
    }

    /**
     * Traverses the current directory in order to add found .class files to the provided {@link List} of already found
     * class files.
     * <p/>
     * If <em>includeSubDir</em> is set to true this method will traverse through all sub directories and add .class
     * files found to the list.
     *
     * @param directory
     *         The directory to add .class files to <em>classFiles</em>
     * @param classFiles
     *         A {@link List} of found .class files within the directory
     * @param fileName
     *         The name of the file to look for
     * @param includeSubDir
     *         true if sub directories should be traversed in order to find class files
     */
    private static void traverseDirectory(File directory, List<File> classFiles, String fileName, boolean includeSubDir)
    {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("Provided directory is invalid and can't be traversed");
        }
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory() && includeSubDir)
                {
                    traverseDirectory(file, classFiles, fileName, true);
                }
                else if (file.getName().endsWith(fileName))
                {
                    classFiles.add(file);
                }
            }
        }
    }

    /**
     * Scans a JAR file for .class-files and returns a {@link List} containing the full name of found classes (in the
     * following form: packageName.className)
     *
     * @param file
     *         JAR-file which should be searched for .class-files
     *
     * @return Returns all found class-files with their full-name as a List of Strings
     */
    public static List<String> scanJarFileForClasses(File file)
    {
        if (file == null || !file.exists())
        {
            return null;
        }
        if (file.getName().endsWith(".jar"))
        {
            List<String> foundClasses = new ArrayList<>();
            try (JarFile jarFile = new JarFile(file))
            {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class"))
                    {
                        String name = entry.getName();
                        name = name.substring(0, name.lastIndexOf(".class"));
                        if (name.contains("/"))
                        {
                            name = name.replace("/", ".");
                        }
                        if (name.contains("\\"))
                        {
                            name = name.replace("\\", ".");
                        }
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

    /**
     * Returns a list of all entries of a jar file as Strings. The entries in the returned list will contain the
     * absolute paths inside the jar-archive.
     *
     * @param file
     *         The jar file to scan for contained files
     *
     * @return A list of file names found inside the jar archive
     */
    public static List<String> scanJarFileForAllFiles(File file)
    {
        return scanJarFileForFiles(file, null);
    }

    /**
     * Returns a list of all entries in a jar file that match the file name as String. The entries in the returned list
     * will contain the absolute paths inside the jar-archive.
     * <p/>
     * Note that the comparison uses the {@link String#endsWith(String)} method for comparison.
     *
     * @param file
     *         The jar file to scan for contained files
     * @param fileName
     *         The name of the files that should be returned in the list.
     *
     * @return The list containing the absolute paths of the files inside the jar that match the given file name
     */
    public static List<String> scanJarFileForFiles(File file, String fileName)
    {
        if (file == null || !file.exists())
        {
            return null;
        }
        if (file.getName().endsWith(".jar"))
        {
            List<String> foundFiles = new ArrayList<>();
            try (JarFile jarFile = new JarFile(file))
            {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    if (fileName != null && entry.getName().endsWith(fileName))
                    {
                        String name = entry.getName();
                        foundFiles.add(name);
                    }
                    else if (fileName == null)
                    {
                        foundFiles.add(entry.getName());
                    }
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                return null;
            }
            return foundFiles;
        }
        return null;
    }
}
