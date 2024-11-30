package com.campfire;
class Response(status: Int, data: ByteArray) {
	val status: Int;
	val data: ByteArray;
	init {
		this.status = status;
		this.data = data;
	}
	override fun toString(): String {
		return "$status\n${String(data)}";
	}
	fun prepare(): ByteArray {
		return byteArrayOf(0x00) + "$status\n".toByteArray() + data + 0xFF.toByte();
	}
	companion object {
		fun create(status: Int, data: String): Response {
			return Response(status, data.toByteArray());

		}
		fun create(status: Int, data: ByteArray): Response {
			return Response(status, data);
		}
		fun create(status: Int, data: HashMap<String, Any>): Response {
			var ret = "";
			for(key in data.keys) {
				ret += "$key:${data[key]}\n";
			}
			return Response(status, ret.toByteArray());
		}
	}
}

enum class RequestType {
	FETCH,
	MESSAGE,
	AUTHCHALLENGE,
	MALFORMED
}

fun reqtype_converter(type: String): RequestType {
	when(type) {
		"FETCH" -> return RequestType.FETCH;
		"MESSAGE" -> return RequestType.MESSAGE;
		"AUTHCHALLENGE" -> return RequestType.AUTHCHALLENGE;
		else -> return RequestType.MALFORMED;
	}
}

class Request(reqString: String) {
	var type: RequestType;
	val args: MutableMap<String, String> = mutableMapOf();
	init {
		try {
			var data: Array<String>;
			data = reqString.split("\n").toTypedArray();
			this.type = reqtype_converter(data[0]);
			data = data.filter({it != data[0]}).toTypedArray();
			var exit = 0;
			for(header in data) {
				if(header == "") {exit++;continue;}
				if(exit >= 2) break;
				val headerdata = header.split(":").toTypedArray();
				this.args[headerdata[0]] = headerdata[1];
			}
		} catch (e: Exception) {
			this.type = RequestType.MALFORMED;
			println("Malformed request: $reqString\n${e.message}");
		}
	}
}
