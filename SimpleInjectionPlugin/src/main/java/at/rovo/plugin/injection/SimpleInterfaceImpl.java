package at.rovo.plugin.injection;

import java.util.logging.Level;
import java.util.logging.Logger;
import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.PROTOTYPE)
public class SimpleInterfaceImpl implements SimpleInterface
{
	/** The logger of this class **/
	private static Logger logger = Logger.getLogger(SimpleInterfaceImpl.class.getName());
	
    @ComponentId
    private Long id;
    
    @Override
    public void fooBar()
    {
        logger.log(Level.INFO, "id: {0} fooBar called! Was loaded with {1}", 
        		new Object[] {id,SimpleInterfaceImpl.class.getClassLoader()});
        System.out.println("Yeah! Injection worked! :)");
    }
}
