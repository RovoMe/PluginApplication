package at.rovo.console.command;

import java.io.File;
import at.rovo.core.PluginManager;

public class LoadCommand extends ConsoleCommand
{
	public LoadCommand(PluginManager manager)
	{
		super("load", manager);
	}
	
	protected LoadCommand(String command, PluginManager manager) 
	{
		super(command, manager);
	}

	@Override
	public void execute(String[] token) 
	{
		// Check if the name contains whitespaces
		if (token.length != 2 && token[1].startsWith("'"))
		{
			// As the name of a plug-in may contain whitespaces connect them
			// load 'C:\Path to the Application\...\PluginApplication\plugin\SimplePlugin'
			String name = token[1];
			int i;
			for (i=2; i<token.length && !token[i-1].endsWith("'"); i++)
			{
				name+=" "+token[i];
//				System.out.println(name);
			}
			name = name.replaceAll("'", "");
			// if no jar-extension has been specified add it!
			if (!name.endsWith(".jar"))
				name += ".jar";
//			System.out.println("Plugin-name: "+name);
			if (i == token.length)
			{
				File file = new File(name);
				if (file.exists())
				{
					this.manager.reloadPlugin(file);
					System.out.println("Plugin loaded: "+this.manager.getPluginNameBasedOnJar(name));
				}
				else
					System.out.println("No plugin found to load with name: "+name);
			}
			else
			{
				System.out.println("Invalid call! Usage: 'load plugin' where plugin is the path and the name of the plugin to load");
			}
		}
		else if (token.length != 2)
		{
			System.out.println("Invalid call! Usage: 'load plugin' where plugin is the path and the name of the plugin to load");
		}
		else
		{
			String name = token[1];
			// if no jar-extension has been specified add it!
			if (!name.endsWith(".jar"))
				name += ".jar";
			if (name.startsWith("./") || name.startsWith(".\\"))
				name = this.manager.getPluginDirectory()+"/"+name.substring(2);
			File file = new File(name);
			if (file.exists())
			{
				this.manager.reloadPlugin(file);
				System.out.println("Plugin loaded: "+this.manager.getPluginNameBasedOnJar(name));
			}
			else
				System.out.println("No plugin found to load with name: "+name);
		}
	}

}
