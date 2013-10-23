PluginApplication
=================

A basic Java plugin architecture with dependency injection and singleton support. The plugin application avoids 
file-locking

Running the framework
=====================

1.   Import the PluginApplication Maven project into Eclipse (optional)

2.   Install PluginInterface and PluginCore either via `mvn install` directly or use mvn install on the parent 
     project PluginFramework which will install the modules automatically
	 
3.  a. run PluginClient using `mvn test` which execute the Main class and avoid spaming of log-messages.
	   
	   On using Eclipse define a new run configuration for the PluginClient project with goal set to `test`.
	   Afterwards executing the client is just a simple click.
	 
    b. If Eclipse or Maven aren't used add `PluginInterface*.jar` and `PluginCore*.jar` to the classpath of 
	   `PluginClient` (either using your prefered IDE or `java -classpath ...`) and execute Main class. 
	   To avoid spaming of log-messages within the console please add 
	   `java.util.logging.config.file=${basedir}/src/test/resources/logging.properties` to your system properties.
	 
4.   Create plugin samples using `mvn package` for the desired plugin each and add the resulting jar file to 
     PluginClient's plugin subdirectory to automatically load the added jar files on startup or on drag&drop
	 
5.   On executin the PluginClient the following operations are possible:

* `list` - lists all currently loaded plugins
	 
* `load relativePathOfPluginJar` - loads a plugin jar using the name of the jar file that contains the plugin. 
  Example: `load ./SimpleInjectionPlugin.jar` if a jar file with this name is located in the plugin sub-directory 
  of PluginClient
	   
* `unload id` - unloads a plugin using the ID as stated by list command
	 
* `unload fullyQualifiedClassName` - unloads a plugin by providing the fully qualified class name of the 
  plugin implementing class
	   
* `exec id` - executes the plugin using the ID as stated by list command
	 
* `exec fullyQualifiedClassName` - executes the plugin using the fully qualified class name of the plugin 
  implementing class
	 
  
Note that plugins are loaded automatically if they get drag&droped into PluginClient's plugin sub-directory or 
unloaded if they are removed from that directory.