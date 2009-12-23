/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jun 19, 2006 */

package clojure.lang;

import java.lang.reflect.Array;

public class ArraySeq extends ASeq implements IndexedSeq, IReduce{
final Object array;
final int i;
final Object[] oa;
//ISeq _rest;

static public ArraySeq create(){
	return null;
}

static public ArraySeq create(Object... array){
	if(array == null || array.length == 0)
		return null;
	return new ArraySeq(array, 0);
}

static ISeq createFromObject(Object array){
	if(array == null || Array.getLength(array) == 0)
		return null;
	Class aclass = array.getClass();
	if(aclass == int[].class)
		return new ArraySeq_int(null, (int[]) array, 0);
	if(aclass == float[].class)
		return new ArraySeq_float(null, (float[]) array, 0);
	if(aclass == double[].class)
		return new ArraySeq_double(null, (double[]) array, 0);
	if(aclass == long[].class)
		return new ArraySeq_long(null, (long[]) array, 0);
	return new ArraySeq(array, 0);
}

ArraySeq(Object array, int i){
	this.array = array;
	this.i = i;
	this.oa = (Object[]) (array instanceof Object[] ? array : null);
//    this._rest = this;
}

ArraySeq(IPersistentMap meta, Object array, int i){
	super(meta);
	this.array = array;
	this.i = i;
	this.oa = (Object[]) (array instanceof Object[] ? array : null);
}

public Object first(){
	if(oa != null)
		return oa[i];
	return Reflector.prepRet(Array.get(array, i));
}

public ISeq next(){
	if(oa != null)
		{
		if(i + 1 < oa.length)
			return new ArraySeq(array, i + 1);
		}
	else
		{
		if(i + 1 < Array.getLength(array))
			return new ArraySeq(array, i + 1);
		}
	return null;
}

public int count(){
	if(oa != null)
		return oa.length - i;
	return Array.getLength(array) - i;
}

public int index(){
	return i;
}

public ArraySeq withMeta(IPersistentMap meta){
	return new ArraySeq(meta, array, i);
}

public Object reduce(IFn f) throws Exception{
	if(oa != null)
		{
		Object ret = oa[i];
		for(int x = i + 1; x < oa.length; x++)
			ret = f.invoke(ret, oa[x]);
		return ret;
		}

	Object ret = Reflector.prepRet(Array.get(array, i));
	for(int x = i + 1; x < Array.getLength(array); x++)
		ret = f.invoke(ret, Reflector.prepRet(Array.get(array, x)));
	return ret;
}

public Object reduce(IFn f, Object start) throws Exception{
	if(oa != null)
		{
		Object ret = f.invoke(start, oa[i]);
		for(int x = i + 1; x < oa.length; x++)
			ret = f.invoke(ret, oa[x]);
		return ret;
		}
	Object ret = f.invoke(start, Reflector.prepRet(Array.get(array, i)));
	for(int x = i + 1; x < Array.getLength(array); x++)
		ret = f.invoke(ret, Reflector.prepRet(Array.get(array, x)));
	return ret;
}

//////////////////////////////////// specialized primitive versions ///////////////////////////////

static public class ArraySeq_int extends ASeq implements IndexedSeq, IReduce{
	final int[] array;
	final int i;

	ArraySeq_int(IPersistentMap meta, int[] array, int i){
		super(meta);
		this.array = array;
		this.i = i;
	}

	public Object first(){
		return array[i];
	}

	public ISeq next(){
		if(i + 1 < array.length)
			return new ArraySeq_int(meta(), array, i + 1);
		return null;
	}

	public int count(){
		return array.length - i;
	}

	public int index(){
		return i;
	}

	public ArraySeq_int withMeta(IPersistentMap meta){
		return new ArraySeq_int(meta, array, i);
	}

	public Object reduce(IFn f) throws Exception{
		Object ret = array[i];
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}

	public Object reduce(IFn f, Object start) throws Exception{
		Object ret = f.invoke(start, array[i]);
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}
}


static public class ArraySeq_float extends ASeq implements IndexedSeq, IReduce{
	final float[] array;
	final int i;

	ArraySeq_float(IPersistentMap meta, float[] array, int i){
		super(meta);
		this.array = array;
		this.i = i;
	}

	public Object first(){
		return array[i];
	}

	public ISeq next(){
		if(i + 1 < array.length)
			return new ArraySeq_float(meta(), array, i + 1);
		return null;
	}

	public int count(){
		return array.length - i;
	}

	public int index(){
		return i;
	}

	public ArraySeq_float withMeta(IPersistentMap meta){
		return new ArraySeq_float(meta, array, i);
	}

	public Object reduce(IFn f) throws Exception{
		Object ret = array[i];
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}

	public Object reduce(IFn f, Object start) throws Exception{
		Object ret = f.invoke(start, array[i]);
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}
}

static public class ArraySeq_double extends ASeq implements IndexedSeq, IReduce{
	final double[] array;
	final int i;

	ArraySeq_double(IPersistentMap meta, double[] array, int i){
		super(meta);
		this.array = array;
		this.i = i;
	}

	public Object first(){
		return array[i];
	}

	public ISeq next(){
		if(i + 1 < array.length)
			return new ArraySeq_double(meta(), array, i + 1);
		return null;
	}

	public int count(){
		return array.length - i;
	}

	public int index(){
		return i;
	}

	public ArraySeq_double withMeta(IPersistentMap meta){
		return new ArraySeq_double(meta, array, i);
	}

	public Object reduce(IFn f) throws Exception{
		Object ret = array[i];
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}

	public Object reduce(IFn f, Object start) throws Exception{
		Object ret = f.invoke(start, array[i]);
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}
}

static public class ArraySeq_long extends ASeq implements IndexedSeq, IReduce{
	final long[] array;
	final int i;

	ArraySeq_long(IPersistentMap meta, long[] array, int i){
		super(meta);
		this.array = array;
		this.i = i;
	}

	public Object first(){
		return array[i];
	}

	public ISeq next(){
		if(i + 1 < array.length)
			return new ArraySeq_long(meta(), array, i + 1);
		return null;
	}

	public int count(){
		return array.length - i;
	}

	public int index(){
		return i;
	}

	public ArraySeq_long withMeta(IPersistentMap meta){
		return new ArraySeq_long(meta, array, i);
	}

	public Object reduce(IFn f) throws Exception{
		Object ret = array[i];
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}

	public Object reduce(IFn f, Object start) throws Exception{
		Object ret = f.invoke(start, array[i]);
		for(int x = i + 1; x < array.length; x++)
			ret = f.invoke(ret, array[x]);
		return ret;
	}
}

}
