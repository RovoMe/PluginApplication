package at.rovo.core;

public class SimplePluginManager extends PluginManager
{
	private static SimplePluginManager instance = null;
	
	private SimplePluginManager()
	{
		super();
	}
	
	public static PluginManager getInstance()
	{
		if (instance == null)
			instance = new SimplePluginManager();
		return instance;
	}
}