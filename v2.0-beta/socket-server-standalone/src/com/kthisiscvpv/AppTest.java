package com.kthisiscvpv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

import org.json.JSONObject;

import com.kthisiscvpv.socket.client.Packet;
import com.kthisiscvpv.util.hash.AES256;
import com.kthisiscvpv.util.hash.SHA256;

public class AppTest {

	public static void main(String[] args) throws Exception {
		String address = "localhost";
		int port = 26317;
		String room = "hunter2";
		String salt = "3gSV'%N=55c^z::@";

		Socket socket = new Socket(address, port);

		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

		JSONObject data = new JSONObject();
		data.put("header", Packet.JOIN);
		data.put("room", SHA256.encrypt(room));
		data.put("name", AES256.encrypt(room + salt, address));
		pw.println(Base64.getEncoder().encodeToString(data.toString().getBytes()));

		String response = br.readLine();
		while (response.isEmpty())
			response = br.readLine();

		System.out.println(new String(Base64.getDecoder().decode(response)));

		socket.close();
	}

}
