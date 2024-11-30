package com.campfire;
import java.sql.*;
import java.io.File;
import kotlin.collections.mutableMapOf
import java.util.Base64;

val PROTECTED_TABLES = arrayListOf(
	""
);
val PROTECTED_COLUMNS = arrayListOf(
	""
);

val MAINDBPATH = "test.db";
private val BASE_TABLES = mapOf(
	"Users" to "uid TEXT PRIMARY KEY, username TEXT, privkey TINYTEXT, pubkey TINYTEXT"
);

private fun sql_tuplifier(data: Collection<Any>): String {
	var ret = "";
	for(item in data) {
		when(item::class.simpleName) {

			"Int" -> ret += item.toString() + ",";

			"String" -> ret += "'" + item + "',";

			"ByteArray" -> ret += "'" + base64ify(item as ByteArray) + "',";

			"Double" -> ret += item.toString() + ",";

			else -> throw Exception("Invalid data type");
		}
	}
	return ret.substring(0, ret.length - 1);
}

private fun sqlify_map(params: Map<String, Any>): String {
	var query = "";
	for(param in params.keys) {
		query += "$param = '${params[param]}' AND ";
	}
	query = query.substring(0, query.length - 5);
	return query
	
}

class DB(dbpath: String) {
	val db: Connection;
	init {
		if(!File(dbpath).exists()) {
			File(dbpath).createNewFile();
		}
		Class.forName("org.sqlite.JDBC");
		this.db = DriverManager.getConnection("jdbc:sqlite:$dbpath");
		this.create_base_tables();
	}
	fun create_base_tables() {
		val stmt = this.db.createStatement();
		for(table in BASE_TABLES.keys) {
			stmt.execute("CREATE TABLE IF NOT EXISTS $table (${BASE_TABLES[table]})");
		}

	}
	fun get(query: String): ResultSet {
		val stmt = this.db.createStatement();
		return stmt.executeQuery(query);
	}
	// Get is designed to only return one value because it is meant to be searched with either a 
	// UID or username, both of which should never have a collision.
	fun get(table: String, params: Map<String, String>): HashMap<String, Any> {
		var query = "SELECT * FROM $table WHERE ";
		query += sqlify_map(params);
		// return this.get(query);
		val row = this.get(query);
		val ret = HashMap<String, Any>();
		// var index = 1;
		while(row.next()) {
			for(index in 1..row.getMetaData().getColumnCount()) {
				val rowdata = row.getObject(index);
				val col = row.getMetaData().getColumnName(index);
				ret[col] = rowdata
			}
			
		// while(index >= row.getMetaData().getColumnCount()) {
		//
			// val rowdata: Any;
			// try {
			// 	rowdata = row.getObject(index);
			// 	println(rowdata)
			// } catch (e: Exception) {
			// 	println(e);
			// 	break;
			// }
			// val col = row.getMetaData().getColumnName(index);
			// ret[col] = rowdata;
			// row.next();
			// index++;
		}
		return ret;
	}
	fun get_user(params: Map<String, String>): User {
		println(this.get("Users", params))
		return User.load(this.get("Users", params));
		
	}
	fun get_protected(table: String, params: Map<String, String>): HashMap<String, Any>? {
		if(table in PROTECTED_TABLES) return null
		if (params.keys.any { it in PROTECTED_COLUMNS }) return null
		if(table == "Users") {
			// TODO
			// Users would be able to redact certain aspects of themselves from FETCH requests.

		}
		return this.get(table, params)

	}
	fun insert(table: String, params: Map<String, Any>) {
		val stmt = this.db.createStatement();
		stmt.execute("INSERT INTO $table(${sql_tuplifier(params.keys)}) VALUES(${sql_tuplifier(params.values)})");
	}
	fun insert_user(user: User) {
		val stmt = this.db.createStatement();
		println(user.privkey.toString() + user.pubkey.toString());
		stmt.execute("INSERT INTO Users(uid, username, privkey, pubkey) VALUES('${user.uid}', '${user.username}', '${base64ify(user.privkey)}', '${base64ify(user.pubkey.getEncoded())}')");
	}
	fun delete(table: String, params: Map<String, Any>): Boolean {
		val stmt = this.db.createStatement();
		return stmt.execute("DELETE FROM $table WHERE ${sqlify_map(params)}");
	}
}
