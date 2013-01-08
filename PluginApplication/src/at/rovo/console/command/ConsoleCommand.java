package at.rovo.console.command;

import at.rovo.console.Console;
import at.rovo.core.PluginManager;

public abstract class ConsoleCommand 
{
	protected String command = "";
	protected Console console = null;
	protected PluginManager manager = null;
	
	protected ConsoleCommand(String command, PluginManager manager)
	{
		this.command = command;
		this.console = Console.getInstance();
		this.console.registerCommand(command, this);
		this.manager = manager;
	}
	
	public String getCommand()
	{
		return this.command;
	}
	
	public abstract void execute(String[] token);
}
