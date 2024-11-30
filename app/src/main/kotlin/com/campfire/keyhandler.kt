package com.campfire;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.security.spec.*

class keyhandler { // TODO: Move to seperate file
	companion object {
		fun loadPublic(filepath: String): PublicKey {
			val reader = FileInputStream(filepath);
			var bytes: List<Byte> = emptyList();
			while (reader.available() > 0) {
				bytes = bytes + reader.read().toByte();
			}
			return loadPublic(bytes.toByteArray());
		}
		fun loadPublic(data: ByteArray): PublicKey {
			return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(data));
		}
		fun loadPrivate(filepath: String): PrivateKey {
			val reader = FileInputStream(filepath);
			var bytes: List<Byte> = emptyList();
			while (reader.available() > 0) {
				bytes = bytes + reader.read().toByte();
			}
			return loadPrivate(bytes.toByteArray());
		}
		fun loadPrivate(data: ByteArray): PrivateKey {
			return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(data));
		}
		fun savePublic(key: PublicKey, filepath: String) {
			val writer = FileOutputStream(filepath);
			writer.write(key.encoded);
			writer.flush();
			writer.close();
		}
		fun savePrivate(key: PrivateKey, filepath: String) {
			val writer = FileOutputStream(filepath);
			writer.write(key.encoded);
			writer.flush();
			writer.close();
		}
	}
}
