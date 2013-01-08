package at.rovo.plugin.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.SINGLETON)
public class Test
{
	private static at.rovo.core.injection.IInjectionController ic = at.rovo.core.injection.InjectionControllerImpl.getInstance();
	private static Test instance = (Test)ic.initialize(new Test());
    @ComponentId
    private Long id;
    
    private Test()
    {
    	
    }
    
    public static Test getInstance()
    {
    	if (instance == null)
    		instance = new Test();
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
