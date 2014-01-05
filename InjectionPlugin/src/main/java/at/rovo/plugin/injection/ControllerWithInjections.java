/* Injection-Setup
 *   ( ==> = inheritance, --> injection)
 * 
 *        D <---+
 *  G     ^     |
 *  ^     |     E <---+    
 *  |     |     ^     |
 *  |     |     |     |
 *  F ==> A ==> B --> C
 *   
 */

package at.rovo.plugin.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.Inject;
import at.rovo.annotations.ScopeType;
import at.rovo.plugin.IPlugin;
import at.rovo.plugin.InjectionException;

@Component(scope=ScopeType.PROTOTYPE)
public class ControllerWithInjections implements IPlugin
{
    @ComponentId
    private Long id;
    
    @Inject
    private D d;
    
    public void setId(Long id)
    {
    	this.id = id;
    }
    
    public Long getId()
    {
    	return this.id;
    }
    
    public void setD(D d)
    {
        this.d = d;
    }
    
    public D getD()
    {
        return this.d;
    }

	@Override
	public void execute() 
	{
		System.out.println();
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		System.out.println("                                    Starting execution: ");
		System.out.println("   ");
		System.out.println("D and E are singleton-components, all others are prototype-components");
		System.out.println("   ");
		System.out.println("   -->  ... injections");
		System.out.println("   ==>  ... inherited injections");
		System.out.println("   ==   ... inheritance");
		System.out.println("   ");
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		A a = null;
		A a2 = null;
		B b1 = null;
		B b2 = null;
		System.out.println("   ");
        System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        try
        {
        	System.out.print("Creating new singleton E ");
	        E e = E.getInstance();
	        System.out.println("with classloader "+e.getClass().getClassLoader()+": ");
	        e.output();
        }
        catch(InjectionException Ex)
        {
        	System.err.println("ERROR: "+Ex.getLocalizedMessage());
        }
		catch(NullPointerException npEx)
		{
			npEx.printStackTrace();
		}
        System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
		System.out.println("   ");
		System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		try
		{
			System.out.print("Creating new prototype B (b1) ");
			b1 = new B();
			System.out.println("with classloader "+b1.getClass().getClassLoader()+": ");
	        b1.output();
		}
		catch(InjectionException Ex)
		{
			System.err.println("ERROR: "+Ex.getLocalizedMessage());
		}
		catch(NullPointerException npEx)
		{
			npEx.printStackTrace();
		}
		System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
        System.out.println("   ");
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        try
        {
        	System.out.print("Creating new prototype A (a) ");
	        a = new A();
	        System.out.println("with classloader "+a.getClass().getClassLoader()+": ");
	        a.output();
        }
        catch(InjectionException Ex)
        {
        	System.err.println("ERROR: "+Ex.getLocalizedMessage());
        }
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
		System.out.println("   ");
		System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		try
		{
			System.out.print("Creating new prototype B (b2) ");
			b2 = new B();
			System.out.println("with classloader "+b2.getClass().getClassLoader()+": ");
	        b2.output();
		}
		catch(InjectionException Ex)
		{
			System.err.println("ERROR: "+Ex.getLocalizedMessage());
		}
		catch(NullPointerException npEx)
		{
			npEx.printStackTrace();
		}
		System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
        System.out.println("   ");
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        try
        {
        	System.out.println("Calling prototype A (a) again with a's classloader "+a.getClass().getClassLoader()+": ");
	        a.output();
        }
        catch(InjectionException Ex)
        {
        	System.err.println("ERROR: "+Ex.getLocalizedMessage());
        }
		catch(NullPointerException npEx)
		{
			npEx.printStackTrace();
		}
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
        System.out.println("   ");
        System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        try
        {
        	System.out.println("Calling prototype B (b1) again with b1's classloader "+b1.getClass().getClassLoader()+": ");
	        b1.output();
        }
        catch(InjectionException Ex)
        {
        	System.err.println("ERROR: "+Ex.getLocalizedMessage());
        }
		catch(NullPointerException npEx)
		{
			npEx.printStackTrace();
		}
        System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
        System.out.println("   ");
        System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        try
        {
        	System.out.print("Calling singleton E again ");
	        E e = E.getInstance();
	        System.out.println("with classloader "+e.getClass().getClassLoader()+": ");
	        e.output();
        }
        catch(InjectionException Ex)
        {
        	System.err.println("ERROR: "+Ex.getLocalizedMessage());
        }
		catch(NullPointerException npEx)
		{
			npEx.printStackTrace();
		}
        System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
        System.out.println("   ");
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        try
        {
        	System.out.print("Creating new prototype A (a2): ");
        	a2 = new A();
        	System.out.println("with classloader "+a2.getClass().getClassLoader()+": ");
	        a2.output();
        	System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        }
        catch(InjectionException Ex)
        {
        	System.err.println("ERROR: "+Ex.getLocalizedMessage());
        	System.out.println("\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        }
		catch(NullPointerException npEx)
		{
			npEx.printStackTrace();
		}
        try 
        {
			Thread.sleep(2*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
	}
}
