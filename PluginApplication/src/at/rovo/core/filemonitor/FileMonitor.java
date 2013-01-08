package at.rovo.core.filemonitor;

import java.util.*;
import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Class for monitoring changes in disk files. Usage: 1. Implement the
 * IDirectoryChangeListener interface. 2. Create a FileMonitor instance. 3. Add the
 * file(s)/directory(ies) to listen for. fileChanged() will be called when a
 * monitored file is created, deleted or its modified time changes.
 */
public class FileMonitor
{
	private Timer timer;
	private HashMap<File, Long> files;
	private Collection<WeakReference<IDirectoryChangeListener>> listeners;
	
	public FileMonitor(long pollingInterval, File dir)
	{
		this.files = new HashMap<File, Long>();
		this.listeners = new ArrayList<WeakReference<IDirectoryChangeListener>>();

		this.timer = new Timer(true);
		this.timer.schedule(new FileMonitorNotifier(this.files, this.listeners, dir), 0, pollingInterval);
	}

	public void stop()
	{
		this.timer.cancel();
	}

	public void addFile(File file)
	{
		if (!this.files.containsKey(file))
		{
			long modifiedTime = file.exists() ? file.lastModified() : -1;
			this.files.put(file, new Long(modifiedTime));
		}
	}

	public void removeFile(File file)
	{
		this.files.remove(file);
	}

	public void addListener(IDirectoryChangeListener IDirectoryChangeListener)
	{
		// Don't add if its already there
		for (WeakReference<IDirectoryChangeListener> reference : this.listeners)
		{
			IDirectoryChangeListener listener = reference.get();
			if (listener == IDirectoryChangeListener)
				return;
		}

		// Use WeakReference to avoid memory leak if this becomes the
		// sole reference to the object.
		this.listeners.add(new WeakReference<IDirectoryChangeListener>(IDirectoryChangeListener));
	}

	public void removeListener(IDirectoryChangeListener IDirectoryChangeListener)
	{
		for (WeakReference<IDirectoryChangeListener> reference : this.listeners)
		{
			IDirectoryChangeListener listener = reference.get();
			if (listener == IDirectoryChangeListener)
			{
				this.listeners.remove(listener);
				break;
			}
		}
	}
}