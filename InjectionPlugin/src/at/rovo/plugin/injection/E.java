package at.rovo.plugin.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.Inject;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.SINGLETON)
public class E
{
    private static E instance;
	@ComponentId
    private Long id;
    @Inject
    private D d;
    
    private E()
    {
    	
    }
    
    public static E getInstance()
    {
    	if (instance == null)
    		instance = new E();
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
    
    public void setD(D d)
    {
        this.d = d;
    }
    
    public D getD()
    {
        return this.d;
    }
    
    public void output()
    {
    	System.out.print("E --> "); d.output();
        System.out.println("E: "+id);
    }
}
