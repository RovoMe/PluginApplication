package at.rovo.plugin.injection2;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.Inject;
import at.rovo.plugin.injection.D;

@Component
public class X
{
	@ComponentId
	private Long id;
	
	@Inject
	private Y y;
	@Inject
	private D d = null;
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Long getId()
	{
		return this.id;
	}
	
	public Y getY()
	{
		return y;
	}
	
	public void setY(Y y)
	{
		this.y = y;
	}
	
	public void output()
	{
		System.out.print("X --> "); d.output();
		System.out.print("X --> "); y.output();
		System.out.println("X: "+id);
	}
}
