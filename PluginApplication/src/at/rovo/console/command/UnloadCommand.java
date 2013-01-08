package at.rovo.console.command;

import java.util.Set;
import at.rovo.core.PluginManager;

public class UnloadCommand extends ConsoleCommand
{
	public UnloadCommand(PluginManager manager)
	{
		super("unload", manager);
	}
	
	public UnloadCommand(String command, PluginManager manager) 
	{
		super(command, manager);
	}

	@Override
	public void execute(String[] token) 
	{
		if (token.length != 2)
		{
			System.out.println("Invalid call! Usage: 'unload id' or 'unload plugin.name' where plugin.name is the name of the plugin to unload");
		}
		else
		{
			try
			{
				// Convert 2nd argument into an integer
				int num = Integer.parseInt(token[1]);
				// and get all loaded plug-ins
				Set<String> plugins = manager.getLoadedPlugins();
				String[] plugin = {};
				if (num > 0 && num <= plugins.size())
				{
					plugin = plugins.toArray(plugin);
					this.manager.unload(plugin[num-1]);
					System.out.println("Plugin unloaded: "+plugin[num-1]);
				}
				else
					System.out.println("No plugin found with id: "+num);
			}
			catch (NumberFormatException nfE)
			{
				// a plug-in name was passed as 2nd argument
				this.manager.unload(token[1]);
				System.out.println("Plugin unloaded: "+token[1]);
			}
		}
	}

}
