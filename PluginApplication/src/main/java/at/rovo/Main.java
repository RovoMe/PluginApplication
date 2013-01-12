package at.rovo;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import at.rovo.console.Console;
import at.rovo.console.command.ExecuteCommand;
import at.rovo.console.command.ListCommand;
import at.rovo.console.command.LoadCommand;
import at.rovo.console.command.UnloadCommand;
import at.rovo.core.IPluginListener;
import at.rovo.core.PluginManager;

/**
 * <p>This class is the entrance point into the plug-in application. It 
 * creates and holds the {@link PluginManager} instance and adds 
 * {@link ConsoleCommand}s to the underlying {@link Console} object.</p>
 * <p>Note that this application is not thread safe and only intended 
 * for a single stand-alone application.</p>
 * 
 * @author Roman Vottner
 * @version 0.2
 */
public class Main implements IPluginListener
{
	/**
	 * <p>Creates a new instance of this class, adds a {@link PluginManager}
	 * instance to the application and starts a {@link Console} to listen
	 * to user commands.</p>
	 */
	public Main()
	{				
		File propFile = new File("application.properties");
		Properties prop = new Properties();
		try 
		{
			// load the application data which tells us where plug-ins
			// should be found
			prop.load(new FileInputStream(propFile));
			String pluginDir = prop.getProperty("plugin.dir");
			String appDir = System.getProperty("user.dir");
			
			// create a new PluginManager
			PluginManager manager = at.rovo.core.InjectionPluginManager.getInstance();
//			PluginManager manager = at.rovo.core.SimplePluginManager.getInstance();
			manager.addPluginListener(this);
			manager.setPluginDirectory(appDir+"/"+pluginDir);
			
			// register new commands with the console
			new ListCommand(manager);
			new LoadCommand(manager);
			new UnloadCommand(manager);
			new ExecuteCommand(manager);
			
			// start the console
			Console.getInstance().run();
		} 
		catch (Exception e) 
		{
			System.err.println(e.getLocalizedMessage());
		} 
	}

	@Override
	public void pluginLoaded(String pluginName)
	{
//		System.out.println(pluginName+" got added!");
	}

	@Override
	public void pluginRemoved(String pluginName)
	{
//		System.out.println(pluginName+" was removed!");
	}

	@Override
	public void exception(String pluginName, Exception e)
	{
//		System.err.println("received "+e.getLocalizedMessage()+" from "+pluginName);
	}

	/**
	 * <p>Entrance point for this application</p>
	 * 
	 * @param args Arguments passed to the application; This application
	 *             does not handle any application arguments, so any
	 *             application level arguments passed on start up will be 
	 *             ignored
	 */
	public static void main(String[] args)
	{
		new Main();
	}
}
