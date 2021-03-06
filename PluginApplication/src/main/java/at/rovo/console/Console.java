package at.rovo.console;

import at.rovo.console.command.ConsoleCommand;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>Console</code> is a singleton class representing a console in- and output.
 * <p/>
 * It allows dynamic manipulation of console commands via {@link #registerCommand(String, ConsoleCommand)} and {@link
 * #unregisterCommand(String)}.
 *
 * @author Roman Vottner
 * @version 0.2
 */
public enum Console implements Runnable
{
    /** Application of the enum singleton pattern **/
    INSTANCE;

    /** The registered commands **/
    private Map<String, ConsoleCommand> registeredCommands = null;

    /**
     * Creates a new instance of this class.
     */
    Console()
    {
        this.registeredCommands = new ConcurrentHashMap<>();
    }

    /**
     * Registers a new {@link ConsoleCommand} under a provided name with the console.
     *
     * @param command
     *         The command to be registered with the console
     * @param action
     *         The {@link ConsoleCommand} object representing the execution logic for the registered command
     */
    public void registerCommand(String command, ConsoleCommand action)
    {
        if (action == null)
        {
            throw new IllegalArgumentException("No defined action for command " + command);
        }

        if (!this.registeredCommands.containsKey(command))
        {
            this.registeredCommands.put(command, action);
        }
    }

    /**
     * Unregisters a certain command from the console.
     *
     * @param command
     *         The command to remove from the console
     */
    public void unregisterCommand(String command)
    {
        if (this.registeredCommands.containsKey(command))
        {
            this.registeredCommands.remove(command);
        }
    }

    /**
     * Starts the console and listens to inputs made by the user. If a known command was found, the execution of this
     * commands delegate object is invoked.
     */
    public void run()
    {
        Scanner scanner = new Scanner(System.in);
        String command = "";
        System.out.println("Application startet - type 'quit' to exit");
        while (!command.equals("quit"))
        {
            String[] token = command.split(" ");
            // token[0] contains the command in case of more than one token
            // f.e. 'load 1' or 'exec 3'; the command is looked up in the
            // local map of registered commands and if it finds a command
            // it triggers its execution
            try
            {
                if (this.registeredCommands.containsKey(token[0]))
                {
                    this.registeredCommands.get(token[0]).execute(token);
                }
                else if (token[0] != null && !token[0].equals(""))
                {
                    System.out.println("Unknown command: " + token[0]);
                }
            }
            catch (Exception e)
            {
                System.err.println(e.getLocalizedMessage());
                // give the error-stream a bit more time to write its output
                // for some reason this stream is much slower than System.out
                try
                {
                    Thread.sleep(20);
                }
                catch (InterruptedException e1)
                {
                    // ignored
                }
            }
            System.out.print("app > ");
            command = scanner.nextLine();
        }
        scanner.close();
    }
}
