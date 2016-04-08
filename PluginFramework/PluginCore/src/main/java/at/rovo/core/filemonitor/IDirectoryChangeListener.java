package at.rovo.core.filemonitor;

import java.io.File;

/**
 * Provides a hook for handling file modification events.
 *
 * @author Roman Vottner
 */
public interface IDirectoryChangeListener
{
    /**
     * <code>fileChanged()</code> is called when a file inside a directory is changed.
     *
     * @param file
     *         File which was either modified, deleted or created
     * @param changeAction
     *         Determines what happened with the file.
     */
    void fileChanged(File file, FileAction changeAction);
}
