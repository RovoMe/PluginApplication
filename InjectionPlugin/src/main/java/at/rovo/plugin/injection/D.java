package at.rovo.plugin.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.SINGLETON)
public class D
{
	private static D instance;
    @ComponentId
    private Long id;
    
    private D()
    {
    	
    }
    
    public static D getInstance()
    {
    	if (instance == null)
    		instance = new D();
    	return instance;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getId()
    {
        return this.id;
    }
    
    public void output()
    {
        System.out.println("D: "+id);
    }
}
