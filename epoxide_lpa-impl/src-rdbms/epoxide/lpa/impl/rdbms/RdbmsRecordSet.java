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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import epoxide.lpa.IterableIterator;
import epoxide.lpa.RecordSet;
import epoxide.lpa.impl.Empty;

public class RdbmsRecordSet<T> implements RecordSet<T> {
	private Class<T> cls;
	private ClassConverter conv;
	@SuppressWarnings("unused")
	private Connection conn;
	@SuppressWarnings("unused")
	private SqlDialect dialect;
	private PreparedStatement stdlookup;
	private PreparedStatement stdquery;
	private PreparedStatement stdinsert;
	private PreparedStatement stdupdate;
	private PreparedStatement stddelete;
	public RdbmsRecordSet(Class<T> cls, RdbmsClassTable clstab) {
		this.cls = cls;
		conv = clstab.conv;
		conn = clstab.conn;
		dialect = clstab.dialect;
		stdlookup = clstab.stdlookup;
		stdquery = clstab.stdquery;
		stdinsert = clstab.stdinsert;
		stdupdate = clstab.stdupdate;
		stddelete = clstab.stddelete;
	}

	@Override
	public synchronized T getEntity(Object id) {
		try {
			conv.setId(stdlookup, id);
			ResultSet rs = stdlookup.executeQuery();
			try{
				if(rs.next())
					return cls.cast(conv.decode(rs));
			}finally{
				rs.close();
			}
		} catch (SQLException e) {
		}
		return null;
	}

	@Override
	public synchronized IterableIterator<T> getEntities() {
		try {
			ResultSet rs = stdquery.executeQuery();
			return new RdbmsIterator<T>(cls, conv, rs);
		} catch (SQLException e) {
		}
		return new Empty<T>();
	}

	@Override
	public synchronized void insert(T record) {
		try {
			conv.encode(record, stdinsert);
			stdinsert.execute();
		} catch (SQLException e) {
		}
	}

	@Override
	public synchronized void update(T record) {
		try {
			conv.encodeUpdate(record, stdupdate);
			stdupdate.execute();
		} catch (SQLException e) {
		}
	}

	@Override
	public synchronized void put(T record) {
		try {
			conv.encode(record, stdinsert);
			stdinsert.execute();
		} catch (SQLException e) {
			// if insert fails retry with update
			try {
				conv.encodeUpdate(record, stdupdate);
				stdupdate.execute();
			} catch (SQLException e2) {
			}
		}
	}

	@Override
	public synchronized void delete(T record) {
		try {
			conv.setIdFromObject(stddelete, record);
			stddelete.execute();
		} catch (SQLException e) {
		}
	}

}
