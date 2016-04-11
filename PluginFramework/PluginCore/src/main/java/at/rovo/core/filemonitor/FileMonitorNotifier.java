package at.rovo.core.filemonitor;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

/**
 * <code>FileMonitorNotifier</code> is the worker object for the {@link FileMonitor}. It is executed on a regular basis
 * defined by the polling interval of {@link FileMonitor#FileMonitor(long, File)}.
 *
 * @author Roman Vottner
 */
class FileMonitorNotifier extends TimerTask
{
    /** The map to keep track of files and the time of their last modification **/
    private Map<File, Long> files = null;
    /** A set of registered listeners who want to be notified on file changes **/
    private Set<IDirectoryChangeListener> listeners = null;
    /** The file or directory to monitor **/
    private File directory = null;

    /**
     * Creates a new instance and initializes required fields.
     *
     * @param files
     *         A mapping of files or directories and a timestamp of their last modification
     * @param listener
     *         A set of objects that spark interest on file modifications
     * @param directory
     *         The directory to monitor
     */
    FileMonitorNotifier(Map<File, Long> files, Set<IDirectoryChangeListener> listener, File directory)
    {
        this.files = files;
        this.listeners = listener;
        this.directory = directory;
    }

    /**
     * Executes the monitoring of a defined directory.
     */
    public void run()
    {
        // Loop over the registered files and see which have changed. Use a copy of the list in case listener wants to
        // alter the list within its fileChanged method.
        Set<File> files = new HashSet<>(this.files.keySet());

        for (File file : files)
        {
            if (file == null || !file.exists())
            {
                notifyListener(file, FileAction.FILE_DELETED);
                this.files.remove(file);
            }
            else
            {
                long lastModifiedTime = this.files.get(file);
                long newModifiedTime = file.exists() ? file.lastModified() : -1;
                // Check if the file has changed
                if (newModifiedTime != lastModifiedTime)
                {
                    // Register new modified time
                    this.files.put(file, newModifiedTime);
                    notifyListener(file, FileAction.FILE_MODIFIED);
                }
            }
        }

        // Fetch all files in the directory and store them in a Collection
        File[] curFiles = this.directory.listFiles(new JarFilter());
        Set<File> curFileList = new HashSet<>();
        curFileList.addAll(Arrays.asList(curFiles));
        // Remove those files that are already been watched
        curFileList.removeAll(files);
        // curFileList now contains all newly created files
        for (File newFile : curFileList)
        {
            this.files.put(newFile, newFile.lastModified());
            notifyListener(newFile, FileAction.FILE_CREATED);
        }
    }

    /**
     * Notifies all registered objects of a file modification.
     *
     * @param file
     *         The file which has been modified
     * @param fileAction
     *         The action that triggered the file modification
     */
    private void notifyListener(File file, FileAction fileAction)
    {
        for (IDirectoryChangeListener listener : this.listeners)
        {
            listener.fileChanged(file, fileAction);
        }
    }
}