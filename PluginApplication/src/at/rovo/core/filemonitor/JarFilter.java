package at.rovo.core.filemonitor;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <p><code>ClassFilter</code> is a simple {@link FilenameFilter} used 
 * for a call of {@link File#list(FilenameFilter)}</p>
 * @author roman
 */
public class JarFilter implements FilenameFilter
{
	@Override
	public boolean accept(File dir, String name)
	{
		if (name.endsWith(".jar") || name.endsWith(".zip"))
			return true;
		else
			return false;
	}

}
