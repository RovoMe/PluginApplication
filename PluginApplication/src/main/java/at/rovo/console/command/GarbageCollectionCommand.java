package at.rovo.console.command;

import at.rovo.core.PluginManager;

public class GarbageCollectionCommand extends ConsoleCommand 
{
	public GarbageCollectionCommand(PluginManager manager)
	{
		super("gc", manager);
	}
	
	protected GarbageCollectionCommand(String command, PluginManager manager) 
	{
		super(command, manager);
	}

	@Override
	public void execute(String[] token) 
	{
		System.gc();
	}
}
