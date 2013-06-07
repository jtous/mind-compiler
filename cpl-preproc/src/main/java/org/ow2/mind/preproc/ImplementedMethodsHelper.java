
package org.ow2.mind.preproc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.adl.implementation.BasicImplementationLocator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

public class ImplementedMethodsHelper {

  // Inject would not work... FIXME
  private static final ImplementationLocator implLocatorItf = new BasicImplementationLocator();

  public static Source getDefinitionSourceFromPath(
      final ImplementationContainer container, final String sourceFileName,
      final Map<Object, Object> context) {

    for (final Source currSource : container.getSources()) {
      try {
        // inlined C code ? TODO: handle this case !!
        if (currSource.getPath() == null) return null;

        // FIXME: Inject instead of hand-made instance
        final URL sourceFileURL = implLocatorItf.findSource(
            currSource.getPath(), context);

        final File sourceFile = new File(sourceFileName);

        if (sourceFileURL.getPath()
            .equals(sourceFile.toURI().toURL().getPath())) return currSource;
      } catch (final MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final NullPointerException e) { // TODO Resolve this mystery
        e.printStackTrace();
      }
    }

    return null;

  }

  /**
   * 
   */
  public static final String IMPLEMENTED_METHODS = "implemented-methods";

  /**
   * @param itf
   * @return
   */
  public static List<String> getImplementedMethods(final Interface itf) {
    final List<String> methNamesList = (List<String>) itf
        .astGetDecoration(IMPLEMENTED_METHODS);
    if (methNamesList == null) return new ArrayList<String>();

    return methNamesList;
  }

  /**
   * @param itf
   * @return
   */
  public static boolean allInterfaceMethodsAreImplemented(final Interface itf) {

    final List<String> unimplementedMethods = getInterfaceUnimplementedMethods(itf);
    if (unimplementedMethods.isEmpty()) return true;

    return false;
  }

  /**
   * @param itf
   * @return
   */
  public static List<String> getInterfaceUnimplementedMethods(
      final Interface itf) {

    final List<String> implMeths = getImplementedMethods(itf);

    final List<String> result = new ArrayList<String>();

    InterfaceDefinition itfDef;

    // assume that itfDef is already loaded
    try {
      itfDef = InterfaceDefinitionDecorationHelper
          .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
    } catch (final ADLException e) {
      return result;
    }

    if (itfDef == null) return result;

    final Method[] methods = itfDef.getMethods();

    for (final Method currMethod : methods) {
      if (!implMeths.contains(currMethod.getName()))
        result.add(currMethod.getName());
    }

    return result;
  }

  public static void addImplementedMethod(final Interface itf,
      final String methName) {
    final List<String> values = getImplementedMethods(itf);
    values.add(methName);
    setImplementedMethods(itf, values);
  }

  /**
   * @param itf
   * @param methNamesList
   */
  public static void setImplementedMethods(final Interface itf,
      final List<String> methNamesList) {

    itf.astSetDecoration(IMPLEMENTED_METHODS, methNamesList);
  }

  /**
   * 
   */
  public static final String SOURCE_VISITED = "source-visited";

  /**
   * @param source
   */
  public static void setSourceVisited(final Source source) {
    source.astSetDecoration(SOURCE_VISITED, Boolean.TRUE);
  }

  /**
   * @param source
   * @return
   */
  public static boolean hasSourceBeenVisited(final Source source) {
    final Boolean value = (Boolean) source.astGetDecoration(SOURCE_VISITED);
    if (value == null) return false;

    return value;
  }

  /**
   * @param container
   * @return
   */
  public static boolean haveAllSourcesBeenVisited(
      final ImplementationContainer container) {

    for (final Source currSource : container.getSources())
      if (!hasSourceBeenVisited(currSource)) return false;

    return true;
  }

}
