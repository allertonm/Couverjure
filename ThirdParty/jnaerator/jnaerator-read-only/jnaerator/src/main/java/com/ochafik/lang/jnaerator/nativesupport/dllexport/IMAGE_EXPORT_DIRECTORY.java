package com.ochafik.lang.jnaerator.nativesupport.dllexport;
/**
 * <i>native declaration : C:\Prog\jnaerator\sources\com\ochafik\lang\jnaerator\nativesupport\dllexport.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class IMAGE_EXPORT_DIRECTORY extends com.ochafik.lang.jnaerator.runtime.Structure<IMAGE_EXPORT_DIRECTORY, IMAGE_EXPORT_DIRECTORY.ByValue, IMAGE_EXPORT_DIRECTORY.ByReference> {
	public int Characteristics;
	public int TimeDateStamp;
	public short MajorVersion;
	public short MinorVersion;
	public int Name;
	public int Base;
	public int NumberOfFunctions;
	public int NumberOfNames;
	public com.sun.jna.ptr.PointerByReference AddressOfFunctions;
	public com.sun.jna.ptr.PointerByReference AddressOfNames;
	public com.sun.jna.ptr.PointerByReference AddressOfNameOrdinals;
	public IMAGE_EXPORT_DIRECTORY() {
		super();
	}
	protected ByReference newByReference() { return new ByReference(); }
	protected ByValue newByValue() { return new ByValue(); }
	protected IMAGE_EXPORT_DIRECTORY newInstance() { return new IMAGE_EXPORT_DIRECTORY(); }
	public static class ByReference extends IMAGE_EXPORT_DIRECTORY implements com.sun.jna.Structure.ByReference {}
	public static class ByValue extends IMAGE_EXPORT_DIRECTORY implements com.sun.jna.Structure.ByValue {}
}
