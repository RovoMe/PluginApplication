package at.rovo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import at.rovo.console.Console;
import at.rovo.console.command.ExecuteCommand;
import at.rovo.console.command.ListCommand;
import at.rovo.console.command.LoadCommand;
import at.rovo.console.command.UnloadCommand;
import at.rovo.core.IPluginListener;
import at.rovo.core.InjectionPluginManager;
//import at.rovo.core.SimplePluginManager;
import at.rovo.core.PluginManager;

public class Main implements IPluginListener
{
	public Main()
	{				
		File propFile = new File("application.properties");
		Properties prop = new Properties();
		try 
		{
			prop.load(new FileInputStream(propFile));
			String pluginDir = prop.getProperty("plugin.dir");
			String appDir = System.getProperty("user.dir");
			PluginManager manager = InjectionPluginManager.getInstance();
//			PluginManager manager = SimplePluginManager.getInstance();
			manager.addPluginListener(this);
			manager.setPluginDirectory(appDir+"/"+pluginDir);
			
			new ListCommand(manager);
			new LoadCommand(manager);
			new UnloadCommand(manager);
			new ExecuteCommand(manager);
			
			Console.getInstance().run();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void pluginLoaded(String pluginName)
	{
		
	}

	@Override
	public void pluginRemoved(String pluginName)
	{

	}

	@Override
	public void exception(Exception e)
	{
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Main();
	}
}
