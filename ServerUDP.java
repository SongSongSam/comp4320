import java.net.*;
import java.nio.*;

public class ServerUDP {
	public static void main(String[] args) throws Exception {
		// Check if port number is provided
		if (args.length != 1) {
			System.out.println("java ServerUDP <portnumber>");
			return;
		}

		// Parse the port number
		int portNumber = Integer.parseInt(args[0]);

		// Create a socket on the given port number
		DatagramSocket serverSocket = new DatagramSocket(portNumber);

		// Create byte arrays for receiving and sending data
		byte[] receiveData = new byte[1024];
		byte[] sendData;

		while(true) {
			// Receive packet from client
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);

			// Wrap the received data for easier parsing
			ByteBuffer wrapped = ByteBuffer.wrap(receiveData);

			// Parse the received data
			byte tml = wrapped.get();
			byte opCode = wrapped.get();
			int operand1 = wrapped.getInt();
			int operand2 = wrapped.getInt();
			short requestId = wrapped.getShort();
			byte opNameLength = wrapped.get();
			byte[] opNameBytes = new byte[opNameLength];
			wrapped.get(opNameBytes);
			String opName = new String(opNameBytes, "UTF-16BE");

			// Print the parsed data
			System.out.println("RequestID: " + requestId);
			System.out.println("Request[Hex]: " + bytesToHex(receiveData, tml));
			System.out.println("Operation Name: " + opName);
			System.out.println("Operand1: " + operand1);
			System.out.println("Operand2: " + operand2);

			// Perform the operation based on the opCode
			int result = 0;
			byte errorCode = 0; // assume no error
			switch(opCode) {
				case 0: result = operand1 * operand2; break;
				case 1: result = operand1 / operand2; break;
				case 2: result = operand1 | operand2; break;
				case 3: result = operand1 & operand2; break;
				case 4: result = operand1 - operand2; break;
				case 5: result = operand1 + operand2; break;
				default: errorCode = 127; // invalid request
			}

			// Prepare the response packet
			ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.put((byte)8); // TML
			buffer.putInt(result);
			buffer.put(errorCode);
			buffer.putShort(requestId);
			sendData = buffer.array();

			// Get client's address and port
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			// Send the response packet
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
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