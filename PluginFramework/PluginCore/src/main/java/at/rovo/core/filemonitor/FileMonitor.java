package at.rovo.core.filemonitor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <Class for monitoring changes in disk files.
 * <p/>
 * Usage:
 * <ol>
 *     <li>Implement the {@link IDirectoryChangeListener} interface</li>
 *     <li>Create a {@link FileMonitor} instance</li>
 *     <li>Add the file(s)/directory(ies) to listen for</li>
 * </ol>
 * <p/>
 * {@link IDirectoryChangeListener#fileChanged(File, FileAction)} will be called when a monitored file is created,
 * deleted or its modified time changes.
 *
 * @author Roman Vottner
 */
public class FileMonitor
{
    /** The timer object to schedule the notifier in certain intervals **/
    private Timer timer;
    /** The map to keep track of files and the time of their last modification **/
    private Map<File, Long> files;
    /** A set of registered listeners who want to be notified on file changes **/
    private Set<IDirectoryChangeListener> listeners;

    /**
     * Creates a new instance and starts monitoring of the defined directory within the specified time interval
     *
     * @param pollingInterval
     *         The time interval used to execute the monitoring
     * @param dir
     *         A file or directory to monitor
     */
    public FileMonitor(long pollingInterval, File dir)
    {
        this.files = new HashMap<>();
        this.listeners = new CopyOnWriteArraySet<>();

        this.timer = new Timer(true);
        this.timer.schedule(new FileMonitorNotifier(this.files, this.listeners, dir), 0, pollingInterval);
    }

    /**
     * Stops the monitoring of the specified file/s.
     */
    public void stop()
    {
        this.timer.cancel();
    }

    /**
     * Adds a new file or directory to monitor.
     *
     * @param file
     *         A new file or directory to monitor
     */
    public void addFile(File file)
    {
        if (!this.files.containsKey(file))
        {
            long modifiedTime = file.exists() ? file.lastModified() : -1;
            this.files.put(file, modifiedTime);
        }
    }

    /**
     * Removes a file or directory from the list of files or list to monitor.
     *
     * @param file
     *         The file or directory to be removed from being monitored
     */
    public void removeFile(File file)
    {
        if (this.files.containsKey(file))
        {
            this.files.remove(file);
        }
    }

    /**
     * Adds an object which implements {@link IDirectoryChangeListener} and marks interest on file or directory
     * modifications.
     *
     * @param listener
     *         The object which has interest on being notified on file or directory modifications
     */
    public void addListener(IDirectoryChangeListener listener)
    {
        if (!this.listeners.contains(listener))
        {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes an object from the set of objects to be notified on file or directory modifications.
     *
     * @param listener
     *         The object to remove from the set of objects to notify on file or directory modifications
     */
    public void removeListener(IDirectoryChangeListener listener)
    {
        if (this.listeners.contains(listener))
        {
            this.listeners.remove(listener);
        }
    }
}