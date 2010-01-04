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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ochafik.lang.jnaerator.JNAeratorConfigUtils.FileExtensionFilter;
import com.ochafik.lang.jnaerator.cplusplus.CPlusPlusMangler;
import com.ochafik.lang.jnaerator.parser.Element;
import com.ochafik.lang.jnaerator.parser.Function;
import com.ochafik.util.CompoundCollection;
import com.ochafik.util.listenable.Adapter;
import com.ochafik.util.listenable.Filter;
import com.ochafik.util.listenable.Pair;
import java.text.MessageFormat;
import java.util.HashSet;

public class JNAeratorConfig {
	
	public enum Compiler {
		GCC4, MSVC9
	}
	public enum Architecture {
		x86_64, i386, PowerPC
	}
	public enum Platform {
		Windows, Linux, MacOSX
	}
	public enum GenFeatures {
		Compile,
		FileComments,
		UsageComments,
		EnumTypeLocationComments,
		LibrariesAutoExtraction,
		CPlusPlusMangling,
		StructConstructors, 
		TypedPointersForForwardDeclarations, 
		OriginalFunctionSignatures, 
		FunctionArgsJavaDoc
	}
	public static final String DEFAULT_HEADER_EXTENSIONS = "h:hpp:hxx";
	public static final String DEFAULT_IMPLEMS_EXTENSIONS = "cpp:c:cxx:m:mm";
	
	public final EnumSet<GenFeatures> features = EnumSet.allOf(GenFeatures.class);
	public final List<CPlusPlusMangler> cPlusPlusManglers = new ArrayList<CPlusPlusMangler>();
	
	public static class PreprocessorConfig {

		public boolean WORKAROUND_PP_BUGS = true;

		public final List<String> includes = new ArrayList<String>();
		public final Map<String, String> macros = new LinkedHashMap<String, String>();
		public final List<String> frameworksPath = new ArrayList<String>();

		public List<String> includeStrings = new ArrayList<String>();

		public boolean preprocess = true;
	}
	
	public final JNAeratorConfig.PreprocessorConfig preprocessorConfig = new JNAeratorConfig.PreprocessorConfig();
	public boolean followIncludes = false;
	public File preprocessingOutFile, macrosOutFile;
	public File choicesOutFile, choicesInputFile;
	public boolean useJNADirectCalls = false;
	public boolean limitComments = false, noComments = false;
	public boolean putTopStructsInSeparateFiles = true;
	public boolean bundleRuntime = true;
	public boolean extractLibSymbols = false;
    public List<Pair<MessageFormat, MessageFormat>> onlineDocumentationURLFormats = new ArrayList<Pair<MessageFormat, MessageFormat>>();
	public String entryName;
	public int maxConstructedFields = 10;
	
	public Map<String, String> extraJavaSourceFilesContents = new LinkedHashMap<String, String>();
	public Set<String> frameworks = new LinkedHashSet<String>();
	boolean skipIncludedFrameworks = false;
	public FileFilter fileFilter = new FileExtensionFilter(DEFAULT_HEADER_EXTENSIONS.split("[:;]"));
	
	public Map<String, List<File>> libraryFilesByArch = new LinkedHashMap<String, List<File>>();
	public List<File> libraryFiles = new ArrayList<File>();
	
	public Map<File, String> libraryByFile = new LinkedHashMap<File, String>();
	public void addLibraryFile(File file, String arch) {
		
		List<File> others = libraryFilesByArch.get(arch);
		if (others == null)
			libraryFilesByArch.put(arch, others = new ArrayList<File>());
		
		String fn = file.getName();
		int i = fn.lastIndexOf('.');
		if (i > 0)
			fn = fn.substring(0, i);
		
		others.add(file);
		libraryByFile.put(file, fn);
		libraryFiles.add(file);
	}
	public void addSourceFile(File file, String library, boolean applyFilters) throws IOException {
		if (file.isFile()) {
			if (fileFilter == null || !applyFilters || fileFilter.accept(file)) {
				file = file.getCanonicalFile();
                if (library == null && fileToLibrary != null)
                    library = fileToLibrary.adapt(file);
				libraryByFile.put(file, library);
				sourceFiles.add(file);
			}
		} else {
			File[] fs = file.listFiles();
			if (fs != null) {
				for (File f : fs) {
					addSourceFile(f, library, true);
				}
			}
		}
	}

	public JNAeratorConfig() {
//		if (System.getenv("POINTER_CLASSES") == null)
//			features.remove(GenFeatures.TypedPointersForForwardDeclarations);
	}
	public boolean verbose = false;
	public File outputDir;
	public List<String> rootDirectoriesPrefixesForSourceComments = new ArrayList<String>();
	public Adapter<Function, Boolean> functionsAccepter;
	public String packageName = null, rootPackageName = null;
	public String defaultLibrary;
	public Map<String, File> libraryProjectSources = new LinkedHashMap<String, File>();
	public Adapter<File, String> fileToLibrary = new Adapter<File, String>() {
		public String adapt(File value) {
			String libraryFile = null;
			try {
				//String canoFile = value.getCanonicalPath();
				//libraryFile = libraryByFile.get(canoFile);
				libraryFile = libraryByFile.get(value.getCanonicalFile());
				//if (value.toString().startsWith("\""))
				//	new Exception("Double quotes in file !").printStackTrace();
//				if (!canoFile.contains("Program Files")) {
//					System.out.println("libraryByFile = " + libraryByFile);
//					System.out.println("libraryByFile(" + canoFile + ") = " + libraryFile);
//					System.out.println("    value = " + value);
//					System.out.println("can value = " + value.getCanonicalFile());
//					System.out.println("abs value = " + value.getAbsoluteFile());
//				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return libraryFile == null ? defaultLibrary : libraryFile;
		}
	};
	public void addRootDir(File dir) throws IOException {
		if (!dir.exists())
			return;
		String str = dir.getCanonicalPath();
		if (!str.endsWith(File.separator))
			str += File.separator;
		if (!rootDirectoriesPrefixesForSourceComments.contains(str))
			rootDirectoriesPrefixesForSourceComments.add(str);
	}
	public Filter<Element> symbolsAccepter = new Filter<Element>() {
		public boolean accept(Element value) {
			String s = Element.getFileOfAscendency(value);
			if (s == null)
				return false;
			
			File f = new File(s);
			try {
				f = f.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return libraryByFile.containsKey(f);
		}
	};
	
	public String libraryForElementsInNullFile;
	public String cPlusPlusNameSpaceSeparator = "_";
	public boolean useJNAeratorUnionAndStructClasses = true;
	public boolean preferJavac = false;
	public Set<File> bridgeSupportFiles = new LinkedHashSet<File>();
	public File outputJar;
	public File cacheDir;
	public boolean autoConf = true;
    public Set<String> undefines = new HashSet<String>();
	public boolean gccLong = false, sizeAsLong = false;
	public boolean compile = true;
	public boolean bundleSources = true;
	public boolean noCPlusPlus;
	
	public String getLibrary(String elementFile) {
		if (elementFile == null)
			return libraryForElementsInNullFile;
		
		return fileToLibrary == null ? 
				defaultLibrary : 
				fileToLibrary.adapt(new File(elementFile));
	}

	Set<File> sourceFiles = new LinkedHashSet<File>();
	public boolean bundleLibraries = true;
	public boolean wcharAsShort = false;
	public boolean genCPlusPlus = false;
	public File extractedSymbolsOut;
	public boolean stringifyConstCStringReturnValues = true;
	public File bridgesupportOutFile;
	public boolean noMangling;
	public boolean noPrimitiveArrays;
	public File scalaOut;
	public Collection<File> getFiles() {
		/*return new AdaptedCollection<String, File>(libraryByFile.keySet(), new Adapter<String, File>() {
			@Override
			public File adapt(String value) {
				return new File(value);
			}
		});*/
		return sourceFiles;//libraryByFile.keySet();
	}

	public String relativizeFileForSourceComments(String path) {
		if (path == null)
			return null;
		
		for (String pref : rootDirectoriesPrefixesForSourceComments) {
			if (path.startsWith(pref)) {
				path = path.substring(pref.length());
				break;
			}
		}
		return path;
	}
	@SuppressWarnings("unchecked")
	public Collection<File> getInputFiles() {
		return new CompoundCollection<File>(sourceFiles, bridgeSupportFiles, libraryFiles);
	}
}