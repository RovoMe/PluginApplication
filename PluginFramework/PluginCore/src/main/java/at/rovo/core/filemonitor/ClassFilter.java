package at.rovo.core.filemonitor;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <code>JarFilter</code> is a simple {@link FilenameFilter} used for a call of {@link File#list(FilenameFilter)}
 *
 * @author Roman Vottner
 */
public class ClassFilter implements FilenameFilter
{
    @Override
    public boolean accept(File dir, String name)
    {
        return name.endsWith(".class");
    }

}
