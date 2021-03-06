package at.rovo.core.filemonitor;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <code>ClassFilter</code> is a simple {@link FilenameFilter} used for a call of {@link File#list(FilenameFilter)}.
 *
 * @author Roman Vottner
 */
public class JarFilter implements FilenameFilter
{
    @Override
    public boolean accept(File dir, String name)
    {
        return (name.endsWith(".jar") || name.endsWith(".zip"));
    }
}
