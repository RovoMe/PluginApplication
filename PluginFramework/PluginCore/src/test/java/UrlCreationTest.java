import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;


public class UrlCreationTest
{
    private URL createUrlOfEntry(String path, String name) throws MalformedURLException
    {
        StringBuilder url = new StringBuilder();
        int pos = 0;
        String base = path;
        if (base.contains("!"))
        {
            base = base.substring(0, base.indexOf("!"));
        }

        if (base.endsWith(".jar") || base.endsWith("zip"))
        {
            if (path.startsWith("jar:"))
            {
                // jar:...
                pos = "jar:".length();
            }
            url.append("jar:");
        }

        if (path.substring(pos, pos + "http://".length()).equals("http://"))
        {
            // jar:http://www... or
            // http://www...
            url.append("http://");
            pos = pos + "http://".length();
        }
        else if (path.substring(pos, pos + "https://".length()).equals("https://"))
        {
            // jar:https://www... or
            // https://www...
            url.append("https://");
            pos = pos + "https://".length();
        }
        else if (path.substring(pos, pos + "file:".length()).equals("file:"))
        {
            // jar:file:C:\\Users\\... or
            // file:C:\\Users\\...
            pos = pos + "file:".length();

            if (path.substring(pos, pos + "/".length()).equals("/"))
            {
                // jar:file:/C:\\Users\\... or
                // file:/C:\\Users\\...
                pos = pos + 1;
            }
            url.append("file:/");
        }
        else if (path.subSequence(pos, pos + "/".length()).equals("/"))
        {
            // /C:\\Users\\...
            pos = pos + 1;
            url.append("file:/");
        }
        else
        {
            // C:\\User\\...
            url.append("file:/");
        }


        // we have an archive file
        if (base.endsWith("jar") || base.endsWith("zip"))
        {
            url.append(base.substring(pos));


            url.append("!");
            if (!name.startsWith("/"))
            {
                url.append("/");
            }
            url.append(name);
        }
        // we have a directory
        else
        {
            if (path.endsWith("/"))
            {
                url.append(path.substring(pos, path.length() - 1));
            }
            else
            {
                url.append(path.substring(pos));
            }

            if (!name.startsWith("/"))
            {
                url.append("/");
            }
            url.append(name);
        }

        System.out.println("url: " + url.toString());

        return new URL(url.toString());
    }

    @Test
    public void testUrlCreation() throws MalformedURLException
    {
        URL test;
        URL local = new URL("jar:file:/C:/Users/Roman Vottner/Documents/test.jar!/test/dir/Test.class");
        URL remote = new URL("jar:http://www.foo.org/bar/file.jar!/dir/file.class");
        URL localDir = new URL("file:/C:/Users/Roman Vottner/Documents/test/test/dir/Test.class");

        // path:  jar:file:/C:/some/dir/file.jar!/dir/file.class
        test = this.createUrlOfEntry("jar:file:/C:/Users/Roman Vottner/Documents/test.jar", "/test/dir/Test.class");
        Assert.assertEquals(local, test);

        test = this.createUrlOfEntry("jar:file:/C:/Users/Roman Vottner/Documents/test.jar", "test/dir/Test.class");
        Assert.assertEquals(local, test);

        test = this.createUrlOfEntry("jar:file:C:/Users/Roman Vottner/Documents/test.jar", "test/dir/Test.class");
        Assert.assertEquals(local, test);

        // path:  file:/C/some/dir/file.jar!/dir/file.class
        test = this.createUrlOfEntry("file:C:/Users/Roman Vottner/Documents/test.jar", "/test/dir/Test.class");
        Assert.assertEquals(local, test);

        test = this.createUrlOfEntry("file:/C:/Users/Roman Vottner/Documents/test.jar", "test/dir/Test.class");
        Assert.assertEquals(local, test);

        // path:  /C:/some/dir/file.jar!/dir/file.class
        test = this.createUrlOfEntry("/C:/Users/Roman Vottner/Documents/test.jar", "/test/dir/Test.class");
        Assert.assertEquals(local, test);

        test = this.createUrlOfEntry("/C:/Users/Roman Vottner/Documents/test.jar", "test/dir/Test.class");
        Assert.assertEquals(local, test);

        // path:  C:/some/dir/file.jar!/dir/file.class
        test = this.createUrlOfEntry("C:/Users/Roman Vottner/Documents/test.jar", "/test/dir/Test.class");
        Assert.assertEquals(local, test);

        test = this.createUrlOfEntry("C:/Users/Roman Vottner/Documents/test.jar", "test/dir/Test.class");
        Assert.assertEquals(local, test);


        test = this.createUrlOfEntry("jar:file:/C:/Users/Roman Vottner/Documents/test.jar!/some/dir",
                                     "/test/dir/Test.class");
        Assert.assertEquals(local, test);

        // path:  jar:http://www.foo.com/bar/file.jar!/dir/file.class
        test = this.createUrlOfEntry("jar:http://www.foo.org/bar/file.jar", "/dir/file.class");
        Assert.assertEquals(remote, test);

        test = this.createUrlOfEntry("jar:http://www.foo.org/bar/file.jar", "dir/file.class");
        Assert.assertEquals(remote, test);

        // path:  http://www.foo.com/bar/file.jar!/dir/file.class
        test = this.createUrlOfEntry("http://www.foo.org/bar/file.jar", "/dir/file.class");
        Assert.assertEquals(remote, test);

        test = this.createUrlOfEntry("http://www.foo.org/bar/file.jar", "dir/file.class");
        Assert.assertEquals(remote, test);

        test = this.createUrlOfEntry("C:/Users/Roman Vottner/Documents/test", "test/dir/Test.class");
        Assert.assertEquals(localDir, test);

        test = this.createUrlOfEntry("C:/Users/Roman Vottner/Documents/test/", "test/dir/Test.class");
        Assert.assertEquals(localDir, test);

        test = this.createUrlOfEntry("C:/Users/Roman Vottner/Documents/test", "/test/dir/Test.class");
        Assert.assertEquals(localDir, test);

        test = this.createUrlOfEntry("C:/Users/Roman Vottner/Documents/test/", "/test/dir/Test.class");
        Assert.assertEquals(localDir, test);
    }
}
