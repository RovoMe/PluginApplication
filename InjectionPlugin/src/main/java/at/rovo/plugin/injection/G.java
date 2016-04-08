package at.rovo.plugin.injection;

import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.ComponentId;

@Component
public class G
{
	@ComponentId
	private Long id;
	
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
    	System.out.println("G: "+id);
    }
}
