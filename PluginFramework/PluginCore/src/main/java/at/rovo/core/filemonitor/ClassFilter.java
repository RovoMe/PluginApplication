package at.rovo.core.filemonitor;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <p><code>JarFilter</code> is a simple {@link FilenameFilter} used 
 * for a call of {@link File#list(FilenameFilter)}</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class ClassFilter implements FilenameFilter
{
	@Override
	public boolean accept(File dir, String name)
	{
		if (name.endsWith(".class"))
			return true;
		else
			return false;
	}

}
