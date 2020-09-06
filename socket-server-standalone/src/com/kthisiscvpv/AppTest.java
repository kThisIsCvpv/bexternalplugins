package com.kthisiscvpv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class AppTest {

	public static void main(String[] args) throws Exception {

		File file = new File("C:/Users/Charles/Desktop/2020-07-16.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));

		int maxRoom = 0;
		int maxLength = 0;

		String ln;
		while ((ln = br.readLine()) != null) {
			if (ln.contains("broadcasted a packet to")) {
				ln = ln.split("packet to")[1].split("total")[0].trim();
				int count = Integer.parseInt(ln);
				if (count > maxRoom)
					maxRoom = count;
			} else if (ln.contains("length=")) {
				ln = ln.split("length=")[1].split("\\)")[0].trim();
				int count = Integer.parseInt(ln);
				if (count > maxLength)
					maxLength = count;
			}
		}

		System.out.printf("broadcasted a packet to %d total members\n", maxRoom);
		System.out.printf("(length=%d)\n", maxLength);
		br.close();

//		String address = "localhost";
//		int port = 26317;
//		String room = "hunter2";
//		String salt = "3gSV'%N=55c^z::@";
//
//		Socket socket = new Socket(address, port);
//
//		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//		PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
//
//		JSONObject data = new JSONObject();
//		data.put("header", Packet.JOIN);
//		data.put("room", SHA256.encrypt(room));
//		data.put("name", AES256.encrypt(room + salt, address));
//		pw.println(Base64.getEncoder().encodeToString(data.toString().getBytes()));
//
//		String response = br.readLine();
//		while (response.isEmpty())
//			response = br.readLine();
//
//		System.out.println(new String(Base64.getDecoder().decode(response)));
//
//		socket.close();
	}

}
