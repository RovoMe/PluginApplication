package at.rovo.annotations;

/**
 * <p>Defines the possible {@link Component} type. A 
 * <em>SINGLETON</em> annotated component is only created
 * once and shared across all plug-ins. A <em>PROTOTYPE</em>
 * annotated component is created on every injection.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public enum ScopeType
{
    SINGLETON,
    PROTOTYPE
};
