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

public class SqlDialect {
	public static enum Type{
		INT,
		INTLONG,
		INTSHORT,
		FLOAT,
		DOUBLE,
		STRING,
		BINARY,
		CLOB,
		BLOB,
		BOOLEAN,
	}
	public String sqlType(Type t){
		switch(t){
		case INT:
			return "INTEGER";
		case INTLONG:
			return "BIGINT";
		case INTSHORT:
			return "SMALLINT";
		case FLOAT:
			return "FLOAT";
		case DOUBLE:
			return "DOUBLE PRECISION";
		case STRING:
			return "VARCHAR";
		case BINARY:
			return "VARBINARY";
		case CLOB:
			return "CLOB";
		case BLOB:
			return "BLOB";
		case BOOLEAN:
			return "BOOLEAN";
		}
		throw new RuntimeException("unknown "+t);
	}
}
