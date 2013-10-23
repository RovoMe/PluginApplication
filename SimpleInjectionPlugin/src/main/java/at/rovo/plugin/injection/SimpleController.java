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

import java.util.logging.Level;
import java.util.logging.Logger;
import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.Inject;
import at.rovo.annotations.ScopeType;
import at.rovo.plugin.IPlugin;

@Component(scope=ScopeType.PROTOTYPE)
public class SimpleController implements IPlugin
{
	/** The logger of this class **/
	private static Logger logger = Logger.getLogger(SimpleController.class.getName());
	
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
		logger.log(Level.INFO, "{0} has ID: {1}; it was loaded with class loader: {2}", 
				new Object[] {this.getClass().getCanonicalName(), this.id, this.getClass().getClassLoader()});
        this.callSi();
	}
}
