package at.rovo.console.command;

import at.rovo.common.plugin.IPlugin;
import at.rovo.console.Console;
import at.rovo.core.PluginManager;
import java.util.Set;

/**
 * <code>ExecuteCommand</code> executes a registered plug-in. The execution can be triggered either by providing the
 * current Id of the plug-in, as provided by {@link ListCommand} or by the fully-qualified class name of the plug-in
 * instance.
 * <p/>
 * If no command was specified 'exec' is taken as default command.
 * <p/>
 * Examples:
 * <code>
 *     <ul>
 *         <li>exec 1</li>
 *         <li>exec at.rovo.plugin.somePlugin</li>
 *     </ul>
 * </code> Note that multiple
 * instances with different names can be created to register the same action to different commands.
 *
 * @author Roman Vottner
 * @version 0.1
 */
public class ExecuteCommand extends ConsoleCommand
{
    /**
     * Creates a new instance of this class and sets its command the {@link Console} will listen to to 'exec'.
     *
     * @param manager
     *         The {@link PluginManager} who takes care of the plug-ins
     */
    public ExecuteCommand(PluginManager manager)
    {
        super("exec", manager);
    }

    /**
     * Creates a new instance of this class and sets its command the {@link Console} will listen to to the specified
     * command.
     *
     * @param command
     *         The command this instance should be registered with in the {@link Console}
     * @param manager
     *         The {@link PluginManager} who takes care of the plug-ins
     */
    protected ExecuteCommand(String command, PluginManager manager)
    {
        super(command, manager);
    }

    @Override
    public void execute(String[] token)
    {
        if (token.length != 2)
        {
            System.out.println(
                    "Invalid call! Usage: 'exec id' or 'exec plugin.name' where plugin.name is the name of the plugin to execute");
        }
        else
        {
            try
            {
                // Convert 2nd argument into an integer - if the parsing succeeds
                // the execution is triggered by its current Id, else the fully
                // qualified name was provided
                int num = Integer.parseInt(token[1].trim());
                // and get all currently loaded plug-ins
                Set<String> plugins = this.manager.getLoadedPlugins();
                String[] plugin = {};
                if (num > 0 && num <= plugins.size())
                {
                    plugin = plugins.toArray(plugin);
                    IPlugin p = this.manager.getNewPluginInstance(plugin[num - 1]);
                    if (p != null)
                    {
                        p.execute();
                    }
                    else
                    {
                        System.out.println("Could not instantiate the plug-ins' main class '" + plugin[num - 1] + "'!");
                    }
                }
                else
                {
                    System.out.println("No plugin found with id: " + num);
                }
            }
            catch (NumberFormatException nfE)
            {
                // a plug-in name was passed as 2nd argument
                IPlugin p = this.manager.getNewPluginInstance(token[1]);
                if (p != null)
                {
                    p.execute();
                }
                else
                {
                    System.out.println("Could not instantiate the plug-ins' main class!");
                }
            }
        }
    }
}
