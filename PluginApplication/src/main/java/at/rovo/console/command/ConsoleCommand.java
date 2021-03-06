package at.rovo.console.command;

import at.rovo.console.Console;
import at.rovo.core.PluginManager;

/**
 * <code>ConsoleCommand</code> is the basic class for any {@link Console} command. Objects wanting to serve as a console
 * command have to extend this class and override {@link #execute(String[])}.
 *
 * @author Roman Vottner
 * @version 0.1
 */
public abstract class ConsoleCommand
{
    /** The instance's command **/
    protected String command = "";
    /** The reference to the executing console the command is registered with **/
    protected Console console = null;
    /** The plug-in manager to allow modifications or execution of plug-ins **/
    protected PluginManager manager = null;

    /**
     * Initializes basic fields for console commands and registers the command with the console.
     *
     * @param command
     *         The command the object should be registered with
     * @param manager
     *         The instance of the {@link PluginManager}
     */
    protected ConsoleCommand(String command, PluginManager manager)
    {
        this.command = command;
        this.console = Console.INSTANCE;
        this.console.registerCommand(command, this);
        this.manager = manager;
    }

    /**
     * Returns the currently assigned console command for this object.
     *
     * @return The console command for this object
     */
    public String getCommand()
    {
        return this.command;
    }

    /**
     * Executes the business logic for this command.
     *
     * @param token
     *         The full command as entered by the user as an array of blank separated tokens
     */
    public abstract void execute(String[] token);
}
