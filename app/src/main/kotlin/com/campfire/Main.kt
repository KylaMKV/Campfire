package com.campfire;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.*;
import java.security.*;
import java.util.Scanner;
import java.security.spec.*;
import kotlin.byteArrayOf;
import kotlin.concurrent.thread;
import java.net.*;
import javax.crypto.Cipher
import java.util.Random;
import java.lang.ref.*;
import java.util.Base64;

val IDABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" // For generating ID's

val PORT = 6280;

fun base64ify(data: ByteArray): String {
	return Base64.getEncoder().encodeToString(data);
}
fun unbase64ify(data: String): ByteArray {
	return Base64.getDecoder().decode(data);
}

open class PartialMessage { // Superclass to SentMessage and Message. For conversion from SentMessage to Message.
	
	
}
open class Messagable {
	val table: String; // The table where the messages are stored in an SQL db
	constructor() {this.table = "null";}
}
class User(uid: String, username: String, pubkey: PublicKey, privkey: ByteArray) : Messagable() {
	val uid: String;
	val username: String;
	val privkey: ByteArray; // it's not PrivateKey because it's encrypted AES bytes the user then decrypts on their device.
	val pubkey: PublicKey; 
	val cipher: Cipher;
	var online: Boolean = false;
	init {
		this.uid = uid;
		this.username = username;
		this.privkey = privkey;
		this.pubkey = pubkey;
		this.cipher = Cipher.getInstance("RSA");
		this.cipher.init(Cipher.ENCRYPT_MODE, this.pubkey);
	}
	override fun toString(): String {
		return "$uid|$username";
	}
	fun gen_auth_code(): Set<ByteArray> {
		val random_code = (0..Random().nextInt(100, 1000)).map { Random().nextInt(0, 255).toByte() }.toByteArray(); // Basically just generates a random array of bytes of a random size between 100-1000.
		return setOf( this.cipher.doFinal(random_code), random_code);
	}
	companion object {
		fun dumby(): User {
			val keypair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
			return User("u00000", "bob", keypair.getPublic(), keypair.getPrivate().getEncoded());

		}
		fun load(dbresult: HashMap<String, Any>): User {
			val uid = dbresult["uid"] as String;
			val username = dbresult["username"] as String;
			val privkey = unbase64ify(dbresult["privkey"] as String);
			val pubkey = keyhandler.loadPublic(unbase64ify(dbresult["pubkey"] as String));
			// val uid = dbresult.getString("uid");
			// val username = dbresult.getString("username");
			// val privkey = dbresult.getBytes("privkey");
			// val pubkey = keyhandler.loadPublic(dbresult.getBytes("pubkey"));
			return User(uid, username, pubkey, privkey);
		}
		fun new(username: String): User {
			val rand = Random();
			var uid = "";
			for(i in (1..32)) {
				uid += IDABLE_CHARS[rand.nextInt(0, IDABLE_CHARS.length)];
			}
			val keypair = KeyPairGenerator.getInstance("RSA").genKeyPair();
			DB(MAINDBPATH).insert("Users", mapOf("uid" to uid, "username" to username, "privkey" to keypair.getPrivate().getEncoded(), "pubkey" to keypair.getPublic().getEncoded()));
			return User(uid, username, keypair.getPublic(), keypair.getPrivate().getEncoded());
		}
	}
}
class Message(sender: User, recipient: User) : PartialMessage() {
	val sender: User;
	val recipient: Messagable;
	val message: List<ByteArray>;
	init {this.sender = sender; this.recipient = recipient; this.message = emptyList();}
}
class Server() {
	val pubkey: PublicKey;
	val privkey: PrivateKey;
	var activeUsers: List<User>;
	val server: ServerSocket;
	init {
		val pubkeypem = File("pub.pem");
		val privkeypem = File("priv.pem");
		if(!pubkeypem.exists() || !privkeypem.exists()) {
			println("One or both of the servers keys are missing or corrupt. Generating new keys...");
			val keypair = KeyPairGenerator.getInstance("RSA").genKeyPair();
			this.pubkey = keypair.getPublic();
			this.privkey = keypair.getPrivate();
			println("Generated keys, Saving...");
			keyhandler.savePublic(this.pubkey, "pub.pem");
			keyhandler.savePrivate(this.privkey, "priv.pem");

		} else {
			println("Loading keys...")
			this.pubkey = keyhandler.loadPublic("pub.pem");
			this.privkey = keyhandler.loadPrivate("priv.pem");
		}
		this.activeUsers = emptyList();
		this.server = ServerSocket(PORT);
		// this.server.bind(InetSocketAddress("127.0.0.1", PORT));
	}
	fun get_user(uid: String): User? {
		val user = this.activeUsers.find() { it.uid == uid };
		if(user == null) {
			val db = DB(MAINDBPATH);
			return db.get_user(hashMapOf("uid" to uid));
		}
		return user;
	}
	fun start() {
		while(true) {
			var client: Socket = this.server.accept();
			thread(start=true) {
				Client(client).request_handler();	
			}
		}
	}
}
enum class OperationStage {
	AWAITING_AUTHENTICATION,
	COMPLETING_AUTH_CHALLENGE,
	ONLINE
}
class Client(client: Socket) {
	var user: User? = null;
	val socket: Socket;
	var stage = OperationStage.AWAITING_AUTHENTICATION;
	var auth_code: ByteArray? = null;
	private fun respond(response: Response) {
		val writer = socket.getOutputStream();
		writer.write(response.prepare());
		println("Sent data to client: " + response.prepare().toString());
		// writer.flush();

	}
	private fun read(): String {
		var reader = socket.getInputStream();
		val ret = reader.readAllBytes();
		// I made it this way because I didn't know `reader.readAllBytes()` existed :(
		// var buf: Byte = 0xFF.toByte();
		// while(buf != 0x00.toByte()) {
		// 	buf = reader.read().toByte();
		// }
		// var ret: ByteArray = byteArrayOf();
		// while(true) {
		// 	buf = reader.read().toByte();
		// 	if(buf == 0xFF.toByte()) break;
		// 	ret += buf;
		// }
		return String(ret);
	}
	init {
		socket = client;
		// this.user = user;
	}
	fun request_handler() {
		while(!socket.isClosed()) {
			val request = Request(read());
			val response = global_handler(request);
			if (response != null) {
				this.respond(response);
				continue
			}
			if (this.stage == OperationStage.AWAITING_AUTHENTICATION) {
				this.auth(request);
			}
			if (this.stage == OperationStage.COMPLETING_AUTH_CHALLENGE) {
				this.finish_auth(request);
			}
		}
	}
	fun finish_auth(request: Request) {
		val code = request.args["CODE"]!!.toByteArray();
		if (code == this.auth_code) {
			this.stage = OperationStage.ONLINE;
			server.activeUsers += this.user!!;
			this.respond(Response.create(200, ""));
		}
	}
	fun auth(request: Request) {
		val db = DB(MAINDBPATH);
		val user = db.get_user(hashMapOf("uid" to request.args["UID"]!!));
		this.user = user;
		val auth_code_set = user.gen_auth_code();
		this.auth_code = auth_code_set.last();
		this.respond(Response.create(200, auth_code_set.first()));
		this.stage = OperationStage.COMPLETING_AUTH_CHALLENGE;
	}
	private fun global_handler(request: Request): Response? {
		if(request.type == RequestType.FETCH) {
			val db = DB(MAINDBPATH);
			try {
				val res = db.get_protected(request.args["CLASS"]!!, mapOf(request.args["SEARCH"]!! to request.args["TERM"]!!));
				if(res == null) return Response.create(404, "NOT_FOUND");
				return Response.create(200, res);

			}catch (e: Exception) {
				println("Malformed FETCH request: '${e}'.");
				return Response.create(400, "MALFORMED");
			}
		}
		return null;
	}

	private fun get_auth_code(request: Request): Response {
		val user = server.get_user(request.args["UID"]!!);
		if (user == null) {return Response.create(404, "USER_NOT_FOUND");}
		val auth_code = user.gen_auth_code();
		return Response.create(200, base64ify(auth_code.first()));
	}
}


val server = Server();
fun main() {
	DB(MAINDBPATH);
	server.start();
}
