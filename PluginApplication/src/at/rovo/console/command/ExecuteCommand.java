package at.rovo.console.command;

import java.util.Set;

import at.rovo.core.PluginManager;
import at.rovo.plugin.IPlugin;

public class ExecuteCommand extends ConsoleCommand
{
	public ExecuteCommand(PluginManager manager)
	{
		super("exec", manager);
	}

	protected ExecuteCommand(String command, PluginManager manager) 
	{
		super(command, manager);
	}

	@Override
	public void execute(String[] token) 
	{
		if (token.length != 2)
		{
			System.out.println("Invalid call! Usage: 'exec id' or 'exec plugin.name' where plugin.name is the name of the plugin to execute");
		}
		else
		{
			try
			{
				// Convert 2nd argument into an integer
				int num = Integer.parseInt(token[1]);
				// and get all loaded plug-ins
				Set<String> plugins = this.manager.getLoadedPlugins();
				String[] plugin = {};
				if (num > 0 && num <= plugins.size())
				{
					plugin = plugins.toArray(plugin);
					IPlugin p = this.manager.getNewPluginInstance(plugin[num-1]);
					if (p != null)
						p.execute();
					else
						System.out.println("Could not instantiate the plug-ins' main class '"+plugin[num-1]+"'!");
				}
				else
					System.out.println("No plugin found with id: "+num);
			}
			catch (NumberFormatException nfE)
			{
				// a plug-in name was passed as 2nd argument
				IPlugin p = this.manager.getNewPluginInstance(token[1]);
				if (p != null)
					p.execute();
				else
					System.out.println("Could not instantiate the plug-ins' main class!");
			}
		}	
	}

}
