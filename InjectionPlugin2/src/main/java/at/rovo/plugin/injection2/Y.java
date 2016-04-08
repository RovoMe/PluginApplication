package at.rovo.plugin.injection2;

import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.ComponentId;

@Component
public class Y extends Z
{
	@ComponentId
	private Long id;
	
	public void setId(Long id)
	{
		super.setId(id);
		this.id = id;
	}
	
	public Long getId()
	{
		return this.id;
	}

	public void output()
	{
		System.out.print("Y == ");	super.output();
		System.out.print("Y ==> ");	this.getSingleton().output();
		System.out.println("Y: "+id);
	}
}
