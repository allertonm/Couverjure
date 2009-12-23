package org.cocoaclojure.jna;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class objc_symtab extends com.ochafik.lang.jnaerator.runtime.Structure<objc_symtab, objc_symtab.ByValue, objc_symtab.ByReference> {
	public com.sun.jna.NativeLong sel_ref_cnt;
	public org.rococoa.Selector refs;
	public short cls_def_cnt;
	public short cat_def_cnt;
	/// variable size
	public com.sun.jna.Pointer[] defs = new com.sun.jna.Pointer[(1)];
	public objc_symtab() {
		super();
	}
	public objc_symtab(com.sun.jna.NativeLong sel_ref_cnt, org.rococoa.Selector refs, short cls_def_cnt, short cat_def_cnt, com.sun.jna.Pointer defs[]) {
		super();
		this.sel_ref_cnt = sel_ref_cnt;
		this.refs = refs;
		this.cls_def_cnt = cls_def_cnt;
		this.cat_def_cnt = cat_def_cnt;
		if (defs.length != this.defs.length) 
			throw new java.lang.IllegalArgumentException("Wrong array size !");
		this.defs = defs;
	}
	protected ByReference newByReference() { return new ByReference(); }
	protected ByValue newByValue() { return new ByValue(); }
	protected objc_symtab newInstance() { return new objc_symtab(); }
	public static objc_symtab[] newArray(int arrayLength) {
		return com.ochafik.lang.jnaerator.runtime.Structure.newArray(objc_symtab.class, arrayLength);
	}
	public static class ByReference extends objc_symtab implements com.sun.jna.Structure.ByReference {}
	public static class ByValue extends objc_symtab implements com.sun.jna.Structure.ByValue {}
}
