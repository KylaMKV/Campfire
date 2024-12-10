package com.campfire

import kotlin.test.*;
import java.security.KeyPairGenerator;

class AppTest {
	@Test fun DBTest() {
		val db = DB("tmp.db");
		db.create_table("test", mapOf("key1" to "TINYTEXT", "key2" to "INT"));
		var table = db.table_info("test");
		assertEquals(table.size, 2, "Table 'test' size should be '2'. Size is '${table.size}'");
		// NAME TEST
		assertEquals(table[0]["name"] as String, "key1", "Name of table 'test' column 0 not equal to 'key1'. Is '${table[0]["name"] as String}'");
		assertEquals(table[1]["name"] as String, "key2", "Name of table 'test' column 1 not equal to 'key2'. Is '${table[1]["name"] as String}'");
		// TYPE TEST
		assertEquals(table[0]["type"] as String, "TINYTEXT", "Type of table 'test' column 'key1' not equal to 'TINYTEXT'. Is '${table[0]["type"] as String}'");
		assertEquals(table[1]["type"] as String, "INT", "Type of table 'test' column 'key2' not equal to 'INT'. Is '${table[1]["type"] as String}'");
	}
	@Test fun InteractionTest() {
		
	}
    @Test fun UserTest() {
		val keypair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		val UserTest = User("u0000", "kyla", keypair.getPublic(), keypair.getPrivate().getEncoded())
        assertEquals(UserTest.uid, "u0000", "UID should be u0000. It is " + UserTest.uid);

		assertEquals(UserTest.username, "kyla", "Username should be kyla. It is " + UserTest.username);

		assertEquals(UserTest.pubkey, keypair.getPublic(), "Pubkey should be " + keypair.getPublic() + ". It is " + UserTest.pubkey);

		// // assertEquals(UserTest.privkey, keypair.getPrivate().getEncoded(), "Privkey should be " + keypair.getPrivate().getEncoded() + ". It is " + UserTest.privkey);
		//
		// val db = DB("test.db");
		// val ret = db.db.createStatement().executeQuery("PRAGMA table_info(Users)");
		// val data = ret.getCharacterStream(2);
		// var byte = data.read();
		// while(byte != -1) {
		// 	print(byte.toChar());
		// 	byte = data.read();
		// }
	}
		// println();
}
