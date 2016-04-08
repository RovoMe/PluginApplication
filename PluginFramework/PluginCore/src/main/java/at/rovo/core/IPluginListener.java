package at.rovo.core;

import at.rovo.common.plugin.IPlugin;

/**
 * If a class needs information on loading, unloading or certain exceptions the {@link IPlugin} instance has thrown it
 * has to implement this interface to be notified when those events occur.
 *
 * @author Roman Vottner
 */
public interface IPluginListener
{
    /**
     * Is triggered after a {@link IPlugin} object was successfully loaded by the {@link PluginManager}.
     *
     * @param pluginName
     *         The name of the {@link IPlugin} which got loaded
     */
    void pluginLoaded(String pluginName);

    /**
     * Is triggered after a {@link IPlugin} object was successfully removed by the {@link PluginManager}.
     *
     * @param pluginName
     *         The name of the {@link IPlugin} which was removed from the system.
     */
    void pluginRemoved(String pluginName);

    /**
     * Is triggered on catching any exceptions while loading the {@link IPlugin} instance.
     *
     * @param pluginName
     *         The name of the {@link IPlugin} which produced the exception
     * @param e
     *         The exception thrown by the {@link PluginManager} while loading the plug-in
     */
    void exception(String pluginName, Exception e);
}
