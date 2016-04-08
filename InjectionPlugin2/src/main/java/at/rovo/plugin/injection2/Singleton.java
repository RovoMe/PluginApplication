package at.rovo.plugin.injection2;

import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.ComponentId;
import at.rovo.common.annotations.ScopeType;

@Component(scope=ScopeType.SINGLETON)
public class Singleton
{
	private static Singleton instance;
	@ComponentId
	private Long id;
	
	private Singleton()
	{
		
	}
	
	public static Singleton getInstance()
	{
		if (instance == null)
			instance = new Singleton();
		return instance;
	}
	
	public Long getId()
	{
		return this.id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public void output()
	{
		System.out.println("Singleton: "+id);
	}
}
