package at.rovo.plugin.injection;

import java.lang.ref.WeakReference;
import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.SINGLETON)
public class D
{
	private static WeakReference<D> REFERENCE = null;
	
    @ComponentId
    private Long id;
    
    private D()
    {
    	
    }
    
    public static D getInstance()
    {
    	if (REFERENCE == null)
    	{
    		synchronized (D.class)
    		{
    			if (REFERENCE == null)
    			{
    				D instance = new D();
    		    	REFERENCE = new WeakReference<>(instance);
    		    	return instance;
    			}
    		}
    	}
    	D instance = REFERENCE.get();
    	if (instance != null)
    	{
    		return instance;
    	}
    	synchronized (D.class)
    	{
    		instance = new D();
        	REFERENCE = new WeakReference<>(instance);
        	return instance;
    	}
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
