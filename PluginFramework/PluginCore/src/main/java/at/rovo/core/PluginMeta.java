package at.rovo.core;

import at.rovo.common.plugin.IPlugin;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * On loading a {@link IPlugin} implementation the responsible {@link PluginManager} creates some meta data for the
 * plug-in which will be stored in a new instance of this class.
 *
 * @author Roman Vottner
 */
@SuppressWarnings("WeakerAccess")
public class PluginMeta
{
    /** The name of the plug-in **/
    private String name = null;
    /** The class which implements the IPlugin interface **/
    private String declaredClass = null;
    /** The class loader which loaded the plug-in **/
    private ClassLoader classLoader = null;
    /** The jar file containing the plug-in **/
    private URL jarFile = null;
    /** The IPlugin implementing class **/
    private Class<?> pluginClass = null;
    /** The loaded and initialized plug-in **/
    private IPlugin plugin = null;
    /** Will hold a strong reference to the exported classes **/
    private Map<String, Class<?>> exportedClasses = new HashMap<>();
    /** Will hold a strong reference to the classes required by this plugin **/
    private Map<String, Class<?>> requiredClasses = new HashMap<>();

    /**
     * Sets the name of the plug-in to the defined value.
     *
     * @param name
     *         The new name of the plug-in this meta data points to
     */
    public void setPluginName(String name)
    {
        this.name = name;
    }

    /**
     * The name of the plug-in this instance is pointing to
     *
     * @return The name of the plug-in
     */
    public String getPluginName()
    {
        return this.name;
    }

    /**
     * Sets the name of the class which implements the {@link IPlugin} interface.
     *
     * @param className
     *         The name of the implementing class
     */
    public void setDeclaredClassName(String className)
    {
        this.declaredClass = className;
    }

    /**
     * Returns the name of the {@link IPlugin} implementing class.
     *
     * @return The name of the {@link IPlugin} implementing class
     */
    public String getDeclaredClassName()
    {
        return this.declaredClass;
    }

    /**
     * Sets the {@link ClassLoader} for the plug-in this instance is pointing to.
     *
     * @param cl
     *         The class loader of the plug-in
     */
    public void setClassLoader(ClassLoader cl)
    {
        this.classLoader = cl;
    }

    /**
     * Returns the {@link ClassLoader} of the plug-in this instance is pointing to.
     *
     * @return The class loader of the plug-in
     */
    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    /**
     * Sets the {@link URL} of the jar file that contains the plug-in.
     *
     * @param jarFile
     *         The {@link URL} of the jar-file containing the plug-in
     */
    public void setJarFileURL(URL jarFile)
    {
        this.jarFile = jarFile;
    }

    /**
     * Sets the path to the jar file that contains the plug-in.
     *
     * @param jarFile
     *         The path of the jar-file containing the plug-in
     */
    public void setJarFileName(String jarFile)
    {
        try
        {
            this.jarFile = new URL(jarFile);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Returns the {@link URL} of the jar containing the plug-in.
     *
     * @return The {@link URL} of the jar containing the plug-in
     */
    public URL getJarFileURL()
    {
        return this.jarFile;
    }

    /**
     * Returns the name of the jar file containing the plug-in.
     *
     * @return The name of the jar file containing the plug-in
     */
    public String getJarFileName()
    {
        String fileName = this.jarFile.toString();
        if (fileName.startsWith("file:"))
        {
            fileName = fileName.substring("file:".length());
        }
        return fileName;
    }

    /**
     * Sets the loaded class object of the class that implements the {@link IPlugin} interface.
     *
     * @param plugin
     *         The class object of the implementing plug-in
     */
    public void setClassObj(Class<?> plugin)
    {
        this.pluginClass = plugin;
    }

    /**
     * Returns the class object of the class that implements the {@link IPlugin} interface.
     *
     * @return The class object of the implementing plug-in
     */
    public Class<?> getClassObj()
    {
        return this.pluginClass;
    }

    /**
     * Sets the loaded and initialized plugin instance. The current meta instance will hold this plugin instance until
     * an unload request is received. This way garbage collection of any object held by the plugin will be prevented
     * until the plugin is actually unloaded.
     *
     * @param plugin
     *         The initialized plug-ins main class
     */
    public void setPlugin(IPlugin plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Returns the plugins main object.
     *
     * @return The initialized plugin main class
     */
    public IPlugin getPlugin()
    {
        return this.plugin;
    }

    /**
     * Adds only the name of the as exported annotated class to the plugin meta description.
     *
     * @param export
     *         The set containing the classes which are marked as to export
     */
    public void setExportedClassSet(List<String> export)
    {
        if (this.exportedClasses == null)
        {
            this.exportedClasses = new HashMap<>();
        }

        for (String name : export)
        {
            this.exportedClasses.put(name, null);
        }
    }

    /**
     * Adds a class which marked as to export to this plugin metadata.
     *
     * @param className
     *         The name of the exported class
     * @param clazz
     *         The class representation of the required class
     */
    public void addExpordedClass(String className, Class<?> clazz)
    {
        if (this.exportedClasses == null)
        {
            this.exportedClasses = new HashMap<>();
        }
        this.exportedClasses.put(className, clazz);
    }

    /**
     * Returns true if the provided class name is among the exported classes.
     *
     * @param className
     *         The class name to check if it is exported
     *
     * @return true if the provided classname equals an exported class
     */
    public boolean isExported(String className)
    {
        return this.exportedClasses != null && this.exportedClasses.containsKey(className);
    }

    /**
     * Returns the names of the exported classes by this plugin.
     *
     * @return The names of the exported classes by this plugin
     */
    public Set<String> getExportedClasses()
    {
        if (this.exportedClasses != null)
        {
            return this.exportedClasses.keySet();
        }
        return Collections.emptySet();
    }

    /**
     * Removes all references to classes marked as to export.
     */
    public void removeExportedClasses()
    {
        if (this.exportedClasses != null)
        {
            Set<String> classNames = new HashSet<>(this.exportedClasses.keySet());
            for (String className : classNames)
            {
                this.exportedClasses.remove(className);
            }
        }
    }

    /**
     * Returns the class object of the provided class name which was defined as to export.
     *
     * @param className
     *         The name of the exported class
     *
     * @return The class object of the exported class
     */
    public Class<?> getExportedClass(String className)
    {
        if (this.exportedClasses != null)
        {
            return this.exportedClasses.get(className);
        }
        return null;
    }

    /**
     * Adds only the name of the as required annotated class to the plugin meta description.
     *
     * @param required
     *         The set containing the classes which are marked as to export
     */
    public void setRequiredClassSet(List<String> required)
    {
        if (this.requiredClasses == null)
        {
            this.requiredClasses = new HashMap<>();
        }
        for (String name : required)
        {
            this.requiredClasses.put(name, null);
        }
    }

    /**
     * Adds a class which is required by this plugin to this metadata object.
     *
     * @param className
     *         The name of the by this plugin required class
     * @param clazz
     *         The class which is required by this plugin
     */
    public void addRequiredClass(String className, Class<?> clazz)
    {
        if (this.requiredClasses == null)
        {
            this.requiredClasses = new HashMap<>();
        }
        this.requiredClasses.put(className, clazz);
    }

    /**
     * Returns true if this plugin requires the class with the given class name.
     *
     * @param className
     *         The class name to check if it is required
     *
     * @return true if the provided class name is needed by this plugin
     */
    public boolean isRequiredClasses(String className)
    {
        return this.requiredClasses != null && this.requiredClasses.containsKey(className);
    }

    /**
     * Returns all as required marked classes for this plugin.
     *
     * @return The names of the required classes by this plugin
     */
    public Set<String> getRequiredClasses()
    {
        if (this.requiredClasses != null)
        {
            return this.requiredClasses.keySet();
        }
        return Collections.emptySet();
    }

    /**
     * Returns the loaded class object for the provided class name.
     *
     * @param className
     *         The name of the required class
     *
     * @return The class object of the required class
     */
    public Class<?> getRequiredClass(String className)
    {
        if (this.requiredClasses != null)
        {
            return this.requiredClasses.get(className);
        }
        return null;
    }

    /**
     * Removes all references to classes marked as required.
     */
    public void removeRequiredClasses()
    {
        if (this.requiredClasses != null)
        {
            Set<String> classNames = new HashSet<>(this.requiredClasses.keySet());
            for (String className : classNames)
            {
                this.requiredClasses.remove(className);
            }
        }
    }
}