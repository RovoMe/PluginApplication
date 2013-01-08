package at.rovo.plugin.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.SINGLETON)
public class X
{
	private static X instance;
    @ComponentId
    private Long id;
    
    private X()
    {
    	
    }
    
    public static X getInstance()
    {
    	if (instance == null)
    		instance = new X();
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
        System.out.println("X: "+id);
    }
}