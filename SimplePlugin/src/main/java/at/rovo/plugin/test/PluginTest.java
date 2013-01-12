package at.rovo.plugin.test;

import at.rovo.plugin.IPlugin;

public class PluginTest implements IPlugin
{
	public String toString()
	{
		return "SimplePluginTest";
	}

	@Override
	public void execute() 
	{
		System.out.println(this);
//		System.out.println();
		System.out.println("Yeah it worked!");
	}
}
