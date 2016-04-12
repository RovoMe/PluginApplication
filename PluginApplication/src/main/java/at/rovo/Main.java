package at.rovo;

import at.rovo.console.Console;
import at.rovo.console.command.ConsoleCommand;
import at.rovo.console.command.ExecuteCommand;
import at.rovo.console.command.GarbageCollectionCommand;
import at.rovo.console.command.ListCommand;
import at.rovo.console.command.LoadCommand;
import at.rovo.console.command.UnloadCommand;
import at.rovo.core.IPluginListener;
import at.rovo.core.InjectionPluginManager;
import at.rovo.core.PluginManager;
import at.rovo.core.injection.InjectionControllerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the entrance point into the plug-in application. It creates and holds the {@link PluginManager}
 * instance and adds {@link ConsoleCommand}s to the underlying {@link Console} object.
 * <p/>
 * Note that this application is not thread safe and only intended for a single stand-alone application.
 *
 * @author Roman Vottner
 * @version 0.2
 */
public class Main implements IPluginListener
{
    /** The logger of this class **/
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Creates a new instance of this class, adds a {@link PluginManager} instance to the application and starts a
     * {@link Console} to listen to user commands.
     */
    public Main(String pluginDir)
    {
        URL url = getClass().getResource("/application.properties");
        String file = url.getFile().replace("%20", " ");
        File propFile = new File(file);

        Properties prop = new Properties();
        try
        {
            // initialize the injection controller here as a delayed initialization in the instantiated plug-in will tie
            // the classloader to the cleanup-thread and therefore create a memory leak as the classloader as well as
            // all the loaded classes can't be garbage collected!
            logger.log(Level.FINEST, "Initialized {0}", new Object[] { InjectionControllerImpl.INSTANCE });
            // create a new PluginManager
            PluginManager manager = InjectionPluginManager.getInstance();
            manager.addPluginListener(this);

            if (pluginDir == null || "".equals(pluginDir))
            {
                // load the application data which tells us where plug-ins
                // should be found
                prop.load(new FileInputStream(propFile));

                pluginDir = prop.getProperty("plugin.dir");
                String appDir = System.getProperty("user.dir");
                manager.setPluginDirectory(appDir + "/" + pluginDir);
            }
            else
            {
                manager.setPluginDirectory(pluginDir);
            }

            // register new commands with the console
            new ListCommand(manager);
            new LoadCommand(manager);
            new UnloadCommand(manager);
            new ExecuteCommand(manager);
            new GarbageCollectionCommand(manager);

            logger.log(Level.INFO, "Application initialized");

            // start the console
            Thread console = new Thread(Console.INSTANCE);
            console.setName("Console");
            console.start();
            // wait on the console to finish
            console.join();

            // cleanup
            manager.close();
            System.out.println("Application exited");
        }
        catch (Exception e)
        {
            System.err.println(e.getLocalizedMessage());
        }
    }

    @Override
    public void pluginLoaded(String pluginName)
    {
        logger.log(Level.INFO, "{0} got added!", new Object[] {pluginName});
    }

    @Override
    public void pluginRemoved(String pluginName)
    {
        logger.log(Level.INFO, "{0} was removed!", new Object[] {pluginName});
    }

    @Override
    public void exception(String pluginName, Exception e)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Exception occurred: ");
        sb.append(e.getClass().getName());
        sb.append("\n");
        for (StackTraceElement elem : e.getStackTrace())
        {
            sb.append("\t");
            sb.append(elem.getClassName());
            sb.append("::");
            sb.append(elem.getMethodName());
            sb.append(" at ");
            sb.append(elem.getLineNumber());
            sb.append("\n");
        }
        logger.log(Level.WARNING, "Caught exception: {0} for {1}\n {2}",
                   new Object[] {e.getLocalizedMessage(), pluginName, sb.toString()});
    }

    /**
     * Entrance point for this application.
     *
     * @param args
     *         Arguments passed to the application; This application accepts a --pluginDir parameter where a directory
     *         can be specified that will hold plugin jars to load. If no parameter is provided the application will
     *         default to the value provided in the application.properties file.
     */
    public static void main(String[] args)
    {
        String pluginDir = "";
        for (String arg : args)
        {
            if (arg.startsWith("--pluginDir="))
            {
                pluginDir = arg.substring(arg.indexOf("=") + 1);
            }
        }

        new Main(pluginDir);
    }
}
