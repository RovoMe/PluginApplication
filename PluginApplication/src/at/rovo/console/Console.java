package at.rovo.console;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import at.rovo.console.command.ConsoleCommand;
import at.rovo.core.IPluginListener;

public class Console implements IPluginListener
{
	private static Console console = null;
	private Map<String, ConsoleCommand> registeredCommands = null;
	
	private Console()
	{
		this.registeredCommands = new HashMap<String, ConsoleCommand>();
	}
	
	public static Console getInstance()
	{
		if (console == null)
			console = new Console();
		return console;
	}
	
	public void registerCommand(String command, ConsoleCommand action)
	{
		if (action == null)
			throw new IllegalArgumentException("No defined action for command "+command);
		
		if (!this.registeredCommands.containsKey(command))
			this.registeredCommands.put(command, action);
	}
	
	public void unregisterCommand(String command)
	{
		if (this.registeredCommands.containsKey(command))
			this.registeredCommands.remove(command);
	}
	
	public void run()
	{
		Scanner scanner = new Scanner(System.in);
		String command = "";
		System.out.println("Application startet - type 'quit' to exit");
		while (!command.equals("quit"))
		{
			String[] token = command.split(" ");
			if (this.registeredCommands.containsKey(token[0]))
				this.registeredCommands.get(token[0]).execute(token);
			else if (token[0] != "")
				System.out.println("Unknown command: "+token[0]);
			
			System.out.print("app > ");
			command = scanner.nextLine();
		}
		System.out.println("Application exited");
	}

	@Override
	public void pluginLoaded(String pluginName)
	{

	}

	@Override
	public void pluginRemoved(String pluginName)
	{
		System.out.println(pluginName+" got removed");
	}
	
	@Override
	public void exception(Exception e)
	{
		e.printStackTrace();
	}
}
