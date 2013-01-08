package at.rovo.plugin;

public class YetAnotherPluginTest implements IPlugin
{
	@Override
	public String toString()
	{
		Test t = new Test();
		return "Yet another plugin Test: "+t.toString();
	}

	@Override
	public void execute() 
	{
		System.out.println(this);
	}
}
