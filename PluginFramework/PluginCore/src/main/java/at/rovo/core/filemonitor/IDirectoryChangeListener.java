package at.rovo.core.filemonitor;

import java.io.File;

/**
 * <p>Provides a hook for handling file modification events.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public interface IDirectoryChangeListener
{	
	/**
	 * <p><code>fileChanged()</code> is called when a file 
	 * inside a directory is changed</p>
	 * @param file File which was either modified, deleted or created
	 * @param changeAction Determines what happened with the file. 
	 */
	public void fileChanged(File file, FileAction changeAction);
}
