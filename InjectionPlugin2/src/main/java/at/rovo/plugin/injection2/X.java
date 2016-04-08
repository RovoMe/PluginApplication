package at.rovo.plugin.injection2;

import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.ComponentId;
import at.rovo.common.annotations.Inject;
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
		if (d != null)
		{
			System.out.print("X --> "); d.output();
		}
		else
			System.out.println("Injection of class D failed due to missing dependency class. Make sure to provide this class first!");
		System.out.print("X --> "); y.output();
		System.out.println("X: "+id);
	}
}
