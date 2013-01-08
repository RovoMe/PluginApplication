package at.rovo.core.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import at.rovo.annotations.Component;
import at.rovo.annotations.ScopeType;

public class InjectionLoaderStrategy implements IClassLoaderStrategy
{
	private IClassLoaderStrategy strategy = null;
	private File jarFile = null;
	/**
     * The names of the classes that we've already instrumented so that we don't
     * implement them twice
     */
	private Set<String> instrumentedClasses = new HashSet<String>();
	/**
     * A list of class prefixes that should not be instrumented, e.g. java.lang
     */
    private List<String> classesToSkip = new ArrayList<String>();
    
	public InjectionLoaderStrategy(IClassLoaderStrategy strategy)
	{
		this.strategy = strategy;
		
		 // Build the list of class prefixes to skip
        classesToSkip.add( "com.geekcap.openapm" );
        classesToSkip.add( "javax." );
        classesToSkip.add( "java." );
        classesToSkip.add( "sun." );
        classesToSkip.add( "com.sun." );
        classesToSkip.add( "org.jdom" );
        classesToSkip.add( "org.apache." );
	}
	
	public void setJarFile(File jarFile)
	{
		this.jarFile = jarFile;
	}
	
	@Override
	public byte[] findClassBytes(String className)
	{
        // Only instrument a class once
        if( this.instrumentedClasses.contains( className ) )
        {
            return null;
        }
        this.instrumentedClasses.add( className );
        
        // Skip in the list of class prefixes to skip
        for( String classToSkip : this.classesToSkip )
        {
            if( className.startsWith( classToSkip ) )
            {
                return null;
            }
        }
		
		byte[] strategyBytes = this.strategy.findClassBytes(className);
		if (strategyBytes != null)
		{
			ClassPool cp = ClassPool.getDefault();
			cp.insertClassPath(new ClassClassPath(this.getClass()));
			ClassPath cp1 = null;
			ClassPath cp2 = null;
			try
			{
				cp1 = cp.insertClassPath(this.jarFile.getAbsolutePath());
			}
			catch (NotFoundException e1)
			{
				e1.printStackTrace();
			}
			cp2 = cp.appendClassPath(new ByteArrayClassPath(className, strategyBytes));
			
			try
			{
				CtClass cc = cp.get(className);
				if (!cc.isFrozen())
				{
					if (cc.hasAnnotation(Component.class))
					{				
						Object o = cc.getAnnotation(Component.class);
						Component comp = null;
						if (o.toString().contains(Component.class.getName()))
							comp = (Component)o;
						if (comp != null && comp.scope().equals(ScopeType.SINGLETON))
						{
							CtField instance = cc.getDeclaredField("instance");
							if (instance != null)
							{
								// removing the old instance field
								cc.removeField(instance);
								// adding the controller as a private field to the class
								CtField controller = CtField.make("private static at.rovo.core.injection.IInjectionController ic = at.rovo.core.injection.InjectionControllerImpl.getInstance();", cc);
								cc.addField(controller);
								// adding the new instance field
								String code = "private static "+cc.getName()+" instance = ("+cc.getName()+")ic.initialize(new "+cc.getName()+"());";
								CtField newInstance = CtField.make(code, cc);
								cc.addField(newInstance);
							}
						}
						else if (comp != null)
						{
							CtConstructor constructor;
							if (cc.getConstructors().length == 0)
							{
								constructor = CtNewConstructor.defaultConstructor(cc);
								cc.addConstructor(constructor);
							}
							else
								constructor = cc.getDeclaredConstructor(null);
											
							// Only instrument the default constructor in this class, 
							// not in the super class because the super class will be 
							// instrumented separately
							if (constructor.getLongName().startsWith(className))
							{							
								String code = 			
								"at.rovo.core.injection.IInjectionController ic = at.rovo.core.injection.InjectionControllerImpl.getInstance(); "+
								"ic.initialize(this);";
								
								constructor.insertAfter(code);
							}
						}
			        }
				}
							
				strategyBytes =  cc.toBytecode();			
			}
			catch (NotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (CannotCompileException e)
			{
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			
			// free the locked resource files
			cp.removeClassPath(cp1);
			cp.removeClassPath(cp2);
		}
		return strategyBytes;
	}

	@Override
	public URL findResourceURL(String resourceName)
	{

		return strategy.findResourceURL(resourceName);
	}

	@Override
	public Enumeration<URL> findResourcesEnum(String resourceName)
	{

		return strategy.findResourcesEnum(resourceName);
	}

	@Override
	public String findLibraryPath(String libraryName)
	{

		return strategy.findLibraryPath(libraryName);
	}
	
	public boolean isSingleton(String name)
	{
		byte[] strategyBytes = this.strategy.findClassBytes(name);
		ClassPool cp = ClassPool.getDefault();
		cp.insertClassPath(new ByteArrayClassPath(name, strategyBytes));
		
		try
		{
			CtClass cc = cp.get(name);
			if (cc.hasAnnotation(Component.class))
			{
				Component comp = (Component)cc.getAnnotation(Component.class);
				if (comp.scope().equals(ScopeType.SINGLETON))
					return true;
			}
		}
		catch (NotFoundException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
