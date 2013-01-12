package at.rovo.console.command;

import java.io.File;
import at.rovo.core.PluginManager;

/**
 * <p><code>LoadCommand</code> invokes loading of a .jar-file containing 
 * a {@link IPlugin} implementation.</p>
 * <p>Loading can be invoked by either providing the absolute path 
 * to the plug-in containing JAR file or the plug-in directory specified
 * in the application is taken on prepending './' or '.\' to the JAR
 * file located in the plug-in directory.</p>
 * <p>If no command was specified 'load' is taken as default command.</p>
 * <p>Examples:</p>
 * <code>
 * <ul>
 * <li>load C:\some\Directory\pluginContainingJar.jar</li>
 * <li>load ./jarInPluginDirectory.jar</li>
 * </ul>
 * </code>
 * <p>In the above examples the jar file-types ('.jar') can be omitted.</p>
 * <p>Note that multiple instances with different names can be
 * created to register the same action to different commands.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class LoadCommand extends ConsoleCommand
{
	/**
	 * <p>Creates a new instance of this class and sets its command
	 * the {@link Console} will listen to to 'load'.</p>
	 * 
	 * @param manager The {@link PluginManager} who takes care of 
	 *                the plug-ins
	 */
	public LoadCommand(PluginManager manager)
	{
		super("load", manager);
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
	protected LoadCommand(String command, PluginManager manager) 
	{
		super(command, manager);
	}

	@Override
	public void execute(String[] token) 
	{
		// Check if the name contains blanks
		if (token.length != 2 && token[1].startsWith("'"))
		{
			// As the name of a plug-in may contain blanks connect them
			// load 'C:\Path to the Application\...\PluginApplication\plugin\SimplePlugin'
			String name = token[1];
			int i;
			for (i=2; i<token.length && !token[i-1].endsWith("'"); i++)
			{
				name+=" "+token[i];
			}
			name = name.replaceAll("'", "");
			// if no jar-extension has been specified add it!
			if (!name.endsWith(".jar"))
				name += ".jar";

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
			// check if a plug-in in the applications' plug-in directory should be loaded
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
