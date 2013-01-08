package at.rovo.core;

public interface IPluginListener 
{
	public void pluginLoaded(String pluginName);
	public void pluginRemoved(String pluginName);
	public void exception(Exception e);
}
