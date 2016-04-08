package at.rovo.core.classloader;

import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.ScopeType;
import at.rovo.core.injection.Instrumented;
import at.rovo.common.plugin.InjectionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * <code>InjectionLoaderStrategyDecorator</code> is a decorator for {@link IClassLoaderStrategy} objects. It uses bytes
 * retrieved by the decorated strategy and modifies these with additional lines of code which will be written into the
 * byte array using Javassist therefore. The resulting bytes of the modified class are then sent to the invoking class
 * of the strategy.
 * <p/>
 * To be able to instrument the byte array returned by the decorated strategy, the jar file that includes the .class
 * file of the class to manipulate needs to be set via {@link #setJarFile(File)}.
 * <p/>
 * This decorator only decorates {@link Component} annotated classes which do NOT match any of the following packages:
 * <code> <ul> <li>java</li> <li>javax</li> <li>sun</li> <li>com.sun</li> <li>org.jdom</li> <li>org.apache</li> </ul>
 * </code>
 * <p/>
 * Further it keeps track of already instrumented classes which will get skipped on additional calls.
 *
 * @author Roman Vottner
 */
public class InjectionLoaderStrategyDecorator implements IClassLoaderStrategy
{
    /** The logger of this class **/
    private final static Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    /** The strategy to decorate **/
    private IClassLoaderStrategy strategy = null;
    /** The jar file to load the class bytes from for class modifications **/
    private File jarFile = null;
    /** A list of class prefixes that should not be instrumented */
    private final List<String> classesToSkip = new ArrayList<>();

    /**
     * Creates a new instance of this class and sets the strategy of an {@link StrategyClassLoader} to decorate.
     *
     * @param strategy
     *         The strategy to decorate
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
     * Sets the jar file which contains the class definition of the {@link Component}s to modify.
     *
     * @param jarFile
     *         The JAR file to use for this decorator
     */
    public void setJarFile(File jarFile)
    {
        this.jarFile = jarFile;
    }

    @Override
    public byte[] findClassBytes(String className) throws IOException
    {
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
            ClassPath cp1;
            ClassPath cp2;
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
                // skip instrumentation if the class is frozen and therefore can't be modified
                if (!cc.isFrozen())
                {
                    // skip the injection if either the class is not a component or already got instrumented
                    if (cc.hasAnnotation(Component.class) && !cc.hasAnnotation(Instrumented.class))
                    {
                        // add an annotation to the class bytes so we know that we already instrumented that class
                        this.addAnnotationToClass(cc, Instrumented.class, cp);

                        LOGGER.log(Level.FINE, "Class {0} has annotation {1}: {2}",
                                   new Object[] {cc.getName(), Instrumented.class.getName(),
                                                 cc.hasAnnotation(Instrumented.class)});
                        LOGGER.log(Level.FINE, "Class {0} has annotation {1}: {2}",
                                   new Object[] {cc.getName(), Component.class.getName(),
                                                 cc.hasAnnotation(Component.class)});

                        // treat singleton components differently to prototype components as they require an
                        // initialization method as the constructor is private
                        Object o = cc.getAnnotation(Component.class);
                        Component comp = null;
                        if (o.toString().contains(Component.class.getName()))
                        {
                            comp = (Component) o;
                        }
                        if (comp != null && comp.scope().equals(ScopeType.SINGLETON))
                        {
                            // the component is a singleton!
                            // fetch the instance field we want to inject the call to
                            this.findSingletonFieldsAndInjectCode(cc);
                        }
                        else if (comp != null)
                        {
                            // the component is a prototype object which means a call of its constructor is safe
                            CtConstructor constructor;
                            if (cc.getConstructors().length == 0)
                            {
                                constructor = CtNewConstructor.defaultConstructor(cc);
                                cc.addConstructor(constructor);
                            }
                            else
                            {
                                constructor = cc.getDeclaredConstructor(null);
                            }

                            // Only instrument the default constructor in this
                            // class, not in the super class because the super
                            // class will be instrumented separately
                            if (constructor.getLongName().startsWith(className))
                            {
                                String code  = "at.rovo.core.injection.IInjectionController ic = " +
                                                   "at.rovo.core.injection.InjectionControllerImpl.INSTANCE; " +
                                               "ic.initialize(this);";

                                constructor.insertAfter(code);
                            }
                        }
                    }
                }

                strategyBytes = cc.toBytecode();
            }
            catch (NotFoundException | IOException | CannotCompileException | ClassNotFoundException e)
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
     * Adds a annotation at class level to the provided class.
     *
     * @param cc
     *         The class to inject the annotation into
     * @param annotation
     *         The annotation to inject
     *
     * @throws NotFoundException
     */
    private void addAnnotationToClass(CtClass cc, Class<?> annotation, ClassPool cp) throws NotFoundException
    {
        if (!annotation.isAnnotation())
        {
            LOGGER.log(Level.WARNING, "Failed to add class {0} as annotation to {1} as it is not an annotation",
                       new Object[] {annotation.getName(), cc.getName()});
            return;
        }

        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();

        // check if there are already annotations available
        AnnotationsAttribute attr = (AnnotationsAttribute) ccFile.getAttribute(AnnotationsAttribute.visibleTag);
        if (attr == null)
        {
            // no annotations found so create one
            LOGGER.log(Level.WARNING,
                       "fetching annotation attributes from class file of {0} failed. Creating new one instead",
                       new Object[] {cc.getName()});
            attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        }
        Annotation annot = new Annotation(constPool, cp.get(annotation.getName()));
        attr.addAnnotation(annot);
        ccFile.addAttribute(attr);

        LOGGER.log(Level.FINE, "Added {0} as annotation at class level to {1}",
                   new Object[] {annotation.getName(), cc.getName()});
    }

    /**
     * Iterates through defined fields of the provided class and injects necessary code for singleton classes.
     * <p/>
     * The method looks for a field that is static and has either the same type as the class it is defined in or is of
     * type {@link WeakReference}.
     * <p/>
     * If multiple fields match the given name an {@link InjectionException} will be thrown indicating the ambiguity
     * found. If no matching field could be found one will be generated automatically. Note further that a method with
     * name <code>getInstance()</code> will be looked which will be replaced by an own implementation.
     *
     * @param cc
     *         The class to inject code into
     *
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void findSingletonFieldsAndInjectCode(CtClass cc) throws CannotCompileException, NotFoundException
    {
        int count = 0;
        for (CtField field : cc.getDeclaredFields())
        {
            // the field holding the singleton reference is obviously a static
            // field.
            if (Modifier.isStatic(field.getModifiers()))
            {
                CtClass retType = field.getType();
                // the type of the field has to either match the class name
                // or WeakReference
                if (retType.getName().equals(cc.getName()) || retType.getName().equals("java.lang.ref.WeakReference"))
                {
                    LOGGER.log(Level.INFO, "found singleton field to inject: {0}", new Object[] {field.getName()});

                    count++;
                    if (count > 1)
                    {
                        throw new InjectionException("Multiple fields found that could be appropriate for injection!");
                    }

                    this.injectIntoSingletonField(cc, field);
                }
            }
        }

        // no static field available so call the injection method without a
        // field. It uses the field just to delete it and replace it with its
        // own version
        if (count == 0)
        {
            LOGGER.log(Level.WARNING,
                       "No static field found in {0} that could hold the singleton. Adding an appropriate field instead",
                       new Object[] {cc.getName()});
            this.injectIntoSingletonField(cc, null);
        }
    }

    /**
     * This method removes the field holding the singleton instance and a the corresponding getInstance() method and
     * replaces it with its own version.
     * <p/>
     * The code injected will make use of the <em>WeakSingleton</em> pattern which unloads the singleton if no strong
     * reference is pointing to the singleton. This might lead to flaws as if no plugin-global reference is kept to the
     * singleton it might get eligible for garbage collection.
     *
     * @param cc
     *         The class to inject code into
     * @param instance
     *         The field which declares the static singleton instance
     *
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private void injectIntoSingletonField(CtClass cc, CtField instance) throws CannotCompileException, NotFoundException
    {
        // removing the old instance field
        if (instance != null)
        {
            LOGGER.log(Level.FINE, "removing field {0} from {1}", new Object[] {instance.getName(), cc.getName()});
            cc.removeField(instance);
        }

        // adding the controller as a private field to
        // the class
        CtField controller = CtField.make("private static at.rovo.core.injection.IInjectionController ic " +
                                          "= at.rovo.core.injection.InjectionControllerImpl.INSTANCE;", cc);
        cc.addField(controller);
        LOGGER.log(Level.FINE, "added field to {0}", new Object[] {cc.getName()});

        // adding the new instance field
        String code = "private static java.lang.ref.WeakReference REFERENCE;";
        LOGGER.log(Level.FINE, "adding field {0} to {1}", new Object[] {code, cc.getName()});
        CtField newInstance = CtField.make(code, cc);
        cc.addField(newInstance);

        // remove any existing getInstance() method of singletons

        CtMethod getInstance = cc.getDeclaredMethod("getInstance", new CtClass[] {});
        if (getInstance != null)
        {
            LOGGER.log(Level.FINE, "Removing getInstance() method of {0}", new Object[] {cc.getName()});
            cc.removeMethod(getInstance);
        }

        // add a new version of the singletons getInstance() method to the class
        StringBuilder sb = new StringBuilder();
        sb.append("public static ");
        sb.append(cc.getName());
        sb.append(" getInstance() {\n");
        sb.append("if (REFERENCE == null) {\n");
        sb.append("synchronized(");
        sb.append(cc.getName());
        sb.append(".class) {\n");
        sb.append("if (REFERENCE == null) {\n");
        sb.append("final ");
        sb.append(cc.getName());
        sb.append(" instance = (");
        sb.append(cc.getName());
        sb.append(")ic.initialize(new ");
        sb.append(cc.getName());
        sb.append("());\n");
        sb.append("REFERENCE = new java.lang.ref.WeakReference(instance);\n");
        sb.append("}\n}\n}\n");
        sb.append(cc.getName());
        sb.append(" instance = (");
        sb.append(cc.getName());
        sb.append(")REFERENCE.get();\n");
        sb.append("if (instance != null)\n");
        sb.append("return instance;\n");
        sb.append("synchronized(");
        sb.append(cc.getName());
        sb.append(".class) {\n");
        sb.append("instance = (");
        sb.append(cc.getName());
        sb.append(")ic.initialize(new ");
        sb.append(cc.getName());
        sb.append("());\n");
        sb.append("REFERENCE = new java.lang.ref.WeakReference(instance);\n");
        sb.append("return instance;\n");
        sb.append("}\n}");

        LOGGER.log(Level.FINE, "Adding modified version of getInstance() to {0} - content is:\n{1}",
                   new Object[] {cc.getName(), sb.toString()});

        getInstance = CtMethod.make(sb.toString(), cc);
        cc.addMethod(getInstance);
    }

    @Override
    public final URL findResource(String resourceName) throws IOException
    {
        return strategy.findResource(resourceName);
    }

    @Override
    public Enumeration<URL> findResources(String resourceName) throws IOException
    {
        return strategy.findResources(resourceName);
    }

    @Override
    public InputStream findResourceAsStream(String resourceName) throws IOException
    {
        return strategy.findResourceAsStream(resourceName);
    }

    @Override
    public String findLibraryPath(String libraryName)
    {
        return strategy.findLibraryPath(libraryName);
    }

    /**
     * Checks if a specified fully-qualified class name is a {@link Component} with {@link ScopeType#SINGLETON}.
     *
     * @param name
     *         Fully qualified name of the class to check for being a {@link ScopeType#SINGLETON} {@link Component}
     *
     * @return true if the <code>name</code> could be inferred to a {@link ScopeType#SINGLETON} {@link Component}
     *
     * @throws IOException
     *         If during loading a resource an exception occurred
     */
    public boolean isSingleton(String name) throws IOException
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
                {
                    return true;
                }
            }
        }
        catch (NotFoundException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
