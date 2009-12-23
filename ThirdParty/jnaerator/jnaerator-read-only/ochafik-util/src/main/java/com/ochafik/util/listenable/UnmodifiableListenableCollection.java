/*
   Copyright 2008 Olivier Chafik

   Licensed under the Apache License, Version 2.0 (the License);
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an AS IS BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   This file comes from the Jalico project (Java Listenable Collections)

       http://jalico.googlecode.com/.
*/
package com.ochafik.util.listenable;

import java.util.Collection;
import java.util.Iterator;

class UnmodifiableListenableCollection<T> extends FilteredListenableCollection<T> {
	public UnmodifiableListenableCollection(ListenableCollection<T> listenableCollection) {
		super(listenableCollection);
	}
	@Override
	public boolean add(T o) {
		throw new UnsupportedOperationException("Unmodifiable listenable collection !");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Unmodifiable listenable collection !");
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable listenable collection !");		
	}

	@Override
	public Iterator<T> iterator() {
		return new FilteredIterator<T>(listenableCollection.iterator()) {
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Unmodifiable listenable collection !");
			}
		};
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Unmodifiable listenable collection !");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable listenable collection !");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable listenable collection !");
	}	
}