package at.rovo.plugin.injection;

import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.ComponentId;
import at.rovo.common.annotations.Inject;

@Component
public class C
{
    @ComponentId
    private Long id;
    
    @Inject
    private E e;
    
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
    	System.out.print("C --> "); e.output();
        System.out.println("C: "+id);
    }
    
    public void setE(E e)
    {
        this.e = e;
    }
    
    public E getE()
    {
        return this.e;
    }
}
