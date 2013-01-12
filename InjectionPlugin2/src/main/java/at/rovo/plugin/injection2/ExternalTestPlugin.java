package at.rovo.plugin.injection2;

import at.rovo.plugin.IPlugin;
import at.rovo.plugin.PluginException;

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
			System.out.print("Creating new prototype B (b1) ");
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
