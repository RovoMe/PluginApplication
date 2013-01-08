package at.rovo.core.filemonitor;

import java.io.File;


public interface IDirectoryChangeListener
{
	/**
	 * <p>Possible actions that could happen to a file.</p>
	 */
	public enum FileAction { FILE_CREATED, FILE_MODIFIED, FILE_DELETED };
	
	/**
	 * <p><code>fileChanged()</code> is called when a file 
	 * inside a directory is changed</p>
	 * @param file File which was either modified, deleted or created
	 * @param changeAction Determines what happened with the file. 
	 */
	public void fileChanged(File file, FileAction changeAction);
}
