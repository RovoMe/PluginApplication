package at.rovo.console.command;

import java.util.Set;
import at.rovo.core.PluginManager;

/**
 * <p><code>ListCommand</code> lists all currently registered plug-ins
 * in the {@link Console} the instance is registered with.</p>
 * <p>If no command was specified 'list' is taken as default command.</p>
 * <p>Note that multiple instances with different names can be
 * <p>Example:</p>
 * <code>
 * <ul>
 * <li>list</li>
 * </ul>
 * </code>
 * created to register the same action to different commands.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class ListCommand extends ConsoleCommand
{
	/**
	 * <p>Creates a new instance of this class and sets its command
	 * the {@link Console} will listen to to 'list'.</p>
	 * 
	 * @param manager The {@link PluginManager} who takes care of 
	 *                the plug-ins
	 */
	public ListCommand(PluginManager manager)
	{
		super("list", manager);
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
	public ListCommand(String command, PluginManager manager) 
	{
		super(command, manager);
	}

	@Override
	public void execute(String[] token) 
	{
		// get the names of all currently loaded plug-ins
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
