/**
 * Copyright (C) 2009 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.idl.parser;

import static org.testng.Assert.assertNotNull;

import java.io.InputStream;

import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.idl.ASTChecker;
import org.ow2.mind.idl.ASTChecker.IDLChecker;
import org.ow2.mind.idl.ASTChecker.MethodCheckerIterator;
import org.ow2.mind.idl.ASTChecker.ParameterCheckerIterator;
import org.ow2.mind.idl.ASTChecker.TypeCheckerIterator;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.jtb.Parser;
import org.ow2.mind.idl.jtb.syntaxtree.ITFFile;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestJTBProcessor {
  protected static final String DTD = "classpath://org/ow2/mind/idl/mind_v1.dtd";
  XMLNodeFactory                nodeFactory;
  JTBProcessor                  processor;
  Injector                      injector;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    injector = Guice.createInjector(new CommonFrontendModule(),
        new IDLFrontendModule(), new PluginLoaderModule());
    nodeFactory = injector.getInstance(XMLNodeFactory.class);
  }

  protected Parser getParser(final String fileName) throws Exception {
    final ClassLoader loader = getClass().getClassLoader();
    final InputStream is = loader.getResourceAsStream(fileName);
    assertNotNull(is, "Can't find input file \"" + fileName + "\"");
    processor = injector.getInstance(JTBProcessor.class);
    processor.setFilename(fileName);
    return new Parser(is);
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Parser parser = getParser("test1.itf");
    final ITFFile content = parser.ITFFile();
    final InterfaceDefinition idlFile = processor
        .toInterfaceDefinition(content);
    final IDLChecker checker = new ASTChecker().assertIDL(idlFile);
    final TypeCheckerIterator typeChecker = checker.definesType();

    // check myint
    typeChecker.whereFirst().isTypedefOf("myint").isPrimitiveType("int");

    // check mypointer
    typeChecker.andNext().isTypedefOf("mypointer").isPointerOf().isArrayOf()
        .isPrimitiveType("int");

    // check struct s
    typeChecker.andNext().isStructDef("s").hasMembers("a", "b").whereFirst()
        .hasType().isArrayOf().isPrimitiveType("int");

    final MethodCheckerIterator methodsChecker = checker.containsMethods("m1",
        "m2", "m3", "m4", "m5");
    ParameterCheckerIterator paramsChecker;

    // check m1;
    methodsChecker.whereFirst().returnsType().isPrimitiveType("void");
    paramsChecker = methodsChecker.hasParameters("a", "b");
    paramsChecker.whereFirst().hasType().isPrimitiveType("int");
    paramsChecker.andNext().hasType().isPrimitiveType("int");

    // check m2;
    methodsChecker.andNext().returnsType().isPrimitiveType("unsigned float");
    methodsChecker.hasParameters("f1").whereFirst().hasType()
        .isPrimitiveType("float");

    // check m3;
    methodsChecker.andNext().returnsType().isPointerOf()
        .isPrimitiveType("void");
    paramsChecker = methodsChecker.hasParameters("a", "b");
    paramsChecker.whereFirst().hasType().isPrimitiveType("int");
    paramsChecker.andNext().hasType().isPrimitiveType("int");

    // check m4;
    methodsChecker.andNext().returnsType().isPrimitiveType("int");
    paramsChecker = methodsChecker.hasParameters("p0", "p1");
    paramsChecker.whereFirst().hasType().isPrimitiveType("int");
    paramsChecker.andNext().hasType().isPrimitiveType("int");

    // check m5;
    methodsChecker.andNext().returnsType().isPrimitiveType("int");
    paramsChecker = methodsChecker.hasParameters("p1", "p0");
    paramsChecker.whereFirst().hasType().isPrimitiveType("int");
    paramsChecker.andNext().hasType().isPrimitiveType("float");

    assertNotNull(idlFile);
  }
}
