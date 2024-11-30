package com.campfire

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import java.security.KeyPairGenerator;

class AppTest {
    @Test fun basic() {
		val keypair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		val UserTest = User("u0000", "kyla", keypair.getPublic(), keypair.getPrivate().getEncoded())
        assertEquals(UserTest.uid, "u0000", "UID should be u0000. It is " + UserTest.uid);

		assertEquals(UserTest.username, "kyla", "Username should be kyla. It is " + UserTest.username);

		assertEquals(UserTest.pubkey, keypair.getPublic(), "Pubkey should be " + keypair.getPublic() + ". It is " + UserTest.pubkey);

		// assertEquals(UserTest.privkey, keypair.getPrivate().getEncoded(), "Privkey should be " + keypair.getPrivate().getEncoded() + ". It is " + UserTest.privkey);
		
		val db = DB("test.db");
		val ret = db.db.createStatement().executeQuery("PRAGMA table_info(Users)");
		val data = ret.getCharacterStream(2);
		var byte = data.read();
		while(byte != -1) {
			print(byte.toChar());
			byte = data.read();
		}
	}
		// println();
}
