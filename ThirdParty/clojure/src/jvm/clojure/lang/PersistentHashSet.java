/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 3, 2008 */

package clojure.lang;

import java.util.List;

public class PersistentHashSet extends APersistentSet{

static public final PersistentHashSet EMPTY = new PersistentHashSet(null, PersistentHashMap.EMPTY);

public static PersistentHashSet create(Object... init){
	PersistentHashSet ret = EMPTY;
	for(int i = 0; i < init.length; i++)
		{
		ret = (PersistentHashSet) ret.cons(init[i]);
		}
	return ret;
}

public static PersistentHashSet create(List init){
	PersistentHashSet ret = EMPTY;
	for(Object key : init)
		{
		ret = (PersistentHashSet) ret.cons(key);
		}
	return ret;
}

static public PersistentHashSet create(ISeq items){
	PersistentHashSet ret = EMPTY;
	for(; items != null; items = items.next())
		{
		ret = (PersistentHashSet) ret.cons(items.first());
		}
	return ret;
}

PersistentHashSet(IPersistentMap meta, IPersistentMap impl){
	super(meta, impl);
}

public IPersistentSet disjoin(Object key) throws Exception{
	if(contains(key))
		return new PersistentHashSet(meta(),impl.without(key));
	return this;
}

public IPersistentSet cons(Object o){
	if(contains(o))
		return this;
	return new PersistentHashSet(meta(),impl.assoc(o,o));
}

public IPersistentCollection empty(){
	return EMPTY.withMeta(meta());	
}

public PersistentHashSet withMeta(IPersistentMap meta){
	return new PersistentHashSet(meta, impl);
}

}
