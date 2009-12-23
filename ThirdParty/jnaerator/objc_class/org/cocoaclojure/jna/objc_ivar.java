package org.cocoaclojure.jna;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class objc_ivar extends com.ochafik.lang.jnaerator.runtime.Structure<objc_ivar, objc_ivar.ByValue, objc_ivar.ByReference> {
	public com.sun.jna.Pointer ivar_name;
	public com.sun.jna.Pointer ivar_type;
	public int ivar_offset;
	public objc_ivar() {
		super();
	}
	public objc_ivar(com.sun.jna.Pointer ivar_name, com.sun.jna.Pointer ivar_type, int ivar_offset) {
		super();
		this.ivar_name = ivar_name;
		this.ivar_type = ivar_type;
		this.ivar_offset = ivar_offset;
	}
	protected ByReference newByReference() { return new ByReference(); }
	protected ByValue newByValue() { return new ByValue(); }
	protected objc_ivar newInstance() { return new objc_ivar(); }
	public static objc_ivar[] newArray(int arrayLength) {
		return com.ochafik.lang.jnaerator.runtime.Structure.newArray(objc_ivar.class, arrayLength);
	}
	public static class ByReference extends objc_ivar implements com.sun.jna.Structure.ByReference {}
	public static class ByValue extends objc_ivar implements com.sun.jna.Structure.ByValue {}
}
