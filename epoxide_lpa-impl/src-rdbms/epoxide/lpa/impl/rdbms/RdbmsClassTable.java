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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RdbmsClassTable {
	public ClassConverter conv;
	public Connection conn;
	public SqlDialect dialect;
	public PreparedStatement stdlookup;
	public PreparedStatement stdquery;
	public PreparedStatement stdinsert;
	public PreparedStatement stdupdate;
	public PreparedStatement stddelete;
	public RdbmsClassTable(Class<?> cls, ClassConverter conv, Connection conn,
			SqlDialect dialect) throws SQLException {
		super();
		this.conv = conv;
		this.conn = conn;
		this.dialect = dialect;
		List<String> empty = Arrays.asList();
		stdlookup = conn.prepareStatement(conv.createSelectByKey(dialect));
		stdquery  = conn.prepareStatement(conv.createSelect(dialect, empty));
		stdinsert = conn.prepareStatement(conv.createInsert(dialect));
		stdupdate = conn.prepareStatement(conv.createUpdate(dialect));
		stddelete = conn.prepareStatement(conv.createDelete(dialect));
	}
	public void createTable() throws SQLException{
		String sqlstr = conv.createTable(dialect);
		PreparedStatement sqlstm = conn.prepareStatement(sqlstr);
		sqlstm.execute();
	}
}
