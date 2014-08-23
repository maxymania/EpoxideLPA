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
import java.sql.SQLException;

import epoxide.lpa.Database;
import epoxide.lpa.RecordSet;

public class RdbmsDatabase implements Database {
	private RdbmsClassTablePool pool;
	private boolean creating;
	public RdbmsDatabase(Connection conn, SqlDialect dialect,boolean creating){
		pool = new RdbmsClassTablePool(conn, dialect);
		this.creating = creating;
	}
	@Override
	public <T> RecordSet<T> getRecordSet(Class<T> clazz) {
		RdbmsClassTable conv = pool.get(clazz);
		if(creating) try {
			conv.createTable();
		} catch (SQLException e) {
		}
		return new RdbmsRecordSet<T>(clazz, conv);
	}
}
