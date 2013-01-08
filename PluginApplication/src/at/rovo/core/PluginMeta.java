package at.rovo.core;

import java.net.MalformedURLException;
import java.net.URL;

import at.rovo.plugin.IPlugin;

public class PluginMeta
{
	private String name = null;
	private String declaredClass = null;
	private ClassLoader classLoader = null;
	private URL jarFile = null;
	private Class<IPlugin> pluginClass = null;
	
	public void setPluginName(String name)
	{
		this.name = name;
	}
	
	public String getPluginName()
	{
		return this.name;
	}
	
	public void setDeclaredClassName(String className)
	{
		this.declaredClass = className;
	}
	
	public String getDeclaredClassName()
	{
		return this.declaredClass;
	}
	
	public void setClassLoader(ClassLoader cl)
	{
		this.classLoader = cl;
	}
	
	public ClassLoader getClassLoader()
	{
		return this.classLoader;
	}
	
	public void setJarFileURL(URL jarFile)
	{
		this.jarFile = jarFile;
	}
	
	public void setJarFileURL(String jarFile)
	{
		try
		{
			this.jarFile = new URL(jarFile);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setClassObj(Class<IPlugin> plugin)
	{
		this.pluginClass = plugin;
	}
	
	public Class<IPlugin> getClassObj()
	{
		return this.pluginClass;
	}
	
	public URL getJarFileURL()
	{
		return this.jarFile;
	}
	
	public String getJarFileName()
	{
		String fileName = this.jarFile.toString();
		if (fileName.startsWith("file:"))
				fileName = fileName.substring("file:".length());
		return fileName;
	}
}