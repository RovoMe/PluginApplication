package at.rovo.console.command;

import java.util.Set;
import at.rovo.core.PluginManager;

/**
 * <p><code>UnloadCommand</code> invokes unloading of a currently loaded 
 * {@link IPlugin}. It therefore either takes the current Id of the loaded
 * plug-in or the fully-qualified name of the plug-in.</p>
 * <p>If no command was specified 'unload' is taken as default command.</p>
 * <p>Examples:</p>
 * <code>
 * <ul>
 * <li>unload 1</li>
 * <li>unload at.rovo.plugin.IPluginImplementingClass</li>
 * </ul>
 * </code>
 * <p>Note that multiple instances with different names can be
 * created to register the same action to different commands.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class UnloadCommand extends ConsoleCommand
{
	/**
	 * <p>Creates a new instance of this class and sets its command
	 * the {@link Console} will listen to to 'unload'.</p>
	 * 
	 * @param manager The {@link PluginManager} who takes care of 
	 *                the plug-ins
	 */
	public UnloadCommand(PluginManager manager)
	{
		super("unload", manager);
	}
	
	/**
	 * <p>Creates a new instance of this class and sets its command
	 * the {@link Console} will listen to to the specified command.</p>
	 * 
	 * @param command The command this instance should be registered
	 *                with in the {@link Console}
	 * @param manager The {@link PluginManager} who takes care of 
	 *                the plug-ins
	 */
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
					System.gc();
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
