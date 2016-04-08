package at.rovo.plugin.injection;

import java.lang.ref.WeakReference;
import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.ComponentId;
import at.rovo.common.annotations.Inject;
import at.rovo.common.annotations.ScopeType;

@Component(scope=ScopeType.SINGLETON)
public class E
{
    private static WeakReference<E> INSTANCE;
    
	@ComponentId
    private Long id;
    @Inject
    private D d;
    
    private E()
    {
    	
    }
    
    public static E getInstance()
    {
    	if (INSTANCE == null)
    	{
    		synchronized (E.class)
    		{
    			if (INSTANCE == null)
    			{
		    		final E instance = new E();
		    		INSTANCE = new WeakReference<>(instance);
		    		return instance;
    			}
    		}
    	}
    	E instance = INSTANCE.get();
    	if (instance != null)
    		return instance;
    	
    	synchronized (E.class)
    	{
		   	instance = new E();
		   	INSTANCE = new WeakReference<>(instance);
		   	return instance;
    	}
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
