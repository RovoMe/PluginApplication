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

@Component(scope=ScopeType.PROTOTYPE)
public class SimpleController implements IPlugin
{
    @ComponentId
    private Long id;
    
    @Inject(specificType = SimpleInterfaceImpl.class)
    private SimpleInterface si;
    
    public void setId(Long id)
    {
    	this.id = id;
    }
    
    public Long getId()
    {
    	return this.id;
    }
    
    public void callSi()
    {
        this.si.fooBar();
    }

	@Override
	public void execute() 
	{
		System.out.println(this.getClass().getCanonicalName()+" has ID: "+this.id+"; it was loaded with class loader: "+this.getClass().getClassLoader());
        this.callSi();
	}
}
