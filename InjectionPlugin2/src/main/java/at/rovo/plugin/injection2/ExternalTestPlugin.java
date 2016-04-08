package at.rovo.plugin.injection2;

import at.rovo.common.plugin.IPlugin;
import at.rovo.common.plugin.PluginException;

public class ExternalTestPlugin implements IPlugin
{
	@Override
	public void execute()
	{		
		System.out.println("");
		System.out.println("Hello from an external test plug-in!");
		System.out.println();
		System.out.println("This plug-in requires class at.rovo.plugin.injection.D to be loaded!");
		System.out.println();
		
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		try
		{
			System.out.print("Creating new prototype X (x) ");
			X x = new X();
			System.out.println("with classloader "+x.getClass().getClassLoader()+": ");
			x.output();
		}
		catch (Exception ex)
		{
			throw new PluginException(ex.getLocalizedMessage());
		}
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

	}
}
