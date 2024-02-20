import java.net.*;
import java.nio.*;

class ServerUDP {
	public static void main(String[] args) throws Exception {
		// Check if port number is provided
		if (args.length != 1) {
			System.out.println("Arguments Error");
			return;
		}

		// Parse the port number
		int portNumber = Integer.parseInt(args[0]);

		// Create a socket on the given port number
		DatagramSocket serverSocket = new DatagramSocket(portNumber);

		// Create byte arrays for receiving and sending data
		byte[] receiveData = new byte[1024];
		byte[] sendData;
		System.out.println("Server started");
		while(true) {
			// Receive packet from client
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);

			// Wrap the received data for easier parsing
			ByteBuffer wrapped = ByteBuffer.wrap(receiveData);

			// Parse the received data
			byte totalMessageLength = wrapped.get();
			byte operationCode = wrapped.get();
			int operand1 = wrapped.getInt();
			int operand2 = wrapped.getInt();
			short requestId = wrapped.getShort();
			byte operationNameLength = wrapped.get();
			byte[] operationNameBytes = new byte[operationNameLength];
			wrapped.get(operationNameBytes);
			String operationName = new String(operationNameBytes, "UTF-16BE");

			// Print the parsed data
			System.out.println("Request in HEX: " + bytesToHex(receiveData, totalMessageLength));
			System.out.println("RequestID: " + requestId);
			System.out.println("Operation Name: " + operationName);
			System.out.println("Operand1: " + operand1);
			System.out.println("Operand2: " + operand2);

			// Perform the operation based on the operationCode
			int result = performOperation(operationCode, operand1, operand2);

			// Prepare the response packet
			ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.put((byte)8); // Total Message Length
			buffer.putInt(result);
			buffer.put((byte)0); // Error Code
			buffer.putShort(requestId);
			sendData = buffer.array();

			// Get client's address and port
			InetAddress clientAddress = receivePacket.getAddress();
			int clientPort = receivePacket.getPort();

			// Send the response packet
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
			serverSocket.send(sendPacket);
		}
	}

	// Method to perform operation based on the operationCode
	private static int performOperation(byte operationCode, int operand1, int operand2) {
		switch(operationCode) {
			case 0: return operand1 * operand2;
			case 1: return operand1 / operand2;
			case 2: return operand1 | operand2;
			case 3: return operand1 & operand2;
			case 4: return operand1 - operand2;
			case 5: return operand1 + operand2;
			default: return 127; // invalid request
		}
	}

	// Method to convert byte array to hex string
	public static String bytesToHex(byte[] bytes, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(String.format("%02X ", bytes[i]));
		}
		return sb.toString();
	}
}
