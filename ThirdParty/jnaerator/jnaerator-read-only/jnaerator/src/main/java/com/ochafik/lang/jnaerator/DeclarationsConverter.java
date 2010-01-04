/*
	Copyright (c) 2009 Olivier Chafik, All Rights Reserved
	
	This file is part of JNAerator (http://jnaerator.googlecode.com/).
	
	JNAerator is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	JNAerator is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public License
	along with JNAerator.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ochafik.lang.jnaerator;

import static com.ochafik.lang.SyntaxUtils.as;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import org.rococoa.AlreadyRetained;
import org.rococoa.cocoa.foundation.NSObject;

import com.ochafik.lang.jnaerator.JNAeratorConfig.GenFeatures;
import com.ochafik.lang.jnaerator.TypeConversion.JavaPrim;
import com.ochafik.lang.jnaerator.TypeConversion.TypeConversionMode;
import com.ochafik.lang.jnaerator.cplusplus.CPlusPlusMangler;
import com.ochafik.lang.jnaerator.parser.*;
import com.ochafik.lang.jnaerator.parser.Enum;
import com.ochafik.lang.jnaerator.parser.Function;
import com.ochafik.lang.jnaerator.parser.Scanner;
import com.ochafik.lang.jnaerator.parser.Statement.Block;
import com.ochafik.lang.jnaerator.parser.StoredDeclarations.*;
import com.ochafik.lang.jnaerator.parser.TypeRef.*;
import com.ochafik.lang.jnaerator.parser.Expression.*;
import com.ochafik.lang.jnaerator.parser.Function.Type;
import com.ochafik.lang.jnaerator.parser.DeclarationsHolder.ListWrapper;
import com.ochafik.lang.jnaerator.parser.Declarator.*;
import com.ochafik.lang.jnaerator.runtime.Bits;
import com.ochafik.lang.jnaerator.runtime.FastCall;
import com.ochafik.lang.jnaerator.runtime.Mangling;
import com.ochafik.lang.jnaerator.runtime.ObjCBlock;
import com.ochafik.lang.jnaerator.runtime.This;
import com.ochafik.lang.jnaerator.runtime.ThisCall;
import com.ochafik.lang.jnaerator.runtime.VirtualTablePointer;
import com.ochafik.util.CompoundCollection;
import com.ochafik.util.listenable.Pair;
import com.ochafik.util.string.StringUtils;
import com.sun.jna.*;
import com.sun.jna.Pointer;

import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import static com.ochafik.lang.jnaerator.parser.ElementsHelper.*;
import static com.ochafik.lang.jnaerator.TypeConversion.*;

public class DeclarationsConverter {
	private static final String DEFAULT_VPTR_NAME = "_vptr";
	private static final Pattern manglingCommentPattern = Pattern.compile("@mangling (.*)$", Pattern.MULTILINE);

	public DeclarationsConverter(Result result) {
		this.result = result;
	}

	protected final Result result;
	
	
	public void convertCallback(FunctionSignature functionSignature, Signatures signatures, DeclarationsHolder out, Identifier callerLibraryName) {
		Identifier name = result.typeConverter.inferCallBackName(functionSignature, true, false, callerLibraryName);
		if (name == null)
			return;
		
		name = result.typeConverter.getValidJavaArgumentName(name);
		
		Function function = functionSignature.getFunction();
		
		int i = 1;
		Identifier chosenName = name;
		while (!(signatures.classSignatures.add(chosenName))) {
			chosenName = ident(name.toString() + (++i));
		}
		
		Element parent = functionSignature.getParentElement();
		Element comel = parent != null && parent instanceof TypeDef ? parent : functionSignature;
		
		Struct callbackStruct = new Struct();
		callbackStruct.setType(Struct.Type.JavaInterface);
		callbackStruct.addModifiers(Modifier.Public);
		callbackStruct.setParents(Arrays.asList(ident(
			FunctionSignature.Type.ObjCBlock.equals(functionSignature.getType()) ?
				ObjCBlock.class : 
				Callback.class
		)));
		callbackStruct.setTag(ident(chosenName));
		if (!result.config.noComments)
			callbackStruct.addToCommentBefore(comel.getCommentBefore(), comel.getCommentAfter(), getFileCommentContent(comel));
		convertFunction(function, new Signatures(), true, callbackStruct, callerLibraryName);
		for (Declaration d : callbackStruct.getDeclarations()) {
			if (d instanceof Function) {
				callbackStruct.addAnnotations(callbackStruct.getAnnotations());
				callbackStruct.setAnnotations(null);
				break;
			}
		}
		out.addDeclaration(new TaggedTypeRefDeclaration(callbackStruct));
	}

	public void convertCallbacks(List<FunctionSignature> functionSignatures, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (functionSignatures != null) {
			for (FunctionSignature functionSignature : functionSignatures) {
				if (functionSignature.findParentOfType(Struct.class) != null)
					continue;
				
				Arg a = functionSignature.findParentOfType(Arg.class);
				if (a != null && a.getParentElement() == null)
					continue;//TODO understand why we end up having an orphan Arg here !!!!
					
				convertCallback(functionSignature, signatures, out, libraryClassName);
			}
		}
		
	}

	public static class DeclarationsOutput {
		Map<String, DeclarationsHolder> holders = new HashMap<String, DeclarationsHolder>();
		public void add(Declaration d, String libraryName) {
			
		}
		public void set(String libraryName, DeclarationsHolder holder) {
			
		}
	}
	public void convertConstants(String library, List<Define> defines, Element sourcesRoot, final Signatures signatures, final DeclarationsHolder out, final Identifier libraryClassName) {
		//final List<Define> defines = new ArrayList<Define>();
		final Map<String, String> constants = Result.getMap(result.stringConstants, library);
//		
		sourcesRoot.accept(new Scanner() {
//			@Override
//			public void visitDefine(Define define) {
//				super.visitDefine(define);
//				if (elementsFilter.accept(define))
//					defines.add(define);
//			}
			@Override
			public void visitVariablesDeclaration(VariablesDeclaration v) {
				super.visitVariablesDeclaration(v);
				//if (!elementsFilter.accept(v))
				//	return;
				
				if (v.findParentOfType(Struct.class) != null)
					return;
				
				if (v.getValueType() instanceof FunctionSignature)
					return;
					
				for (Declarator decl : v.getDeclarators()) {
					if (!(decl instanceof DirectDeclarator))
						continue; // TODO provide a mapping of exported values
					
					TypeRef mutatedType = (TypeRef) decl.mutateType(v.getValueType());
					if (mutatedType == null || 
							!mutatedType.getModifiers().contains(Modifier.Const) ||
							mutatedType.getModifiers().contains(Modifier.Extern) ||
							decl.getDefaultValue() == null)
						continue;
					
					//TypeRef type = v.getValueType();
					String name = decl.resolveName();
					
					JavaPrim prim = result.typeConverter.getPrimitive(mutatedType, libraryClassName);
					if (prim == null) {
						if (mutatedType.toString().contains("NSString")) {
							String value = constants.get(name);
							if (value != null)
								outputNSString(name, value, out, signatures, v, decl);
						}
						continue;
					}
					
					try {
						
						//DirectDeclarator dd = (DirectDeclarator)decl;
						Pair<Expression, TypeRef> val = result.typeConverter.convertExpressionToJava(decl.getDefaultValue(), libraryClassName, true);
						
						if (!signatures.variablesSignatures.add(name))
							continue;
						
						TypeRef tr = prim == JavaPrim.NativeLong || prim == JavaPrim.NativeSize ?
							typeRef("long") :
							result.typeConverter.convertTypeToJNA(mutatedType, TypeConversion.TypeConversionMode.FieldType, libraryClassName)
						;
						VariablesDeclaration vd = new VariablesDeclaration(tr, new DirectDeclarator(name, val.getFirst()));
						if (!result.config.noComments) {
							vd.setCommentBefore(v.getCommentBefore());
							vd.addToCommentBefore(decl.getCommentBefore());
							vd.addToCommentBefore(decl.getCommentAfter());
							vd.addToCommentBefore(v.getCommentAfter());
						}
						
						out.addDeclaration(vd);
					} catch (UnsupportedConversionException e) {
						out.addDeclaration(skipDeclaration(v, e.toString()));
					}
					
				}
			}

		});
		
		if (defines != null) {
			for (Define define : reorderDefines(defines)) {
				if (define.getValue() == null)
					continue;
				
				try {
					out.addDeclaration(outputConstant(define.getName(), define.getValue(), signatures, define.getValue(), "define", libraryClassName, true, false, false));
				} catch (UnsupportedConversionException ex) {
					out.addDeclaration(skipDeclaration(define, ex.toString()));
				}
			}
		}
		for (Map.Entry<String, String> e : constants.entrySet()) {
			outputNSString(e.getKey(), e.getValue(), out, signatures);
		}
	}


	private void outputNSString(String name, String value, DeclarationsHolder out, Signatures signatures, Element... elementsToTakeCommentsFrom) {

		if (!signatures.variablesSignatures.add(name))
			return;
		
		TypeRef tr = typeRef(String.class);
		VariablesDeclaration vd = new VariablesDeclaration(tr, new DirectDeclarator(name, expr(value)));
		if (!result.config.noComments)
			for (Element e : elementsToTakeCommentsFrom) {
				vd.addToCommentBefore(e.getCommentBefore());
				vd.addToCommentBefore(e.getCommentAfter());
			}
		vd.addModifiers(Modifier.Public);
		out.addDeclaration(vd);
	}
	static Map<Class<?>, Pair<List<Pair<Function, String>>, Set<String>>> cachedForcedMethodsAndTheirSignatures;
	
	public static synchronized Pair<List<Pair<Function,String>>,Set<String>> getMethodsAndTheirSignatures(Class<?> originalLib) {
		if (cachedForcedMethodsAndTheirSignatures == null)
			cachedForcedMethodsAndTheirSignatures = new HashMap<Class<?>, Pair<List<Pair<Function, String>>,Set<String>>>();

		Pair<List<Pair<Function, String>>, Set<String>> pair = cachedForcedMethodsAndTheirSignatures.get(originalLib);
		if (pair == null) {
			pair = new Pair<List<Pair<Function, String>>, Set<String>>(new ArrayList<Pair<Function, String>>(), new HashSet<String>());
			for (Method m : originalLib.getDeclaredMethods()) {
				Function f = Function.fromMethod(m);
				String sig = f.computeSignature(false);
				//if (m.getDeclaringClass().equals(NSObject.class) && f.getName().equals("as")) {
				//	Declaration
				//}
				pair.getFirst().add(new Pair<Function, String>(f, sig));
				pair.getSecond().add(sig);
			}
		}
		return pair;
	}
	
	public void addMissingMethods(Class<?> originalLib, Signatures existingSignatures, Struct outputLib) {
		for (Pair<Function, String> f : getMethodsAndTheirSignatures(originalLib).getFirst())
			if (existingSignatures.methodsSignatures.add(f.getSecond()))
				outputLib.addDeclaration(f.getFirst().clone());
	}
	
	public EmptyDeclaration skipDeclaration(Element e, String... preMessages) {
		if (result.config.limitComments)
			return null;
		
		List<String> mess = new ArrayList<String>();
		if (preMessages != null)
			mess.addAll(Arrays.asList(preMessages));
		mess.addAll(Arrays.asList("SKIPPED:", e.formatComments("", true, true, false), getFileCommentContent(e), e.toString().replace("*/", "* /")));
		return new EmptyDeclaration(mess.toArray(new String[0]));
	}
	
	public void convertEnum(Enum e, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (e.isForwardDeclaration())
			return;
		
		DeclarationsHolder localOut = out;
		Signatures localSignatures = signatures;
		
		Struct enumInterf = null;
		Identifier enumName = getActualTaggedTypeName(e);
		boolean repeatFullEnumComment;
		if (enumName != null && enumName.resolveLastSimpleIdentifier().getName() != null) {
			if (!signatures.classSignatures.add(enumName))
				return;
			
			repeatFullEnumComment = false;
			
			enumInterf = publicStaticClass(enumName, null, Struct.Type.JavaInterface, e);
			if (!result.config.noComments)
				if (result.config.features.contains(JNAeratorConfig.GenFeatures.EnumTypeLocationComments))
					enumInterf.addToCommentBefore("enum values");
			out.addDeclaration(new TaggedTypeRefDeclaration(enumInterf));
			
			localSignatures = new Signatures();
			localOut = enumInterf;
		} else {
			repeatFullEnumComment = true;
		}
		Integer lastAdditiveValue = null;
		Expression lastRefValue = null;
		boolean failedOnceForThisEnum = false;
		for (com.ochafik.lang.jnaerator.parser.Enum.EnumItem item : e.getItems()) {
			Expression resultingExpression;
			try {
				if (item.getValue() == null) {
					// no explicit value
					if (lastRefValue == null) {
						if (lastAdditiveValue != null) {
							lastAdditiveValue++;
							resultingExpression = expr(lastAdditiveValue);
						} else {
							if (item == e.getItems().get(0)) {
								lastAdditiveValue = 0;
								resultingExpression = expr(lastAdditiveValue);
							} else
								resultingExpression = null;
						}
					} else {
						// has a last reference value
						if (lastAdditiveValue != null)
							lastAdditiveValue++;
						else
							lastAdditiveValue = 1;
						
						resultingExpression = //result.typeConverter.convertExpressionToJava(
							expr(
								lastRefValue.clone(), 
								Expression.BinaryOperator.Plus, 
								expr(lastAdditiveValue)
							//)
						);
					}
				} else {
					// has an explicit value
					failedOnceForThisEnum = false;// reset skipping
					lastAdditiveValue = null;
					lastRefValue = item.getValue();
					resultingExpression = lastRefValue;
					if (lastRefValue instanceof Expression.Constant) {
						try {
							lastAdditiveValue = ((Expression.Constant)lastRefValue).asInteger();
							lastRefValue = null;
						} catch (Exception ex) {}
					}	
				}
			} catch (Exception ex) {
				//ex.printStackTrace();
				resultingExpression = null;
			}
			failedOnceForThisEnum = failedOnceForThisEnum || resultingExpression == null;
			if (failedOnceForThisEnum)
				out.addDeclaration(skipDeclaration(item));
			else {
				try {
					Declaration ct = outputConstant(
						item.getName(), 
						result.typeConverter.convertExpressionToJava(resultingExpression, libraryClassName, true),
						localSignatures, 
						item, 
						"enum item", 
						libraryClassName, 
						enumInterf == null,
						true,
						true,
						true
					);
					if (!result.config.noComments)
						if (ct != null && repeatFullEnumComment) {
							String c = ct.getCommentBefore();
							ct.setCommentBefore(e.getCommentBefore());
							ct.addToCommentBefore(c);
						}
					localOut.addDeclaration(ct);
				} catch (Exception ex) {
					out.addDeclaration(skipDeclaration(item, ex.toString()));
				}
			}
		}
		//if (enumInterf != null)
		//	enumInterf.addDeclarations(localOut);
	}

	private Declaration outputConstant(String name, Expression x, Signatures signatures, Element element, String elementTypeDescription, Identifier libraryClassName, boolean addFileComment, boolean signalErrors, boolean forceInteger) throws UnsupportedConversionException {
		return outputConstant(name, pair(x, (TypeRef)null), signatures, element, elementTypeDescription, libraryClassName, addFileComment, signalErrors, forceInteger, false);
	}
	@SuppressWarnings("static-access")
	private Declaration outputConstant(String name, Pair<Expression, TypeRef> x, Signatures signatures, Element element, String elementTypeDescription, Identifier libraryClassName, boolean addFileComment, boolean signalErrors, boolean forceInteger, boolean alreadyConverted) throws UnsupportedConversionException {
		try {
			if (result.typeConverter.isJavaKeyword(name))
				throw new UnsupportedConversionException(element, "The name '" + name + "' is invalid for a Java field.");
			
			Pair<Expression, TypeRef> converted = alreadyConverted ? x : result.typeConverter.convertExpressionToJava(x.getFirst(), libraryClassName, true);
			//TypeRef tr = result.typeConverter.inferJavaType(converted);
			JavaPrim prim = result.typeConverter.getPrimitive(converted.getValue(), libraryClassName);
			
			if (forceInteger && prim == JavaPrim.Boolean) {
				prim = JavaPrim.Int;
				//tr = typeRef("int");
				converted = pair(expr("true".equals(String.valueOf(converted.toString())) ? 1 : 0), typeRef(Integer.TYPE));
			}
			
			if ((prim == null || converted.getValue() == null) && signalErrors) {
				if (result.config.limitComments)
					return null;
				
				return new EmptyDeclaration("Failed to infer type of " + converted);
			} else if (prim != JavaPrim.Void && converted.getValue() != null) {
//				if (prim == JavaPrim.Int)
//					tr = typeRef("long");
				
				if (signatures.variablesSignatures.add(name)) {
					String t = converted.toString();
					if (t.contains("sizeof")) {
						converted = alreadyConverted ? x : result.typeConverter.convertExpressionToJava(x.getFirst(), libraryClassName, false);
					}

					//TypeRef tr = new TypeRef.SimpleTypeRef(result.typeConverter.typeToJNA(type, vs, TypeConversion.TypeConversionMode.FieldType, callerLibraryClass));
					Declaration declaration = new VariablesDeclaration(converted.getValue(), new DirectDeclarator(name, converted.getFirst()));
					declaration.addModifiers(Modifier.Public, Modifier.Static, Modifier.Final);
					declaration.importDetails(element, false);
					declaration.moveAllCommentsBefore();
					if (!result.config.noComments)
						if (addFileComment)
							declaration.addToCommentBefore(getFileCommentContent(element));
					return declaration;
				}
			}
			return skipDeclaration(element, elementTypeDescription);
		} catch (UnsupportedConversionException e) {
			return skipDeclaration(element, elementTypeDescription, e.toString());
		}	
		
	} 

	public void convertEnums(List<Enum> enums, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (enums != null) {
			//out.println("public static class ENUMS {");
			for (com.ochafik.lang.jnaerator.parser.Enum e : enums) {
				if (e.findParentOfType(Struct.class) != null)
					continue;
				
				convertEnum(e, signatures, out, libraryClassName);
			}
			//out.println("}");
		}
	}

	Map<String, Pair<Function, List<Function>>> functionAlternativesByNativeSignature = new LinkedHashMap<String, Pair<Function, List<Function>>>();

	static <E extends Element> E cleanClone(E e) {
		E c = (E)e.clone();
		c.setCommentBefore(null);
		c.setCommentAfter(null);
		if (c instanceof Declaration) {
			Declaration d = (Declaration)c;
			d.setAnnotations(null);
		}
		return c;
	}
	
	public void convertFunction(Function function, Signatures signatures, boolean isCallback, DeclarationsHolder out, Identifier libraryClassName) {
		if (result.config.functionsAccepter != null && !result.config.functionsAccepter.adapt(function))
			return;
		
		//if (function.findParentOfType(Template))
		String library = result.getLibrary(function);
		Identifier functionName = function.getName();
		if (functionName == null) {
			if (function.getParentElement() instanceof FunctionSignature)
				functionName = ident("invoke");
			else
				return;
		}
		if (function.getParentElement() instanceof FriendDeclaration)
			return;
		
		if (functionName.toString().contains("<")) {
			return;
		}
		functionName = result.typeConverter.getValidJavaMethodName(functionName);
		if (functionName == null)
			return;

		String sig = function.computeSignature(false);
		Pair<Function, List<Function>> alternativesPair = functionAlternativesByNativeSignature.get(sig);
		if (alternativesPair != null) {
			if (result.config.choicesInputFile != null) {
				for (Function alt : alternativesPair.getValue())
					out.addDeclaration(alt.clone());
				return;
			}
		} else {
			functionAlternativesByNativeSignature.put(
				sig,
				alternativesPair = new Pair<Function, List<Function>>(
					cleanClone(function),
					new ArrayList<Function>()
				)
			);
		}
		List<Function> alternatives = alternativesPair.getValue();

		Function natFunc = new Function();
		
		Element parent = function.getParentElement();
		List<String> ns = new ArrayList<String>(function.getNameSpace());
		boolean isMethod = parent instanceof Struct;
		if (isMethod) {
			ns.clear();
			ns.addAll(parent.getNameSpace());
			switch (((Struct)parent).getType()) {
			case ObjCClass:
			case ObjCProtocol:
				break;
			case CPPClass:
				if (!result.config.genCPlusPlus && !Modifier.Static.isContainedBy(function.getModifiers()))
					return;
				ns.add(((Struct)parent).getTag().toString());
				break;
			}
		}
		
		if (!isMethod && library != null) {
			Boolean alreadyRetained = Result.getMap(result.retainedRetValFunctions, library).get(functionName.toString());
			if (alreadyRetained != null && alreadyRetained) {
				natFunc.addAnnotation(new Annotation(AlreadyRetained.class, expr(alreadyRetained)));
			}
		}
		//String namespaceArrayStr = "{\"" + StringUtils.implode(ns, "\", \"") + "\"}";
		//if (!ns.isEmpty())
		//	natFunc.addAnnotation(new Annotation(Namespace.class, "(value=" + namespaceArrayStr + (isMethod ? ", isClass=true" : "") + ")"));
		boolean isObjectiveC = function.getType() == Type.ObjCMethod;

		natFunc.setType(Function.Type.JavaMethod);
		if (result.config.useJNADirectCalls && !isCallback && !isObjectiveC) {
			natFunc.addModifiers(Modifier.Public, Modifier.Static, Modifier.Native);
		}

		try {
			//StringBuilder outPrefix = new StringBuilder();
			TypeRef returnType = null;
			
			if (!isObjectiveC) {
				returnType = function.getValueType();
				if (returnType == null)
					returnType = new TypeRef.Primitive("int");
				if (returnType != null)
					returnType.addModifiers(function.getModifiers());
			} else {
				returnType = RococoaUtils.fixReturnType(function);
				functionName = ident(RococoaUtils.getMethodName(function));
			}
			
			Identifier modifiedMethodName;
			if (isCallback) {
				modifiedMethodName = ident("invoke");
			} else {
				modifiedMethodName = result.typeConverter.getValidJavaMethodName(ident(StringUtils.implode(ns, result.config.cPlusPlusNameSpaceSeparator) + (ns.isEmpty() ? "" : result.config.cPlusPlusNameSpaceSeparator) + functionName));
			}
			Set<String> names = new LinkedHashSet<String>();
			//if (ns.isEmpty())
			
			if (!result.config.noMangling)
				if (!isCallback && !isObjectiveC && result.config.features.contains(JNAeratorConfig.GenFeatures.CPlusPlusMangling))
					addCPlusPlusMangledNames(function, names);
			
			if (!modifiedMethodName.equals(functionName) && ns.isEmpty())
				names.add(function.getName().toString());
			if (function.getAsmName() != null)
				names.add(function.getAsmName());
			
			if (!isCallback && !names.isEmpty())
				natFunc.addAnnotation(new Annotation(Mangling.class, "({\"" + StringUtils.implode(names, "\", \"") + "\"})"));

			boolean needsThis = false, needsThisAnnotation = false;
			if (Modifier.__fastcall.isContainedBy(function.getModifiers())) {
				natFunc.addAnnotation(new Annotation(FastCall.class));
				needsThis = true;
			}
			if (Modifier.__thiscall.isContainedBy(function.getModifiers())) {
				natFunc.addAnnotation(new Annotation(ThisCall.class));
				needsThis = true;
			}
			if (function.getType() == Type.CppMethod && !function.getModifiers().contains(Modifier.Static)) {
				needsThisAnnotation = true;
				needsThis = true;
			}
			
			if (needsThis && !result.config.genCPlusPlus)
				return;
			
			if (needsThis) {
				natFunc.addAnnotation(new Annotation(Deprecated.class));
				
				TypeRef classRef;
				if (parent instanceof Struct) {
					classRef = typeRef(((Struct)function.getParentElement()).getTag().clone());
				} else {
					classRef = null;
				}
				if (classRef != null) {
					natFunc.addArg((Arg)new Arg("__this__", classRef)).addAnnotation(needsThisAnnotation ? new Annotation(This.class) : null);
				}
			}
				
			//if (isCallback || !modifiedMethodName.equals(functionName))
			//	natFunc.addAnnotation(new Annotation(Name.class, "(value=\"" + functionName + "\"" + (ns.isEmpty() ? "" : ", namespace=" + namespaceArrayStr)  + (isMethod ? ", classMember=true" : "") + ")"));

			//if (modifiedMethodName.toString().equals("NSStringFromSelector"))
			//	modifiedMethodName = ident("NSStringFromSelector");

			natFunc.setName(modifiedMethodName);
			natFunc.setValueType(result.typeConverter.convertTypeToJNA(returnType, TypeConversionMode.ReturnType, libraryClassName));
			if (!result.config.noComments) {
				natFunc.importDetails(function, false);
				natFunc.moveAllCommentsBefore();
				if (!isCallback)
					natFunc.addToCommentBefore(getFileCommentContent(function));
			}

            if (function.getName() != null) {
                Object[] name = new Object[] {function.getName().toString()};
                for (Pair<MessageFormat, MessageFormat> mf : result.config.onlineDocumentationURLFormats) {
                    try {
                        MessageFormat urlFormat = mf.getSecond();
                        URL url = new URL(urlFormat.format(name));
                        URLConnection con = url.openConnection();
                        con.getInputStream().close();
                        MessageFormat displayFormat = mf.getFirst();
                        natFunc.addToCommentBefore("@see <a href=\"" + url + "\">" + displayFormat.format(name) + "</a>");
                        break;
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                    }
                }
            }
			
			boolean alternativeOutputs = !isCallback;
			
			Function primOrBufFunc = alternativeOutputs ? natFunc.clone() : null;
			Function natStructFunc = alternativeOutputs ? natFunc.clone() : null;
			
			Set<String> argNames = new TreeSet<String>();
//			for (Arg arg : function.getArgs())
//				if (arg.getName() != null) 
//					argNames.add(arg.getName());
				
			int iArg = 1;
			for (Arg arg : function.getArgs()) {
				if (arg.isVarArg() && arg.getValueType() == null) {
					//TODO choose vaname dynamically !
					Identifier vaType = ident(isObjectiveC ? NSObject.class : Object.class);
					String argName = chooseJavaArgName("varargs", iArg, argNames);
					natFunc.addArg(new Arg(argName, typeRef(vaType.clone()))).setVarArg(true);
					if (alternativeOutputs) {
						primOrBufFunc.addArg(new Arg(argName, typeRef(vaType.clone()))).setVarArg(true);
						natStructFunc.addArg(new Arg(argName, typeRef(vaType.clone()))).setVarArg(true);
					}
				} else {
					String argName = chooseJavaArgName(arg.getName(), iArg, argNames);
					
					TypeRef mutType = arg.createMutatedType();
					if (mutType == null)
						throw new UnsupportedConversionException(function, "Argument " + arg.getName() + " cannot be converted");
					
					if (mutType.toString().contains("NSOpenGLContextParameter")) {
						argName = argName.toString();
					}
					natFunc.addArg(new Arg(argName, result.typeConverter.convertTypeToJNA(mutType, TypeConversionMode.NativeParameter, libraryClassName)));
					if (alternativeOutputs) {
						primOrBufFunc.addArg(new Arg(argName, result.typeConverter.convertTypeToJNA(mutType, TypeConversionMode.PrimitiveOrBufferParameter, libraryClassName)));
						natStructFunc.addArg(new Arg(argName, result.typeConverter.convertTypeToJNA(mutType, TypeConversionMode.NativeParameterWithStructsPtrPtrs, libraryClassName)));
					}
				}
				iArg++;
			}
			
			String natSign = natFunc.computeSignature(false),
				primOrBufSign = alternativeOutputs ? primOrBufFunc.computeSignature(false) : null,
				bufSign = alternativeOutputs ? natStructFunc.computeSignature(false) : null;
				
			if (signatures == null || signatures.methodsSignatures.add(natSign)) {
				if (alternativeOutputs && !primOrBufSign.equals(natSign)) {
					if (!result.config.noComments) {
						if (primOrBufSign.equals(bufSign))
							natFunc.addToCommentBefore(Arrays.asList("@deprecated use the safer method {@link #" + primOrBufSign + "} instead"));
						else
							natFunc.addToCommentBefore(Arrays.asList("@deprecated use the safer methods {@link #" + primOrBufSign + "} and {@link #" + bufSign + "} instead"));
					}
					natFunc.addAnnotation(new Annotation(Deprecated.class));
				}
				collectParamComments(natFunc);
				out.addDeclaration(natFunc);
				alternatives.add(cleanClone(natFunc));
			}
			
			if (alternativeOutputs) {
				if (signatures == null || signatures.methodsSignatures.add(primOrBufSign)) {
					collectParamComments(primOrBufFunc);
					out.addDeclaration(primOrBufFunc);
					alternatives.add(cleanClone(primOrBufFunc));
				}
				if (signatures == null || signatures.methodsSignatures.add(bufSign)) {
					collectParamComments(natStructFunc);
					out.addDeclaration(natStructFunc);
					alternatives.add(cleanClone(natStructFunc));
				}
			}
		} catch (UnsupportedConversionException ex) {
			if (!result.config.limitComments)
				out.addDeclaration(new EmptyDeclaration(getFileCommentContent(function), ex.toString()));
		}
	}

	protected boolean isCPlusPlusFileName(String file) {
		if (file == null)
			return true;
			
		file = file.toLowerCase();
		return !file.endsWith(".c") && !file.endsWith(".m");
	}
	private void addCPlusPlusMangledNames(Function function, Set<String> names) {
		if (function.getType() == Type.ObjCMethod)
			return;
		
		String elementFile = result.resolveFile(function);
		if (elementFile != null && (
				elementFile.contains(".framework/") ||
				elementFile.endsWith(".bridgesupport")))
			return;
		
		ExternDeclarations externDeclarations = function.findParentOfType(ExternDeclarations.class);
		if (externDeclarations != null && !"C++".equals(externDeclarations.getLanguage()))
			return;
		
		if (!isCPlusPlusFileName(Element.getFileOfAscendency(function)))
			return;
		
		/// Parse or infer name manglings
		List<String[]> mats = function.getCommentBefore() == null ? null : com.ochafik.util.string.RegexUtils.find(function.getCommentBefore(), manglingCommentPattern);
		if (mats != null && !mats.isEmpty()) {
			for (String[] mat : mats)
				names.add(mat[1]);
		} else {
			for (CPlusPlusMangler mangler : result.config.cPlusPlusManglers) {
				try {
					names.add(mangler.mangle(function, result));
				} catch (Exception ex) {
					System.err.println("Error in mangling of '" + function.computeSignature(true) + "' : " + ex);
					ex.printStackTrace();
				}
			}
		}
		
	}

	private void collectParamComments(Function f) {
		for (Arg arg : f.getArgs()) {
			arg.moveAllCommentsBefore();
			TypeRef argType = arg.getValueType();
			if (argType != null) {
				if (!result.config.noComments) {
					argType.moveAllCommentsBefore();
					arg.addToCommentBefore(argType.getCommentBefore());
				}
				argType.stripDetails();
			}
			if (arg.getCommentBefore() != null) {
				if (!result.config.noComments)
					f.addToCommentBefore("@param " + arg.getName() + " " + Element.cleanComment(arg.getCommentBefore()));
				arg.stripDetails();
			}
		}
	}

	public void convertFunctions(List<Function> functions, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (functions != null) {
			//System.err.println("FUNCTIONS " + functions);
			for (Function function : functions) {
				convertFunction(function, signatures, false, out, libraryClassName);
			}
		}
	}

	public Identifier getActualTaggedTypeName(TaggedTypeRef struct) {
		Identifier structName = null;
		Identifier tag = struct.getTag();
		if (tag == null || tag.isPlain() && tag.toString().startsWith("_")) {
			TypeDef parentDef = as(struct.getParentElement(), TypeDef.class);
			if (parentDef != null) {
				structName = new Identifier.SimpleIdentifier(JNAeratorUtils.findBestPlainStorageName(parentDef));
			} else if (tag != null) {
				String better = tag.toString().substring(1);
				Pair<TypeDef, Declarator> pair = result.typeDefs.get(better);
				if (pair != null && pair.getFirst().getValueType() != null && pair.getSecond() instanceof DirectDeclarator) {
					TypeRef tr = pair.getFirst().getValueType();
					DirectDeclarator dd = (DirectDeclarator) pair.getSecond();
					
					if (tr instanceof SimpleTypeRef) {
						if (tag.equals(((SimpleTypeRef)tr).getName()))
							structName = ident(dd.resolveName());
					} else if (tr instanceof TaggedTypeRef) {
						if (tag.equals(((TaggedTypeRef)tr).getTag()))
							structName = ident(dd.resolveName());
					}
				}
			}
		}
		if (structName == null || structName.toString().equals(""))
			structName = tag;
		return structName == null ? null : structName.clone();
	}
	public Struct convertStruct(Struct struct, Signatures signatures, Identifier callerLibraryClass, boolean onlyFields) throws IOException {
		Identifier structName = getActualTaggedTypeName(struct);
		if (structName == null)
			return null;
		
		//if (structName.toString().contains("MonoSymbolFile"))
		//	structName.toString();
		
		if (struct.isForwardDeclaration())// && !result.structsByName.get(structName).isForwardDeclaration())
			return null;
		
		if (!signatures.classSignatures.add(structName))
			return null;

		boolean isUnion = struct.getType() == Struct.Type.CUnion;
		boolean inheritsFromStruct = false;
		Identifier baseClass = null;
		if (!onlyFields) {
			if (!struct.getParents().isEmpty()) {
				for (Identifier parentName : struct.getParents()) {
					Struct parent = result.structsByName.get(parentName);
					if (parent == null) {
						// TODO report error
						continue;
					}
					baseClass = result.getTaggedTypeIdentifierInJava(parent);
					if (baseClass != null) {
						inheritsFromStruct = true;
						break; // TODO handle multiple and virtual inheritage
					}
				}
			}
			if (baseClass == null) {
				if (result.config.useJNAeratorUnionAndStructClasses) {
					Class<?> c = isUnion ? com.ochafik.lang.jnaerator.runtime.Union.class : com.ochafik.lang.jnaerator.runtime.Structure.class;
					baseClass = ident(
						c, 
						expr(typeRef(structName.clone())), 
						expr(typeRef(ident(structName.clone(), "ByValue"))), 
						expr(typeRef(ident(structName.clone(), "ByReference")))
					);
				} else
					baseClass = ident(struct.getType() == Struct.Type.CUnion ? Union.class : Structure.class);
			}
		}
		Struct structJavaClass = publicStaticClass(structName, baseClass, Struct.Type.JavaClass, struct);
		
		final int iChild[] = new int[] {0};
		
		//cl.addDeclaration(new EmptyDeclaration())
		Signatures childSignatures = new Signatures();
		
		if (isVirtual(struct) && !onlyFields) {
			String vptrName = DEFAULT_VPTR_NAME;
			VariablesDeclaration vptr = new VariablesDeclaration(typeRef(VirtualTablePointer.class), new Declarator.DirectDeclarator(vptrName));
			vptr.addModifiers(Modifier.Public);
			structJavaClass.addDeclaration(vptr);
			childSignatures.variablesSignatures.add(vptrName);
			// TODO add vptr grabber to constructor !
		}
		
		//List<Declaration> children = new ArrayList<Declaration>();
		for (Declaration d : struct.getDeclarations()) {
			if (d instanceof VariablesDeclaration) {
				convertVariablesDeclaration((VariablesDeclaration)d, structJavaClass, iChild, callerLibraryClass);
			} else if (!onlyFields) {
				if (d instanceof TaggedTypeRefDeclaration) {
					TaggedTypeRef tr = ((TaggedTypeRefDeclaration) d).getTaggedTypeRef();
					if (tr instanceof Struct) {
						outputConvertedStruct((Struct)tr, childSignatures, structJavaClass, callerLibraryClass, false);
					} else if (tr instanceof Enum) {
						convertEnum((Enum)tr, childSignatures, structJavaClass, callerLibraryClass);
					}
				} else if (d instanceof TypeDef) {
					TypeDef td = (TypeDef)d;
					TypeRef tr = td.getValueType();
					if (tr instanceof Struct) {
						outputConvertedStruct((Struct)tr, childSignatures, structJavaClass, callerLibraryClass, false);
					} else if (tr instanceof FunctionSignature) {
						convertCallback((FunctionSignature)tr, childSignatures, structJavaClass, callerLibraryClass);
					}
				} else if (result.config.genCPlusPlus && d instanceof Function) {
					Function f = (Function) d;
					String library = result.getLibrary(struct);
					if (library == null)
						continue;
					List<Declaration> decls = new ArrayList<Declaration>();
					convertFunction(f, childSignatures, false, new ListWrapper(decls), callerLibraryClass);
					for (Declaration md : decls) {
						if (!(md instanceof Function))
							continue;
						Function method = (Function) md;
						Identifier methodImplName = method.getName().clone();
						Identifier methodName = result.typeConverter.getValidJavaMethodName(f.getName());
						method.setName(methodName);
						List<Expression> args = new ArrayList<Expression>();
						
						boolean isStatic = Modifier.Static.isContainedBy(f.getModifiers());
						int iArg = 0;
						for (Arg arg : new ArrayList<Arg>(method.getArgs())) {
							if (iArg == 0 && !isStatic) {
								arg.replaceBy(null);
								args.add(varRef("this"));
							} else
								args.add(varRef(arg.getName()));
							iArg++;
						}
						Expression implCall = methodCall(result.getLibraryInstanceReferenceExpression(library), MemberRefStyle.Dot, methodImplName.toString(), args.toArray(new Expression[args.size()]));
						method.setBody(block(
							"void".equals(String.valueOf(method.getValueType())) ?
								stat(implCall) : 
								new Statement.Return(implCall)
						));
						method.addModifiers(Modifier.Public, isStatic ? Modifier.Static : null);
						structJavaClass.addDeclaration(method);
					}
				}
			}
		}
		
		if (!onlyFields) {
			if (result.config.features.contains(GenFeatures.StructConstructors))
				addStructConstructors(structName, structJavaClass/*, byRef, byVal*/, struct);
			
			Struct byRef = publicStaticClass(ident("ByReference"), structName, Struct.Type.JavaClass, null, ident(ident(Structure.class), "ByReference"));
			Struct byVal = publicStaticClass(ident("ByValue"), structName, Struct.Type.JavaClass, null, ident(ident(Structure.class), "ByValue"));
			
			if (!inheritsFromStruct) {
				structJavaClass.addDeclaration(createNewStructMethod("newByReference", byRef));
				structJavaClass.addDeclaration(createNewStructMethod("newByValue", byVal));
			}
			structJavaClass.addDeclaration(createNewStructMethod("newInstance", structJavaClass));

			structJavaClass.addDeclaration(createNewStructArrayMethod(structJavaClass, isUnion));

			structJavaClass.addDeclaration(decl(byRef));
			structJavaClass.addDeclaration(decl(byVal));
		}
		return structJavaClass;
	}
	void outputConvertedStruct(Struct struct, Signatures signatures, DeclarationsHolder out, Identifier callerLibraryClass, boolean onlyFields) throws IOException {
		Struct structJavaClass = convertStruct(struct, signatures, callerLibraryClass, onlyFields);
		if (structJavaClass == null)
			return;
		
		if (result.config.putTopStructsInSeparateFiles && struct.findParentOfType(Struct.class) == null) {
			String library = result.getLibrary(struct);
			Identifier javaPackage = result.getLibraryPackage(library);
			Identifier fullClassName = ident(javaPackage, structJavaClass.getTag().clone());
			
			structJavaClass.removeModifiers(Modifier.Static);
			structJavaClass = result.notifyBeforeWritingClass(fullClassName, structJavaClass, signatures, library);
			if (structJavaClass != null) {
				PrintWriter pout = result.classOutputter.getClassSourceWriter(fullClassName.toString());
				result.printJavaHeader(javaPackage, pout);
				pout.println(structJavaClass);
				pout.close();
			}
		} else
			out.addDeclaration(decl(structJavaClass));
	}

	Map<Identifier, Boolean> structsVirtuality = new HashMap<Identifier, Boolean>();
	public boolean isVirtual(Struct struct) {
		Identifier name = getActualTaggedTypeName(struct);
		Boolean bVirtual = structsVirtuality.get(name);
		if (bVirtual == null) {
			boolean hasVirtualParent = false, hasVirtualMembers = false;
			for (Identifier parentName : struct.getParents()) {
				Struct parentStruct = result.structsByName.get(parentName);
				if (parentStruct == null) {
					if (result.config.verbose)
						System.out.println("Failed to resolve parent '" + parentName + "' for struct '" + name + "'");
					continue;
				}
				if (isVirtual(parentStruct)) {
					hasVirtualParent = true;
					break;
				}
			}

			for (Declaration mb : struct.getDeclarations()) {
				if (Modifier.Virtual.isContainedBy(mb.getModifiers())) {
					hasVirtualMembers = true;
					break;
				}
			}
			bVirtual = hasVirtualMembers && !hasVirtualParent;
			structsVirtuality.put(name, bVirtual);
		}
		return bVirtual;
	}


	private Function createNewStructMethod(String name, Struct byRef) {
		TypeRef tr = typeRef(byRef.getTag().clone());
		Function f = new Function(Function.Type.JavaMethod, ident(name), tr);
		String varName = "s";

		f.addModifiers(Modifier.Protected);
		if (result.config.useJNAeratorUnionAndStructClasses) {
			f.setBody(block(
				//new Statement.Return(methodCall("setupClone", new Expression.New(tr.clone(), methodCall(null))))
					new Statement.Return(new Expression.New(tr.clone(), methodCall(null)))
			).setCompact(true));
		} else {
			f.setBody(block(
				stat(tr.clone(), varName, new Expression.New(tr.clone(), methodCall(null))),
				stat(methodCall(varRef(varName), MemberRefStyle.Dot, "useMemory", methodCall("getPointer"))),
				stat(methodCall("write")),
				stat(methodCall(varRef(varName), MemberRefStyle.Dot, "read")),
				new Statement.Return(varRef(varName))
			));
		}
		return f;
	}
	private Function createNewStructArrayMethod(Struct struct, boolean isUnion) {
		if (!result.config.useJNAeratorUnionAndStructClasses)
			return null;

		TypeRef tr = typeRef(struct.getTag().clone());
		TypeRef ar = new TypeRef.ArrayRef(tr);
		String varName = "arrayLength";
		Function f = new Function(Function.Type.JavaMethod, ident("newArray"), ar, new Arg(varName, typeRef(Integer.TYPE)));
		
		f.addModifiers(Modifier.Public, Modifier.Static);
		f.setBody(block(
			new Statement.Return(
				methodCall(
					expr(typeRef(isUnion ? com.ochafik.lang.jnaerator.runtime.Union.class : com.ochafik.lang.jnaerator.runtime.Structure.class)),
					MemberRefStyle.Dot,
					"newArray",
					classLiteral(tr),
					varRef(varName)
				)
			)
		));
		return f;
	}

	public void convertStructs(List<Struct> structs, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) throws IOException {
		if (structs != null) {
			for (Struct struct : structs) {
				if (struct.findParentOfType(Struct.class) != null)
					continue;
					
				outputConvertedStruct(struct, signatures, out, libraryClassName, false);
			}
		}
	}

	public VariablesDeclaration convertVariablesDeclaration(String name, TypeRef mutatedType, int[] iChild, Identifier callerLibraryName, Element... toImportDetailsFrom) throws UnsupportedConversionException {
		name = result.typeConverter.getValidJavaArgumentName(ident(name)).toString();
		//convertVariablesDeclaration(name, mutatedType, out, iChild, callerLibraryName);

		Expression initVal = null;
		TypeRef  javaType = result.typeConverter.convertTypeToJNA(
			mutatedType, 
			TypeConversion.TypeConversionMode.FieldType,
			callerLibraryName
		);
		mutatedType = result.typeConverter.resolveTypeDef(mutatedType, callerLibraryName, true);
		
		VariablesDeclaration convDecl = new VariablesDeclaration();
		convDecl.addModifiers(Modifier.Public);
		
		if (javaType instanceof ArrayRef && mutatedType instanceof ArrayRef) {
			ArrayRef mr = (ArrayRef)mutatedType;
			ArrayRef jr = (ArrayRef)javaType;
			Expression mul = null;
			List<Expression> dims = mr.flattenDimensions();
			for (int i = dims.size(); i-- != 0;) {
				Expression x = dims.get(i);
			
				if (x == null || x instanceof EmptyArraySize) {
					javaType = jr = new ArrayRef(typeRef(Pointer.class));
					break;
				} else {
					Pair<Expression, TypeRef> c = result.typeConverter.convertExpressionToJava(x, callerLibraryName, false);
					c.getFirst().setParenthesis(dims.size() == 1);
					if (mul == null)
						mul = c.getFirst();
					else
						mul = expr(c.getFirst(), BinaryOperator.Multiply, mul);
				}
			}
			initVal = new Expression.NewArray(jr.getTarget(), mul);
		}
		if (javaType == null) {
			throw new UnsupportedConversionException(mutatedType, "failed to convert type to Java");
		} else if (javaType.toString().equals("void")) {
			throw new UnsupportedConversionException(mutatedType, "void type !");
			//out.add(new EmptyDeclaration("SKIPPED:", v.formatComments("", true, true, false), v.toString()));
		} else {
			for (Element e : toImportDetailsFrom)
				convDecl.importDetails(e, false);
			convDecl.importDetails(mutatedType, true);
			convDecl.importDetails(javaType, true);
			
//			convDecl.importDetails(v, false);
//			convDecl.importDetails(vs, false);
//			convDecl.importDetails(valueType, false);
//			valueType.stripDetails();
			convDecl.moveAllCommentsBefore();
			convDecl.setValueType(javaType);
			convDecl.addDeclarator(new DirectDeclarator(name, initVal));
			
			return convDecl;//out.addDeclaration(convDecl);
		}
	}
	public void convertVariablesDeclaration(VariablesDeclaration v, DeclarationsHolder out, int[] iChild, Identifier callerLibraryClass) {
		//List<Declaration> out = new ArrayList<Declaration>();
		try {
			TypeRef valueType = v.getValueType();
			for (Declarator vs : v.getDeclarators()) {
				String name = vs.resolveName();
				if (name == null || name.length() == 0)
					continue;
	
				TypeRef mutatedType = valueType;
				if (!(vs instanceof DirectDeclarator))
				{
					mutatedType = (TypeRef)vs.mutateType(valueType);
					vs = new DirectDeclarator(vs.resolveName());
				}
				VariablesDeclaration vd = convertVariablesDeclaration(name, mutatedType, iChild, callerLibraryClass, v, vs);
				if (vd != null) {
					Declarator d = v.getDeclarators().get(0);
					if (d.getBits() > 0) {
						int bits = d.getBits();
						vd.addAnnotation(new Annotation(Bits.class, "(" + bits + ")"));
						String st = vd.getValueType().toString(), mst = st;
						if (st.equals("int") || st.equals("long") || st.equals("short") || st.equals("long")) {
							if (bits <= 8)
								mst = "byte";
							else if (bits <= 16)
								mst = "short";
							else if (bits <= 32)
								mst = "int";
							else
								mst = "long"; // should not happen
						}
						if (!st.equals(mst))
							vd.setValueType(new Primitive(mst));
					}
					if (!(mutatedType instanceof Primitive) && !result.config.noComments)
						vd.addToCommentBefore("C type : " + mutatedType);
					out.addDeclaration(vd);
				}
				iChild[0]++;
			}
		} catch (UnsupportedConversionException e) {
			if (!result.config.limitComments)
				out.addDeclaration(new EmptyDeclaration(e.toString()));
		}
	}
	TaggedTypeRefDeclaration publicStaticClassDecl(Identifier name, Identifier parentName, Struct.Type type, Element toCloneCommentsFrom, Identifier... interfaces) {
		return decl(publicStaticClass(name, parentName, type, toCloneCommentsFrom, interfaces));
	}
	Struct publicStaticClass(Identifier name, Identifier parentName, Struct.Type type, Element toCloneCommentsFrom, Identifier... interfaces) {
		Struct cl = new Struct();
		cl.setType(type);
		cl.setTag(name);
		if (parentName != null)
			cl.setParents(parentName);
		if (type == Struct.Type.JavaInterface)
			for (Identifier inter : interfaces)
				cl.addParent(inter);
		else
			cl.setProtocols(Arrays.asList(interfaces));
		
		if (!result.config.noComments)
			if (toCloneCommentsFrom != null ) {
				cl.importDetails(toCloneCommentsFrom, false);
				cl.moveAllCommentsBefore();
				cl.addToCommentBefore(getFileCommentContent(toCloneCommentsFrom));
			}
		cl.addModifiers(Modifier.Public, Modifier.Static);
		return cl;
	}
	public Pair<List<VariablesDeclaration>, List<VariablesDeclaration>> getParentAndOwnDeclarations(Struct structJavaClass, Struct nativeStruct) throws IOException {
		Pair<List<VariablesDeclaration>, List<VariablesDeclaration>> ret = 
			new Pair<List<VariablesDeclaration>, List<VariablesDeclaration>>(
				new ArrayList<VariablesDeclaration>(), 
				new ArrayList<VariablesDeclaration>()
			)
		;
		if (!nativeStruct.getParents().isEmpty()) {
			for (Identifier parentName : nativeStruct.getParents()) {
				Struct parent = result.structsByName.get(parentName);
				if (parent == null) {
					// TODO report error
					continue;
				}
				Struct parentJavaClass = convertStruct(parent, new Signatures(), null, true);
				Pair<List<VariablesDeclaration>, List<VariablesDeclaration>> parentDecls = getParentAndOwnDeclarations(parentJavaClass, parent);
				ret.getFirst().addAll(parentDecls.getFirst());
				ret.getFirst().addAll(parentDecls.getSecond());
			}
		}
		for (Declaration d : structJavaClass.getDeclarations()) {
			if (!(d instanceof VariablesDeclaration))
				continue;
			VariablesDeclaration vd = (VariablesDeclaration)d;
			if (vd.getDeclarators().size() != 1)
				continue; // should not happen !
			if (!isField(vd))
				continue;
			
			ret.getSecond().add(vd);
		}
				
		return ret;
	}
	@SuppressWarnings("unchecked")
	private void addStructConstructors(Identifier structName, Struct structJavaClass/*, Struct byRef,
			Struct byVal*/, Struct nativeStruct) throws IOException {
		
		List<Declaration> initialMembers = new ArrayList<Declaration>(structJavaClass.getDeclarations());
		Set<String> signatures = new TreeSet<String>();
		
		Function emptyConstructor = new Function(Function.Type.JavaMethod, structName.clone(), null).addModifiers(Modifier.Public);
		emptyConstructor.setBody(block(stat(methodCall("super"))));
		addConstructor(structJavaClass, emptyConstructor);
		
		
		boolean isUnion = nativeStruct.getType() == Struct.Type.CUnion;
		if (isUnion) {
			Map<String, Pair<TypeRef, List<Pair<String, String>>>> fieldsAndCommentsByTypeStr = new HashMap<String, Pair<TypeRef, List<Pair<String, String>>>>();
			for (Declaration d : initialMembers) {
				if (!(d instanceof VariablesDeclaration))
					continue;
					
				VariablesDeclaration vd = (VariablesDeclaration)d;
				if (vd.getDeclarators().size() != 1)
					continue; // should not happen !
				String name = vd.getDeclarators().get(0).resolveName();
				TypeRef tr = vd.getValueType();
				if (!isField(vd))
					continue;
				
				String trStr = tr.toString();
				Pair<TypeRef, List<Pair<String, String>>> pair = fieldsAndCommentsByTypeStr.get(trStr);
				if (pair == null)
					fieldsAndCommentsByTypeStr.put(trStr, pair = new Pair<TypeRef, List<Pair<String, String>>>(tr, new ArrayList<Pair<String, String>>()));
				
				pair.getSecond().add(new Pair<String, String>(vd.getCommentBefore(), name));
			}
			for (Pair<TypeRef, List<Pair<String, String>>> pair : fieldsAndCommentsByTypeStr.values()) {
				List<String> commentBits = new ArrayList<String>(), nameBits = new ArrayList<String>();
				for (Pair<String, String> p : pair.getValue()) {
					if (p.getFirst() != null)
						commentBits.add(p.getFirst());
					nameBits.add(p.getValue());
				}
				String name = StringUtils.implode(nameBits, "_or_");
				TypeRef tr = pair.getFirst();
				Function unionValConstr = new Function(Function.Type.JavaMethod, structName.clone(), null, new Arg(name, tr.clone()));
				if (!result.config.noComments)
					if (!commentBits.isEmpty())
						unionValConstr.addToCommentBefore("@param " + name + " " + StringUtils.implode(commentBits, ", or "));
				
				unionValConstr.addModifiers(Modifier.Public);
				
				Expression assignmentExpr = varRef(name);
				for (Pair<String, String> p : pair.getValue())
					assignmentExpr = new Expression.AssignmentOp(memberRef(varRef("this"), MemberRefStyle.Dot, ident(p.getValue())), AssignmentOperator.Equal, assignmentExpr);
				
				unionValConstr.setBody(block(
					stat(methodCall("super")),
					tr instanceof TypeRef.ArrayRef ? throwIfArraySizeDifferent(name) : null,
					stat(assignmentExpr),
					stat(methodCall("setType", result.typeConverter.getJavaClassLitteralExpression(tr)))
				));
				
				if (signatures.add(unionValConstr.computeSignature(false))) {
					structJavaClass.addDeclaration(unionValConstr);
//					byRef.addDeclaration(unionValConstr.clone().setName(byRef.getTag().clone()));
//					byVal.addDeclaration(unionValConstr.clone().setName(byVal.getTag().clone()));
				}
			}
		} else {
			Function fieldsConstr = new Function(Function.Type.JavaMethod, structName.clone(), null);
			fieldsConstr.setBody(new Block()).addModifiers(Modifier.Public);
			
			Pair<List<VariablesDeclaration>, List<VariablesDeclaration>> decls = getParentAndOwnDeclarations(structJavaClass, nativeStruct);
			Map<Integer, String> namesById = new TreeMap<Integer, String>();
			Set<String> names = new HashSet<String>();
			int iArg = 0;
			for (VariablesDeclaration vd : new CompoundCollection<VariablesDeclaration>(decls.getFirst(), decls.getSecond())) {
				String name = chooseJavaArgName(vd.getDeclarators().get(0).resolveName(), iArg, names);
				namesById.put(vd.getId(), name);
				fieldsConstr.addArg(new Arg(name, vd.getValueType().clone()));
				iArg++;
			}
			FunctionCall superCall = methodCall("super");
			for (VariablesDeclaration vd : decls.getFirst()) {
				String name = vd.getDeclarators().get(0).resolveName(), uname = namesById.get(vd.getId());
				Struct parent = (Struct)vd.getParentElement();
				Identifier parentTgName = result.getTaggedTypeIdentifierInJava(parent);
				if (!result.config.noComments)
					fieldsConstr.addToCommentBefore("@param " + name + " @see " + parentTgName + "#" + vd.getDeclarators().get(0).resolveName());
				superCall.addArgument(varRef(uname));
			}
			fieldsConstr.getBody().addStatement(stat(superCall));
			
			for (VariablesDeclaration vd : decls.getSecond()) {
				String name = vd.getDeclarators().get(0).resolveName(), uname = namesById.get(vd.getId());
				if (!result.config.noComments)
					if (vd.getCommentBefore() != null)
						fieldsConstr.addToCommentBefore("@param " + uname + " " + vd.getCommentBefore());
				if (vd.getValueType() instanceof TypeRef.ArrayRef)
					fieldsConstr.getBody().addStatement(throwIfArraySizeDifferent(uname));
				fieldsConstr.getBody().addStatement(stat(
						new Expression.AssignmentOp(memberRef(varRef("this"), MemberRefStyle.Dot, ident(name)), AssignmentOperator.Equal, varRef(uname))));
			}
			int nArgs = fieldsConstr.getArgs().size();
			if (nArgs == 0)
				System.err.println("Struct with no field : " + structName);
			
			if (nArgs > 0 && nArgs < result.config.maxConstructedFields) {
				if (signatures.add(fieldsConstr.computeSignature(false))) {
					structJavaClass.addDeclaration(fieldsConstr);
				}
			}
		}
		
//		Function pointerConstructor = new Function(Function.Type.JavaMethod, structName.clone(), null, 
//			new Arg("pointer", new TypeRef.SimpleTypeRef(Pointer.class.getName())),
//			new Arg("offset", new TypeRef.Primitive("int"))
//		).addModifiers(Modifier.Public).setBody(block(
//			stat(methodCall("super", varRef("pointer"), varRef("offset")))
//		).setCompact(true));
//		pointerConstructor.setCommentBefore("Cast data at given memory location (pointer + offset) as an existing " + structName + " struct");
//		pointerConstructor.setBody(block(
//			stat(methodCall("super")),
//			stat(methodCall("useMemory", varRef("pointer"), varRef("offset"))),
//			stat(methodCall("read"))
//		));
//		boolean addedPointerConstructor = false;
//		if (signatures.add(pointerConstructor.computeSignature(false))) {
//			addConstructor(structJavaClass, pointerConstructor);
//			addedPointerConstructor = true;
//		}
		
//		String copyArgName = isUnion ? "otherUnion" : "otherStruct";
//		Function shareMemConstructor = new Function(Function.Type.JavaMethod, structName.clone(), null, 
//			new Arg(copyArgName, new TypeRef.SimpleTypeRef(structName.clone()))
//		).addModifiers(Modifier.Public);
		
//		Block useCopyMem = //addedPointerConstructor ? 
//			//null : 
//			block(
//					stat(methodCall("super")),
//					stat(methodCall("useMemory", methodCall(varRef(copyArgName), MemberRefStyle.Dot, "getPointer"), expr(0))),
//					stat(methodCall("read"))
//			)
//		;
//		shareMemConstructor.setBody(//addedPointerConstructor ? 
////			block(
////					stat(methodCall("super", methodCall(varRef(copyArgName), MemberRefStyle.Dot, "getPointer"), expr(0)))
////			).setCompact(true) :
//			useCopyMem
//		);
//		shareMemConstructor.setCommentBefore("Create an instance that shares its memory with another " + structName + " instance");
//		if (signatures.add(shareMemConstructor.computeSignature(false))) {
////			addConstructor(byRef, shareMemConstructor);
////			shareMemConstructor = shareMemConstructor.clone();
////			addConstructor(byVal, shareMemConstructor);
//		
//			shareMemConstructor = shareMemConstructor.clone();
//			shareMemConstructor.setBody(/*addedPointerConstructor ?
//				block(
//					stat(methodCall("this", methodCall(varRef(copyArgName), MemberRefStyle.Dot, "getPointer"), expr(0)))
//				).setCompact(true) :*/
//				useCopyMem.clone()
//			);
//			addConstructor(structJavaClass, shareMemConstructor);
//		}
	}
	private boolean isField(VariablesDeclaration vd) {
		List<Modifier> mods = vd.getModifiers();
		if (Modifier.Final.isContainedBy(mods))
			return false;
		if (vd.getValueType() == null || vd.getValueType().toString().equals(VirtualTablePointer.class.getName()))
			return false;
		return true;
	}

	Statement throwIfArraySizeDifferent(String varAndFieldName) {
		return new Statement.If(
			expr(
				memberRef(varRef(varAndFieldName), MemberRefStyle.Dot, "length"), 
				BinaryOperator.IsDifferent,
				memberRef(memberRef(varRef("this"), MemberRefStyle.Dot, varAndFieldName), MemberRefStyle.Dot, "length")
			),
			new Statement.Throw(new Expression.New(typeRef(IllegalArgumentException.class), expr("Wrong array size !"))),
			null
		);
	}
	void addConstructor(Struct s, Function f) {
		Identifier structName = getActualTaggedTypeName(s);
		
		f.setName(structName);
		s.addDeclaration(f);
	}
	
	String getFileCommentContent(File file, Element e) {
		if (file != null) {
			String path = result.config.relativizeFileForSourceComments(file.getAbsolutePath());
			String inCategoryStr = "";
			if (e instanceof Function) {
				Function fc = (Function)e;
				Struct parent;
				if (fc.getType() == Type.ObjCMethod && ((parent = as(fc.getParentElement(), Struct.class)) != null) && (parent.getCategoryName() != null)) {
					inCategoryStr = "from " + parent.getCategoryName() + " ";
				}
			}
			return "<i>" + inCategoryStr + "native declaration : " + path + (e == null || e.getElementLine() < 0 ? "" : ":" + e.getElementLine()) + "</i>";
		} else if (e != null && e.getElementLine() >= 0) {
			return "<i>native declaration : <input>:" + e.getElementLine() + "</i>";
		}
		return null;
	}
	String getFileCommentContent(Element e) {
		if (result.config.limitComments)
			return null;
		
		String f = Element.getFileOfAscendency(e);
		if (f == null && e != null && e.getElementLine() >= 0)
			return "<i>native declaration : line " + e.getElementLine() + "</i>";
		
		return f == null ? null : getFileCommentContent(new File(f), e);
	}
	

	public List<Define> reorderDefines(Collection<Define> defines) {
		List<Define> reordered = new ArrayList<Define>(defines.size());
		HashSet<Identifier> added = new HashSet<Identifier>(), all = new HashSet<Identifier>();
		
		
		Map<String, Pair<Define, Set<Identifier>>> pending = new HashMap<String, Pair<Define, Set<Identifier>>>();
		for (Define define : defines) {
			Set<Identifier> dependencies = new TreeSet<Identifier>();
			computeVariablesDependencies(define.getValue(), dependencies);
			all.add(ident(define.getName()));
			if (dependencies.isEmpty()) {
				reordered.add(define);
				added.add(ident(define.getName()));
			} else {
				pending.put(define.getName(), new Pair<Define, Set<Identifier>>(define, dependencies));
			}	
		}
		
		for (int i = 3; i-- != 0 && !pending.isEmpty();) {
			for (Iterator<Map.Entry<String, Pair<Define, Set<Identifier>>>> it = pending.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Pair<Define, Set<Identifier>>> e = it.next(); 
				Set<Identifier> dependencies = e.getValue().getSecond();
				String name = e.getKey();
				boolean missesDep = false;
				for (Identifier dependency : dependencies) {
					if (!added.contains(dependency)) {
						missesDep = true;
						if (!all.contains(dependency)) {
							it.remove();
							all.remove(name);
						}
						
						break;
					}
				}
				if (missesDep)
					continue;
				
				it.remove();
				reordered.add(e.getValue().getFirst());
			}
		}
		
		return reordered;
	}
	public void computeVariablesDependencies(Element e, final Set<Identifier> names) {
		e.accept(new Scanner() {

			@Override
			public void visitVariableRef(VariableRef variableRef) {
				names.add(variableRef.getName());
			}
		});
	}
	
	private String chooseJavaArgName(String name, int iArg, Set<String> names) {
		Identifier jan = result.typeConverter.getValidJavaArgumentName(ident(name));
		String baseArgName = jan == null ? null : jan.toString();
		int i = 1;
		if (baseArgName == null)
			baseArgName = "arg";
		
		String argName;
		do {
			argName = baseArgName + (i == 1 ? "" : i + "");
			i++;
		} while (names.contains(argName) || TypeConversion.isJavaKeyword(argName));
		names.add(argName);
		return argName;
	}

}