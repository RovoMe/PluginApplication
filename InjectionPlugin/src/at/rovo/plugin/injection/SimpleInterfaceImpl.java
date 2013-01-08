package at.rovo.plugin.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.ComponentId;
import at.rovo.annotations.ScopeType;

@Component(scope=ScopeType.PROTOTYPE)
public class SimpleInterfaceImpl implements SimpleInterface
{
    @ComponentId
    private Long id;
    
    @Override
    public void fooBar()
    {
        System.out.println("[SimpleInterfaceImpl] id: "+id+" fooBar called!");
    }
}
