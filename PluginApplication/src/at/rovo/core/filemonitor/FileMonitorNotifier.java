package at.rovo.core.filemonitor;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimerTask;

public class FileMonitorNotifier extends TimerTask
{
	private HashMap<File, Long> files = null;
	private Collection<WeakReference<IDirectoryChangeListener>> listeners = null;
	private File directory = null;
	
	public FileMonitorNotifier(HashMap<File, Long> files, Collection<WeakReference<IDirectoryChangeListener>> listener, File directory)
	{
		this.files = files;
		this.listeners = listener;
		this.directory = directory;
	}
	
	public void run()
	{
		// Loop over the registered files and see which have changed.
		// Use a copy of the list in case listener wants to alter the
		// list within its fileChanged method.
		Collection<File> files = new HashSet<File>(this.files.keySet());

		for (File file : files)
		{
			if (file == null || !file.exists())
			{
				notifyListener(file, IDirectoryChangeListener.FileAction.FILE_DELETED);
				this.files.remove(file);
			}
			else
			{
				long lastModifiedTime = this.files.get(file);
				long newModifiedTime = file.exists() ? file.lastModified() : -1;
							// Chek if file has changed
				if (newModifiedTime != lastModifiedTime)
				{
					// Register new modified time
					this.files.put(file, new Long(newModifiedTime));
					notifyListener(file, IDirectoryChangeListener.FileAction.FILE_MODIFIED);
				}
			}
		}
		
		// Fetch all files in the directory and store them in a Collection
		File[] curFiles = this.directory.listFiles(new JarFilter());
		Collection<File> curFileList = new HashSet<File>();
		for (File file : curFiles)
			curFileList.add(file);
		// Remove those files that are already been watched
		curFileList.removeAll(files);
		// curFileList now contains all newly created files
		for (File newFile : curFileList)
		{
			this.files.put(newFile, newFile.lastModified());
			notifyListener(newFile, IDirectoryChangeListener.FileAction.FILE_CREATED);
		}
	}
	
	private void notifyListener(File file, IDirectoryChangeListener.FileAction fileAction)
	{
		for (WeakReference<IDirectoryChangeListener> reference : this.listeners)
		{
			IDirectoryChangeListener listener = reference.get();

			// Remove from list if the back-end object has been GC'd
			if (listener == null)
				this.listeners.remove(listener);
			else
				listener.fileChanged(file, fileAction);
		}
	}
}