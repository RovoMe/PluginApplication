package at.rovo.console.command;

import java.util.Set;
import at.rovo.core.PluginManager;

public class ListCommand extends ConsoleCommand
{
	
	public ListCommand(PluginManager manager)
	{
		super("list", manager);
	}

	public ListCommand(String command, PluginManager manager) 
	{
		super(command, manager);
	}

	@Override
	public void execute(String[] token) 
	{
		Set<String> plugins = manager.getLoadedPlugins();
		if (plugins.size() > 0)
		{
			int pluginNr = 1;
			System.out.println("Currently loaded plug-ins");
			for (String plugin : plugins)
				System.out.println("\t"+ pluginNr++ +"\t"+plugin);
		}
		else
			System.out.println("No plugins loaded");
	}
}
