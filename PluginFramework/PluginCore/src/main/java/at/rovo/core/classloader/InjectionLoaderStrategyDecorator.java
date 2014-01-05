package at.rovo.core.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import at.rovo.annotations.Component;
import at.rovo.annotations.ScopeType;
import at.rovo.plugin.InjectionException;

/**
 * <p>
 * <code>InjectionLoaderStrategyDecorator</code> is a decorator for
 * {@link IClassLoaderStrategy} objects. It uses bytes retrieved by the
 * decorated strategy and modifies these with additional lines of code which
 * will be written into the byte array using Javassist therefore. The resulting
 * bytes of the modified class are then sent to the invoking class of the
 * strategy.
 * </p>
 * <p>
 * To be able to instrument the byte array returned by the decorated strategy,
 * the jar file that includes the .class file of the class to manipulate needs
 * to be set via {@link #setJarFile(File)}.
 * </p>
 * <p>
 * This decorator only decorates {@link Component} annotated classes which do
 * NOT match any of the following packages:
 * </p>
 * <code>
 * <ul>
 * <li>java</li>
 * <li>javax</li>
 * <li>sun</li>
 * <li>com.sun</li>
 * <li>org.jdom</li>
 * <li>org.apache</li>
 * </ul>
 * </code>
 * <p>
 * Further it keeps track of already instrumented classes which will get skipped
 * on additional calls.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class InjectionLoaderStrategyDecorator implements IClassLoaderStrategy
{
	/** The logger of this class **/
	private static Logger LOGGER = 
			Logger.getLogger(InjectionLoaderStrategyDecorator.class.getName());
	/** The strategy to decorate **/
	private IClassLoaderStrategy strategy = null;
	/** The jar file to load the class bytes from for class modifications **/
	private File jarFile = null;
	/**
	 * The names of the classes that we've already instrumented so that we don't
	 * implement them twice
	 **/
	private Set<String> instrumentedClasses = new HashSet<>();
	/** A list of class prefixes that should not be instrumented */
	private List<String> classesToSkip = new ArrayList<String>();

	/**
	 * <p>
	 * Creates a new instance of this class and sets the strategy of an
	 * {@link StrategyClassLoader} to decorate.
	 * </p>
	 * 
	 * @param strategy
	 *            The strategy to decorate
	 */
	public InjectionLoaderStrategyDecorator(IClassLoaderStrategy strategy)
	{
		this.strategy = strategy;

		// Build the list of class prefixes to skip
		classesToSkip.add("javax.");
		classesToSkip.add("java.");
		classesToSkip.add("sun.");
		classesToSkip.add("com.sun.");
		classesToSkip.add("org.jdom");
		classesToSkip.add("org.apache.");
	}

	/**
	 * <p>
	 * Sets the jar file which contains the class definition of the
	 * {@link Component}s to modify.
	 * </p>
	 * 
	 * @param jarFile
	 */
	public void setJarFile(File jarFile)
	{
		this.jarFile = jarFile;
	}

	@Override
	public byte[] findClassBytes(String className)
	{
		// prevents injection classes from being hot deployed if they change
		// only instrument a class once
		if (this.instrumentedClasses.contains(className))
		{
			return null;
		}
		this.instrumentedClasses.add(className);

		// skip in the list of class prefixes to skip
		for (String classToSkip : this.classesToSkip)
		{
			if (className.startsWith(classToSkip))
			{
				return null;
			}
		}

		// get class bytes from the class loader strategy we are decorating
		byte[] strategyBytes = this.strategy.findClassBytes(className);
		if (strategyBytes != null)
		{
			// Javassist part starts here
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
				return null;
			}
			cp2 = cp.appendClassPath(new ByteArrayClassPath(className, strategyBytes));

			try
			{
				CtClass cc = cp.get(className);
				if (!cc.isFrozen())
				{
					if (cc.hasAnnotation(Component.class))
					{
						// treat singleton components differently to prototype
						// components as they require an initialization method
						// as the constructor is private
						Object o = cc.getAnnotation(Component.class);
						Component comp = null;
						if (o.toString().contains(Component.class.getName()))
							comp = (Component) o;
						if (comp != null
								&& comp.scope().equals(ScopeType.SINGLETON))
						{
							// the component is a singleton!
							// fetch the instance field we want to inject the
							// call to
							this.findSingletonFieldsAndInjectCode(cc);
						}
						else if (comp != null)
						{
							// the component is a prototype object which means a
							// call of its constructor is safe
							CtConstructor constructor;
							if (cc.getConstructors().length == 0)
							{
								constructor = CtNewConstructor.defaultConstructor(cc);
								cc.addConstructor(constructor);
							}
							else
								constructor = cc.getDeclaredConstructor(null);

							// Only instrument the default constructor in this
							// class, not in the super class because the super 
							// class will be instrumented separately
							if (constructor.getLongName().startsWith(className))
							{
								String code = "at.rovo.core.injection.IInjectionController ic = at.rovo.core.injection.InjectionControllerImpl.INSTANCE; "
										+ "ic.initialize(this);";

								constructor.insertAfter(code);
							}
						}
					}
				}

				strategyBytes = cc.toBytecode();
			}
			catch (NotFoundException | IOException | CannotCompileException
					| ClassNotFoundException e)
			{
				throw new InjectionException(e.getLocalizedMessage());
			}

			// free the locked resource files
			cp.removeClassPath(cp1);
			cp.removeClassPath(cp2);
		}
		return strategyBytes;
	}
	
	/**
	 * <p>
	 * Iterates through defined fields of the provided class and injects 
	 * necessary code for singleton classes.
	 * </p>
	 * <p>
	 * The method looks for a field name that matches one of the following 
	 * rules:
	 * </p>
	 * <ul>
	 * <li>Field name equals <code>instance</code></li>
	 * <li>Field name equals <code>reference</code></li>
	 * <li>Field name starts with <code>ini</code></li>
	 * <li>Field name starts with <code>ref</code></li>
	 * </ul>
	 * <p>
	 * Note that field names are checked in case insensitive manner.
	 * </p>
	 * <p>
	 * If multiple fields match the given name an {@link InjectionException}
	 * will be thrown indicating the ambiguity found. If no matching field could
	 * be found one will be generated automatically. Note further that a method
	 * with name <code>getInstance()</code> will be looked which will be 
	 * replaced by an own implementation.
	 * </p>
	 * 
	 * @param cc The class to inject code into
	 * 
	 * @throws NotFoundException 
	 * @throws CannotCompileException 
	 */
	private final void findSingletonFieldsAndInjectCode(CtClass cc) 
			throws CannotCompileException, NotFoundException
	{
		int count = 0;
		for (CtField field : cc.getDeclaredFields())
		{
			String fieldName = field.getName().toLowerCase();
			if (fieldName.equals("instance") || fieldName.startsWith("ini")
					|| fieldName.equals("reference") || fieldName.startsWith("ref"))
			{
				LOGGER.log(Level.FINE, "found singleton field to inject: {0}",
						new Object[] { fieldName });
				count++;
				if (count > 1)
					throw new InjectionException(
							"Multiple fields found that could be appropriate for injection!");
				
				this.injectIntoSingletonField(cc, field);
			}
		}
	}
	
	/**
	 * <p>
	 * This method removes the field holding the singleton instance and a the 
	 * corresponding getInstance() method and replaces it with its own version.
	 * </p>
	 * <p>
	 * The code injected will make use of the <em>WeakSingleton</em> pattern
	 * which unloads the singleton if no strong reference is pointing to the
	 * singleton. This might lead to flaws as if no plugin-global reference is
	 * kept to the singleton it might get eligible for garbage collection.
	 * </p>
	 * 
	 * @param cc The class to inject code into
	 * @param instance The field which declares the static singleton instance
	 * 
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	private final void injectIntoSingletonField(CtClass cc, CtField instance) 
			throws CannotCompileException, NotFoundException
	{
		// removing the old instance field
		if (instance != null)
		{
			LOGGER.log(Level.FINE, "removing field {0} from {1}", 
					new Object[] { instance.getName(), cc.getName() });
			cc.removeField(instance);
		}
		
		// adding the controller as a private field to
		// the class
		CtField controller = CtField
				.make("private static at.rovo.core.injection.IInjectionController ic "
						+ "= at.rovo.core.injection.InjectionControllerImpl.INSTANCE;",
						cc);
		cc.addField(controller);
		LOGGER.log(Level.FINE, "added field to {0}", 
				new Object[] { cc.getName() } );
		
		// adding the new instance field		
		String code = "private static java.lang.ref.WeakReference REFERENCE;";
		LOGGER.log(Level.FINE, "adding field {0} to {1}", 
				new Object[] { code, cc.getName() });
		CtField newInstance = CtField.make(code, cc);
		cc.addField(newInstance);
		
		// remove any existing getInstance() method of singletons
		
		CtMethod getInstance = cc.getDeclaredMethod("getInstance", new CtClass[] {});
		if (getInstance != null)
		{
			LOGGER.log(Level.FINE, "Removing getInstance() method of {0}", 
					new Object[] { cc.getName() });
			cc.removeMethod(getInstance);
		}
		
		// add a new version of the singletons getInstance() method to the class
		
		StringBuilder sb = new StringBuilder();
		sb.append("public static "+cc.getName()+" getInstance() {\n");
		sb.append("if (REFERENCE == null) {\n");
		sb.append("synchronized("+cc.getName()+".class) {\n");
		sb.append("if (REFERENCE == null) {\n");
		sb.append("final "+cc.getName()+" instance = ("+cc.getName()+")ic.initialize(new "+cc.getName()+"());\n");
		sb.append("REFERENCE = new java.lang.ref.WeakReference(instance);\n");
		sb.append("}\n}\n}\n");
		sb.append(cc.getName()+" instance = ("+cc.getName()+")REFERENCE.get();\n");
		sb.append("if (instance != null)\n");
		sb.append("return instance;\n");
		sb.append("synchronized("+cc.getName()+".class) {\n");
		sb.append("instance = ("+cc.getName()+")ic.initialize(new "+cc.getName()+"());\n");
		sb.append("REFERENCE = new java.lang.ref.WeakReference(instance);\n");
		sb.append("return instance;\n");
		sb.append("}\n}");
		
		LOGGER.log(Level.FINE, "Adding modified version of getInstance() to {0} - content is:\n{1}",
				new Object[] { cc.getName(), sb.toString() } );
		
		getInstance = CtMethod.make(sb.toString(), cc);
		cc.addMethod(getInstance);
	}

	@Override
	public final URL findResourceURL(String resourceName)
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

	/**
	 * <p>
	 * Checks if a specified fully-qualified class name is a {@link Component}
	 * with {@link ScopeType#SINGLETON}.
	 * </p>
	 * 
	 * @param name
	 *            Fully qualified name of the class to check for being a
	 *            {@link ScopeType#SINGLETON} {@link Component}
	 * @return true if the <code>name</code> could be inferred to a
	 *         {@link ScopeType#SINGLETON} {@link Component}
	 */
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
				Component comp = (Component) cc.getAnnotation(Component.class);
				if (comp.scope().equals(ScopeType.SINGLETON))
					return true;
			}
		}
		catch (NotFoundException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
