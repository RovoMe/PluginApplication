package at.rovo.plugin.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.Inject;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.PROTOTYPE)
public class B extends A
{
    @ComponentId
    private Long id;
    
    @Inject
    private C c;
    
    @Inject
    private E e;
    
    @Override
    public Long getId()
    {
        return this.id;
    }
    
    @Override
    public void setId(Long id)
    {
    	super.setId(id);
        this.id = id;
    } 
    
    public C getC()
    {
        return this.c;
    }
    
    public void setC(C c)
    {
        this.c = c;
    }
    
    public E getE()
    {
        return e;
    }
    
    public void setE(E e)
    {
        this.e = e;
    }
    
    @Override
    public void output()
    {
    	System.out.print("B == "); super.output();
    	System.out.print("B ==> "); getG().output();
    	System.out.print("B --> "); c.output();
        System.out.print("B ==> "); getD().output();
        System.out.print("B --> "); getE().output();
        System.out.println("B: "+id);
    }
}
