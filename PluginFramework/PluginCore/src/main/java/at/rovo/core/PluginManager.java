package at.rovo.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.Attributes;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import at.rovo.core.classloader.DelegationClassLoader;
import at.rovo.core.classloader.IClassLoaderStrategy;
import at.rovo.core.classloader.PluginLoaderStrategy;
import at.rovo.core.classloader.StrategyClassLoader;
import at.rovo.core.filemonitor.ClassFilter;
import at.rovo.core.filemonitor.FileAction;
import at.rovo.core.filemonitor.FileMonitor;
import at.rovo.core.filemonitor.IDirectoryChangeListener;
import at.rovo.core.filemonitor.JarFilter;
import at.rovo.plugin.IPlugin;
import at.rovo.plugin.PluginException;

/**
 * <p>
 * <code>PluginManager</code> takes care of the loading of plug-ins in a
 * specified directory, which can be set with the
 * {@link #setPluginDirectory(String)}-Method. Classes interested in
 * notification of loading or unloading from or exception thrown by plug-ins can
 * register themselves with <code>PluginManager</code> via the
 * {@link #addPluginListener(IPluginListener)}-method.
 * </p>
 * <p>
 * This class provides several methods to actually load plug-ins:
 * </p>
 * <li>{@link #loadPlugins()}: Loads all plug-ins located in the directory
 * defined by {@link #setPluginDirectory(String)}</li> <li>
 * {@link #loadAllPluginsFromDirectory(File)}: Loads all plug-ins located in a
 * directory which has to be provided as argument.</li> <li>
 * {@link #reloadPlugin(File)}: (Re)Loads a specific plug-in</li>
 * <p>
 * Every plug-in gets loaded by a different {@link ClassLoader} to provide a
 * mechanism to unload an unneeded or reload an updated plug-in at runtime
 * without having to tear down the whole system.
 * </p>
 * <p>
 * As with the loading, <code>PluginManager</code> provides two methods to
 * remove plug-ins from the system:
 * </p>
 * <li>{@link #unloadAll()}: Unloads every loaded plug-in.</li> <li>
 * {@link #unload(String)}: Unloads a specific plug-in.</li>
 * <p>
 * The unloading of loaded classes is not guaranteed as they get unloaded by the
 * garbage collector. If any class does have a valid reference to a class
 * defined by the plug-in, unloading of the plug-in will fail.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public abstract class PluginManager implements IDirectoryChangeListener
{
	/** The logger of this class **/
	private static final Logger 
		LOGGER = Logger.getLogger(PluginManager.class.getName());
	/** The directory plug-ins should be found **/
	private String pluginDir = null;
	/** A mapping of plug-in names and their corresponding meta-data **/
	protected Map<String, PluginMeta> pluginData = null;
	/** A set of currently registered listeners who want to be informed on
	 * successful loads, unload or exceptions while loading plug-ins **/
	protected Set<IPluginListener> listeners;
	/** The class loader which holds the singleton components **/
	protected DelegationClassLoader commonClassLoader = null;
	
	/** The thread which takes care of re-checking if a needed dependency was
	 * already loaded **/
	protected final Thread waitForDependenciesThread;
	/** **/
	protected final List<String> waitingForDependencies = new ArrayList<>();
	/** Specifies if the waiting for dependencies thread should finish his work
	 * (= true) or if it is still needed (= false) **/
	protected volatile boolean done = false;
	
	/**
	 * <p>
	 * Instantiates the instance with required initial setups.
	 * </p>
	 */
	protected PluginManager()
	{
		this.pluginData = new HashMap<>();
		this.listeners = new CopyOnWriteArraySet<>();
		// create the class loader which will hold the exported and required
		// class definitions and therefore be responsible for their creation 
		this.commonClassLoader = new DelegationClassLoader(this.getClass().getClassLoader());
		
		this.waitForDependenciesThread = new Thread(new Runnable() 
		{
			@Override
			public void run()
			{
				List<String> reload = new ArrayList<>();
				while (!done)
				{
					if (!waitingForDependencies.isEmpty())
					{
						reload.clear();
						synchronized(waitingForDependencies)
						{
							// wait 10 seconds before retry
							try
							{
								waitingForDependencies.wait(10000);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
							// check if the dependencies of the plugin in 
							// question are now available
							for (String plugin : waitingForDependencies)
							{
								reload.add(plugin);
							}
						}
						
						if (!reload.isEmpty())
							for (String plugin : reload)
								reloadPlugin(plugin);
					}
					else
					{
						// no plugins available to check
						synchronized(waitingForDependencies)
						{
							try
							{
								waitingForDependencies.wait();
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		this.waitForDependenciesThread.setName("WaitForDependencies");
		this.waitForDependenciesThread.setDaemon(true);
		this.waitForDependenciesThread.start();
	}
		
	/**
	 * <p>
	 * Sets the directory <code>PluginManager</code> should look for plug-ins.
	 * After a valid directory got set, <code>PluginManager</code> starts
	 * watching for modified files.
	 * </p>
	 * 
	 * @param dir
	 *            Directory containing plug-ins
	 * @throws FileNotFoundException
	 *             if the directory does not exist
	 * @throws IOException
	 *             if the file is not an directory
	 */
	public void setPluginDirectory(String dir) throws FileNotFoundException, IOException
	{
		File file = new File(dir);
		if (!file.exists())
			throw new FileNotFoundException("Directory "+dir+" does not exist");
		if (!file.isDirectory())
			throw new IOException(dir+" is no directory");
		
		this.pluginDir = dir;
		
		// poll the FileMonitor every second
		FileMonitor monitor = new FileMonitor(1000, file);
		monitor.addListener(this);
		String[] files = file.list(new JarFilter());
		for (String _file : files)
			monitor.addFile(new File(dir, _file));
	}
	
	/**
	 * <p>
	 * Retrieves the directory <code>PluginManager</code> looks for plug-ins
	 * </p>
	 * 
	 * @return Absolute path of the plug-in-directory
	 */
	public String getPluginDirectory()
	{
		return this.pluginDir;
	}
	
	/**
	 * <p>
	 * Adds a listener to an internal List to objects who get notified when a
	 * plug-in has been loaded or removed.
	 * </p>
	 * 
	 * @param listener
	 *            The object who wants to be notified when a plug-in get loaded
	 *            or removed from the system.
	 */
	public void addPluginListener(IPluginListener listener)
	{
		this.listeners.add(listener);
	}
	
	/**
	 * <p>
	 * Removes a listener from the internal list of objects who get notified
	 * when a plug-in has been loaded or removed.
	 * </p>
	 * 
	 * @param listener
	 *            The object who wants to stop being notified when a plug-in
	 *            gets loaded or removed from the system.
	 * @return Returns true if the listener could be removed from the list of
	 *         notified objects, false otherwise.
	 */
	public boolean removePluginListener(IPluginListener listener)
	{
		boolean removed = false;
		if (this.listeners.contains(listener))
			removed = this.listeners.remove(listener);
		return removed;
	}
	
	@Override
	public void fileChanged(File file, FileAction fileAction)
	{
		switch (fileAction)
		{
			case FILE_CREATED:
				this.reloadPlugin(file);
				break;
			case FILE_MODIFIED:
				try
				{
					this.unload(this.getPluginNameBasedOnJar(file.toURI().toURL().toString()));
				}
				catch (MalformedURLException e)
				{
					
				}
				System.gc();
				this.reloadPlugin(file);
				break;
			case FILE_DELETED:
				// On dragging a jar inside the plug-in directory to a different 
				// location a FILE_DELETED action will be propagated by the 
				// FileMonitor
				// if automatic unloading of the plug-in should not happen, 
				// comment out the following lines
				String pluginName = null;
				for (PluginMeta data : this.pluginData.values())
				{
					try
					{
						String jarName = file.toURI().toURL().toString().substring("file:".length());
						if (data.getJarFileName().equals(jarName))
						{
							pluginName = data.getDeclaredClassName();
							break;
						}
					}
					catch (MalformedURLException e)
					{

					}
				}
				// the plugin could have been unloaded before and afterwards removed
				// in that case pluginName is null
				if (pluginName != null)
				{
					this.unload(pluginName);
				}
				System.gc();
				break;
			default:
				throw new IllegalArgumentException("Unknown FileAction provided!");
		}
	}
	
	/**
	 * <p>
	 * This method returns the actual {@link ClassLoader} of the plug-in.
	 * </p>
	 * 
	 * @param pluginName
	 *            Full name of the plug-in.
	 * @return Returns the {@link ClassLoader} which loaded the plug-in
	 */
	public ClassLoader getClassLoaderOfPlugin(String pluginName)
	{
		return pluginData.get(pluginName).getClassLoader();
	}
	
	/**
	 * <p>
	 * Returns a String array of filenames in the directory which are potential
	 * plug-in files.
	 * </p>
	 * 
	 * @param dir
	 *            The File object representing the directory to iterate through
	 * @return The jar files contained in the plugin directory
	 */
	protected String[] getPluginDirectoryContents(File dir)
	{
		if (!dir.exists() || !dir.isDirectory())
			return new String[0];
		
		String[] contents = dir.list(new JarFilter());
		return contents;
	}
	
	/**
	 * <p>
	 * Returns a String array of filenames in the directory which are .class
	 * files.
	 * </p>
	 * 
	 * @param dir
	 *            The File object representing the directory to iterate through
	 */
	protected String[] getPluginDirClasses(File dir)
	{
		if (!dir.exists() || !dir.isDirectory())
			return new String[0];
		
		String[] contents = dir.list(new ClassFilter());
		return contents;
	}
	
	/**
	 * Simple helper method to convert a List of URL objects into an array of
	 * URL objects (required by URLClassLoader)
	 * 
	 * @param list A {@link List} of {@link URL}s
	 * 
	 * @return The elements of the provided list as an array of {@link URL}s
	 */
	protected URL[] ListToArray(List<URL> list)
	{
		return list.toArray(new URL[] {});
	}
	
	/**
	 * <p>
	 * Loads all plug-ins in the through {@link #setPluginDirectory(String)}
	 * defined directory.
	 * </p>
	 * <p>
	 * A plug-in is either a compiled java-file (.class) or a zip-file
	 * containing compiled java-files (.jar).
	 * </p>
	 */
	public void loadPlugins()
	{
		this.loadAllPluginsFromDirectory(new File(this.pluginDir));
	}
	
	/**
	 * <p>
	 * Loads all found .jar- or .zip-files from a certain directory into the
	 * java virtual machine.
	 * </p>
	 * <p>
	 * Every plug-in is loaded in a separate ClassLoader, which allows Plug-ins
	 * to be unloaded from and reloaded into the system. Note however that the
	 * unloading of already loaded classes is not guaranteed as they are only
	 * unloaded by the garbage collector if no reference to the classes to be
	 * unloaded exists anymore.
	 * </p>
	 * 
	 * @param dir
	 *            Directory the plug-ins reside in
	 */
	public void loadAllPluginsFromDirectory(File dir)
	{
		String[] contents = this.getPluginDirectoryContents(dir);
		for (String content : contents)
		{
			File jarFile = new File(dir,content);
			this.reloadPlugin(jarFile);
		}
	}
	
	/**
	 * <p>
	 * Loads or reloads a plug-in in from of a certain .jar- or .zip- archive
	 * and looks in the MANIFEST/MANIFEST.MF-file, lying inside the archive, for
	 * a "Plugin-Class:"-entry to know which class in the archive will be the
	 * starting point.
	 * </p>
	 * <p>
	 * Every plug-in is loaded in a separate ClassLoader, which allows Plug-ins
	 * to be unloaded from and reloaded into the system. Note however that the
	 * unloading of already loaded classes is not guaranteed as they are only
	 * unloaded by the garbage collector if no reference to the classes to be
	 * unloaded exists anymore.
	 * </p>
	 * 
	 * @param jarFile
	 *            .jar- or .zip-archive containing the class(es) to load and the
	 *            MANIFEST/MANIFEST.MF-file describing what class to load first.
	 */
	public void reloadPlugin(File file)
	{
		String pluginClass = null;
		try
		{
			// Extract the entry-point of the plug-in which is defined
			// by the 'Plugin-Class'-field in the MANIFEST.MF-file
			JarFile jarFile = new JarFile(file);
			if (jarFile.getManifest() == null)
			{
				jarFile.close();
				throw new IllegalArgumentException("Archive does not have a META-INF/MANIFEST.MF-file");
			}
			Attributes attributes = jarFile.getManifest().getMainAttributes();
			// releases the lock on the loaded jar file
			jarFile.close();
			
			pluginClass = attributes.getValue("Plugin-Class").trim();
			// parse the as exported marked classes - a class marked as export
			// will be put into the common classloader
			String rawExportedClasses = attributes.getValue("Export");
			List<String> export = this.parseClassSet(rawExportedClasses);
			LOGGER.log(Level.INFO, "Found classes to export {0} inside jar {1}", 
					new Object[] { export, file });
			
			// parse the as required marked classes - before loading any class 
			// of the plugin it will be checked if the required classes are
			// available
			String rawRequiredClasses = attributes.getValue("Requires");
			List<String> required = this.parseClassSet(rawRequiredClasses);
			LOGGER.log(Level.INFO, "Found required classes for {0}: {1}", 
					new Object[] { file, required });

			attributes = null;
			
			if (pluginClass == null)
				throw new JarException("Missing entry \"Plugin-Class:\" inside the MANIFEST.MF-file");
			this.reloadPlugin(file, pluginClass, export, required);
		}
		catch (IOException e)
		{
			for (IPluginListener listener : this.listeners)
				listener.exception(pluginClass, e);
		}
	}
	
	/**
	 * <p>
	 * Loads or reloads a plug-in, therefore this method caches the archive and
	 * the class-name which represent the starting point of the plug-in.
	 * </p>
	 * <p>
	 * This method does not check the .jar or .zip-file if they contain a
	 * MANIFEST/MANIFEST.MF-file or the entry-point class-name, use
	 * {@link #reloadPlugin(File)} therefore.
	 * </p>
	 * <p>
	 * Every plug-in is loaded in a separate ClassLoader, which allows Plug-ins
	 * to be unloaded from and reloaded into the system. Note however that the
	 * unloading of already loaded classes is not guaranteed as they are only
	 * unloaded by the garbage collector if no reference to the classes to be
	 * unloaded exists anymore.
	 * </p>
	 * 
	 * @param jarFile
	 * @param pluginName
	 */
	protected void reloadPlugin(File jarFile, String pluginName, 
			List<String> exported, List<String> required)
	{		
		// if the plug-in was loaded before there has to be
		// still a valid PluginMeta-instance for this plug-in,
		// if there is none, we have to create a new PluginMeta-
		// object.		
		PluginMeta meta = this.pluginData.get(pluginName);		
		if (meta == null)
		{
			meta = new PluginMeta();
			meta.setPluginName(pluginName);
			meta.setDeclaredClassName(pluginName);
			meta.setExportedClassSet(exported);
			meta.setRequiredClassSet(required);
			LOGGER.log(Level.INFO, "Created meta for plugin {0}", 
					new Object[] { pluginName });
		}
		try
		{
			meta.setJarFileURL(jarFile.toURI().toURL());
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		this.pluginData.put(pluginName, meta);
		this.reloadPlugin(pluginName);
	}
	
	/**
	 * <p>
	 * Reloads a plug-in whose .jar or .zip-file got already loaded into the
	 * systems cache.
	 * </p>
	 * <p>
	 * If a plug-in from a modified or newly added archive should be loaded use
	 * {@link #reloadPlugin(File)} or {@link #reloadPlugin(File, String)}
	 * instead.
	 * </p>
	 * 
	 * @param pluginName
	 *            Full name of the plug-in to reload.
	 */
	protected void reloadPlugin(String pluginName)
	{
		PluginMeta meta = this.pluginData.get(pluginName);
		try
		{
			// check if all dependencies specified for the plug-in are available
			// skip further processing if a dependendy is missing
			if (!this.checkDependencies(meta))
				return;
			
			URL fileURL = meta.getJarFileURL();

			LOGGER.log(Level.INFO, "Creating strategy for {0}", 
					new Object[] { pluginName });
			Set<IClassLoaderStrategy> strategy = 
					new HashSet<IClassLoaderStrategy>();
			strategy.add(new PluginLoaderStrategy(fileURL));	
			
			// load classes that are marked as to export in the common 
			// classloader
			this.loadExportedClasses(meta, strategy);
			
			// load the rest of the plugin with the plugin's own respective 
			// classloader which is a child of the commons class loader
			StrategyClassLoader pluginLoader = 
					new StrategyClassLoader(this.commonClassLoader, strategy);

			meta.setClassLoader(pluginLoader);
				
			LOGGER.log(Level.INFO, "Loading plugin's main class: {0}", 
					new Object[] { pluginName });
			Class<?> result = pluginLoader.loadClass(pluginName);
			meta.setClassObj(result);
			if (result != null)
			{
				for (IPluginListener listener : this.listeners)
					listener.pluginLoaded(pluginName);
			}
			
			// check if the added plugin solved dependency issues
			if (!this.waitingForDependencies.isEmpty())
			{
				synchronized(this.waitingForDependencies)
				{
					this.waitingForDependencies.notify();
				}
			}
		}
		catch (Exception e)
		{
			for (IPluginListener listener : this.listeners)
				listener.exception(pluginName, e);
		}
	}
	
	/**
	 * <p>
	 * Creates a new instance of the main-class of a plug-in
	 * </p>
	 * 
	 * @param name
	 *            Name of the plug-in which should get instantiated
	 * @return Returns a new instance of the plug-ins' main class
	 */
	public IPlugin getNewPluginInstance(String name)
	{
		LOGGER.log(Level.INFO, "plugin-name: {0}", new Object[] { name });
		PluginMeta meta = this.pluginData.get(name);
		Class<?> _class = meta.getClassObj();
		Constructor<?> c;
		try 
		{
			c = _class.getConstructor();
			IPlugin instance = (IPlugin)c.newInstance();
			meta.setPlugin(instance);
			return instance;
		} 
		catch (Exception e)
		{
			for (IPluginListener listener : this.listeners)
				listener.exception(name, e);
		}
		return null;
	}
	
	/**
	 * <p>
	 * Unloads all currently loaded plug-ins by calling {@link #unload(String)}
	 * for every found plug-in.
	 * </p>
	 * <p>
	 * Note that unloading plug-ins is not guaranteed as plug-ins get unloaded
	 * by the system garbage collector and if there is still a valid reference
	 * to a plug-in unloading will fail.
	 * </p>
	 */
	public void unloadAll()
	{
		java.util.Iterator<String> iter = this.pluginData.keySet().iterator();
		while (iter.hasNext())
		{
			unload(iter.next());
		}
	}
	
	/**
	 * <p>
	 * Unloads a certain plug-in, if it has been loaded before. A reference to
	 * the .jar- or .zip-file which provided the plug-in remains in the system.
	 * </p>
	 * <p>
	 * Note that unloading a plug-in is not guaranteed as the plug-in gets
	 * unloaded by the system garbage collector and if there is still a valid
	 * reference to the plug-in unloading will fail.
	 * </p>
	 * 
	 * @param name
	 *            Full name of the plug-in to unload from the system.
	 */
	public void unload(String name)
	{		
		PluginMeta meta = this.pluginData.get(name);
		if (meta != null)
		{
			try
			{
			meta.removeExportedClasses();
			meta.removeRequiredClasses();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			// on removing the plugin instance all objects held by this instance
			// will lose their strong reference and will be eligible for garbage
			// collection. Singleton instances however may prevent the the clean
			// up step as they keep a strong reference to themselves.
			meta.setPlugin(null);
			
			// the class loader is only removed if all initialized objects 
			// defined by the plugin are unloaded. If a further plugin 
			// references any class of this plugin the classloader can't be
			// garbage collected and stays alive till the strong reference is 
			// lost
			meta.setClassLoader(null);
			meta.setClassObj(null);
			
			this.pluginData.remove(name);
			
			for (IPluginListener listener : this.listeners)
				listener.pluginRemoved(name);
		}
		else
			throw new PluginException("Couldn't find registered plugin: "+name);
	}
	
	/**
	 * <p>
	 * Returns a {@link Set} of loaded plug-ins by their full qualified name
	 * </p>
	 * 
	 * @return {@link Set} of full qualified names of loaded plug-ins
	 */
	public Set<String> getLoadedPlugins()
	{
		return this.pluginData.keySet();
	}
	
	/**
	 * <p>
	 * Returns the full qualified name of the class defined in the MANIFEST.MF
	 * file inside the META-INF-directory of the .jar- or .zip-file.
	 * </p>
	 * 
	 * @param jarName
	 *            Name of the .jar- or .zip-file containing the plug-in
	 *            including the path to the file
	 * @return full qualified name of the class which defines the plug-in, empty
	 *         {@link String} if the plug-in could not be found
	 * @throws IOException
	 */
	public String getPluginNameBasedOnJar(String jarName)
	{
		String jarNameUrl = null;
		// filter an absolute path as it would result in a structure like:
		// C:/some/path/workspace/file:/C:/some/path/workspace/package/plugin.jar
		if (!jarName.startsWith("file:"))
		{
			if (jarName.startsWith("./"))
				jarName = jarName.substring(2);
			if (jarName.startsWith("."))
				jarName = jarName.substring(1);
			
			
			File file = new File(jarName);
			try
			{
				jarNameUrl = file.toURI().toURL().toString();
			}
			catch (MalformedURLException e)
			{
				return "";
			}
		}
		else
			jarNameUrl = jarName;
		jarNameUrl = jarNameUrl.substring("file:".length());
		
		for (PluginMeta data : this.pluginData.values())
		{						
			if (data.getJarFileName().equals(jarNameUrl))
				return data.getDeclaredClassName();
		}
		return "";
	}
	
	/**
	 * <p>
	 * Parses a set of classes which are separated by a blank and adds them to
	 * the list which is then returned.
	 * </p>
	 * 
	 * @param classes The string which contains the classes to split
	 * @return The list containing the parsed classes
	 */
	private final List<String> parseClassSet(String classes)
	{
		if (classes == null)
			return Collections.emptyList();
		List<String> parsedClasses = new ArrayList<>();
		String[] split = classes.trim().split("\\s");
		for (String clazz : split)
			if (!clazz.trim().equals(""))
				parsedClasses.add(clazz);
		return parsedClasses;
	}
	
	/**
	 * <p>
	 * Add classes that are marked as export for the respective plugin to the
	 * common classloader and adds a reference of the class file loaded to the
	 * plugin's meta data.
	 * </p>
	 * 
	 * @param meta
	 *            The plugin's meta data which hold the information of the
	 *            classes to load
	 * @param strategy
	 *            The strategy used on loading the required classes
	 */
	protected final void loadExportedClasses(PluginMeta meta,
			Set<IClassLoaderStrategy> strategies)
	{
		for (String classToExport : meta.getExportedClasses())
		{
			StrategyClassLoader loader = new StrategyClassLoader(this.commonClassLoader, strategies);
			this.commonClassLoader.addLoaderForName(classToExport, loader);
			Class<?> export = null;
			try
			{
				export = loader.loadClass(classToExport);
			}
//			catch (ClassNotFoundException e)
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			meta.addExpordedClass(classToExport, export);
			LOGGER.log(Level.INFO, "Loaded exported class: '{0}' with '{1}' added as composition to '{2}'", 
					new Object[] { classToExport, loader.getName(), this.commonClassLoader });
		}
	}
	
	/**
	 * <p>
	 * Executes cleanup steps necessary in order to finish properly.
	 * </p>
	 */
	public void close()
	{
		this.done = true;
		if (this.waitForDependenciesThread != null 
				&& this.waitingForDependencies != null)
		{
			synchronized (this.waitingForDependencies)
			{
				this.waitingForDependencies.notify();
			}
		}
	}
	
	/**
	 * <p>
	 * Checks if all dependencies for a plug-in are available. If a required
	 * class is missing, this method will add the plug-in to a waiting list
	 * which will retry every 10 seconds if the dependencies are available.
	 * </p>
	 * 
	 * @param meta
	 *            The plug-ins meta data which contain the required dependencies
	 *            for the given plug-in as well as the plug-ins name
	 * @return true if all dependencies are available, false otherwise
	 */
	protected boolean checkDependencies(PluginMeta meta)
	{
		boolean allClassesAvailable = true;
		// check if all dependencies are available in the common classloader
		Set<String> requiredClasses = meta.getRequiredClasses();
		for (String requiredClass : requiredClasses)
		{
			if (!this.commonClassLoader.containsClass(requiredClass))
			{
				LOGGER.log(Level.INFO, "Plugin {0} is missing class '{1}'.", 
						new Object[] { meta.getPluginName(), requiredClass });
				allClassesAvailable = false;
				break;
			}
		}
		
		// only proceed if all required classes (if any were defined) are
		// available
		if (allClassesAvailable)
		{
			// The dependencies for the plugin are (now) available. If the
			// plugin was set on the waiting list before remove it
			synchronized(this.waitingForDependencies)
			{
				if (this.waitingForDependencies.contains(meta.getPluginName()))
				{
					this.waitingForDependencies.remove(meta.getPluginName());
					LOGGER.log(Level.INFO, "Dependencies for plugin {0} found - loading plugin.", 
							new Object[] { meta.getPluginName() });
				}
			}
			return true;
		}
		else
		{
			LOGGER.log(Level.FINE, "Missing depency for {0}", 
					new Object[] { meta.getPluginName()});
			synchronized(this.waitingForDependencies)
			{
				// add the plugin name if it was not yet avaialble
				if (!this.waitingForDependencies.contains(meta.getPluginName()))
				{
					LOGGER.log(Level.INFO, 
							"Missing dependency for plugin {0} - added plugin to waiting list", 
							new Object[] { meta.getPluginName() });
					this.waitingForDependencies.add(meta.getPluginName());
				}
				else
					LOGGER.log(Level.INFO, "Dependencies for plugin {0} still not found.", 
							new Object[] { meta.getPluginName() });
				// wake up the thread if it was sleeping
				this.waitingForDependencies.notify();
			}
			// do not proceed as not all dependencies are available
			return false;
		}
	}
}
