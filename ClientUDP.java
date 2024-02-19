import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Scanner;

public class ClientUDP {
	// Method to convert byte array to hex string
	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}

	public static void main(String args[]) throws Exception {
		// Check if server name and port number are provided
		if (args.length != 2) {
			return;
		}

		// Parse the server name and port number
		String serverName = args[0];
		int serverPort = Integer.parseInt(args[1]);

		// Create a socket and get the server's IP address
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(serverName);

		// Create byte arrays for sending and receiving data
		byte[] sendData;
		byte[] receiveData = new byte[8];

		// Create a scanner for user input
		Scanner scanner = new Scanner(System.in);
		int requestCount = 1;
		while (true) {
			// Prompt the user for operation code and operands
			System.out.println("OpCode, Operand1, Operand2: ");
			int opCode = scanner.nextInt();
			int operand1 = scanner.nextInt();
			int operand2 = scanner.nextInt();

			// Convert the operation code to operation name
			String opName = "";
			switch(opCode) {
				case 0: opName = "multiplication"; break;
				case 1: opName = "division"; break;
				case 2: opName = "or"; break;
				case 3: opName = "and"; break;
				case 4: opName = "subtraction"; break;
				case 5: opName = "addition"; break;
			}
			byte[] opNameBytes = opName.getBytes("UTF-16BE");
			int opNameLength = opNameBytes.length;

			// Generate request ID
			int requestId = requestCount;

			// Prepare the request packet
			ByteBuffer buffer = ByteBuffer.allocate(13 + opNameLength);
			buffer.put((byte)(13 + opNameLength)); // TML
			buffer.put((byte)opCode);
			buffer.putInt(operand1);
			buffer.putInt(operand2);
			buffer.putShort((short)requestId);
			buffer.put((byte)opNameLength);
			buffer.put(opNameBytes);
			sendData = buffer.array();

			// Print the request in hex
			System.out.println("Request[Hex]: " + bytesToHex(sendData));

			// Send the request packet
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
			long start = System.currentTimeMillis();
			clientSocket.send(sendPacket);

			// Receive the response packet
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			long end = System.currentTimeMillis();

			// Parse the response packet
			ByteBuffer wrapped = ByteBuffer.wrap(receiveData);
			byte tml = wrapped.get();
			int result = wrapped.getInt();
			byte errorCode = wrapped.get();
			short responseRequestId = wrapped.getShort();

			// Print the response and round trip time
			System.out.println("Response Below:");
			System.out.println("Response[Hex]" + bytesToHex(receiveData));
			System.out.println("Request ID: " + responseRequestId);
			System.out.println("Result: " + result);
			System.out.println("Error Code: " + (errorCode == 0 ? "Ok" : errorCode));
			System.out.println("Round trip time: " + (end - start) + "ms");

			// Prompt the user to exit or continue
			System.out.println("Enter e to exit the program otherwise continue: ");
			String input = scanner.next();
			if (input.equalsIgnoreCase("e")) {
				break;
			}
			requestCount++;
		}
		clientSocket.close();
	}
}