package at.rovo.plugin.injection2;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.Inject;

@Component
public class Z
{
	@ComponentId
	private Long id;
	@Inject
	private Singleton singleton;
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Long getId()
	{
		return this.id;
	}
	
	public void setSingleton(Singleton singleton)
	{
		this.singleton = singleton;
	}
	
	public Singleton getSingleton()
	{
		return this.singleton;
	}
	
	public void output()
	{
		System.out.print("Z --> "); this.singleton.output();
		System.out.println("Z: "+id);
	}
}
