/*
   Copyright 2014 Simon Schmidt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package epoxide.lpa.impl.rdbms;

import java.sql.ResultSet;
import java.sql.SQLException;

import epoxide.lpa.IterableIterator;

public class RdbmsIterator<T> implements IterableIterator<T> {
	private Class<T> cls;
	private ClassConverter conv;
	private ResultSet results;
	private boolean isNext;
	
	public RdbmsIterator(Class<T> cls, ClassConverter conv, ResultSet results) throws SQLException {
		super();
		this.cls = cls;
		this.conv = conv;
		this.results = results;
		this.isNext = results.next();
	}

	@Override
	public boolean hasNext() {
		return isNext;
	}

	@Override
	public T next() {
		if(isNext)
		try {
			Object obj = conv.decode(results);
			isNext = results.next();
			return cls.cast(obj);
		} catch (SQLException e) {
		}
		return null;
	}

	@Override
	public void remove() {}

	@Override
	public IterableIterator<T> iterator() { return this; }

	@Override
	public void close() {
		try {
			results.close();
		} catch (SQLException e) {
		}
	}
}
