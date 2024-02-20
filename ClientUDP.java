import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

class ClientUDP {
	private static final String EXIT_COMMAND = "q";

	public static void main(String[] args) throws Exception {
		// Check if server name and port number are provided
		if (args.length != 2) {
			return;
		}

		String serverName = args[0];
		int serverPort = Integer.parseInt(args[1]);

		// Create a socket and get the server's IP address
		try (DatagramSocket clientSocket = new DatagramSocket()) {
			InetAddress serverAddress = InetAddress.getByName(serverName);
			System.out.println("Client started");
			Scanner scanner = new Scanner(System.in);
			int requestCount = 1;
			while (true) {
				// Prepare the request data
				byte[] requestData = prepareRequest(scanner, requestCount);
				// Send the request data to the server
				sendRequest(clientSocket, serverAddress, serverPort, requestData);
				// Handle the response from the server
				handleResponse(clientSocket);

				System.out.println("Q to exit the program");
				if (EXIT_COMMAND.equalsIgnoreCase(scanner.next())) {
					break;
				}
				requestCount++;
			}
		}
	}

	private static byte[] prepareRequest(Scanner scanner, int requestId) throws Exception {
		System.out.println("OpCode, Operand1, Operand2: ");
		int opCode = scanner.nextInt();
		int operand1 = scanner.nextInt();
		int operand2 = scanner.nextInt();

		// Get the operation name based on the operation code
		String operationName = getOperationName(opCode);
		byte[] operationNameBytes = operationName.getBytes("UTF-16BE");
		int operationNameLength = operationNameBytes.length;

		// Prepare the request data
		ByteBuffer buffer = ByteBuffer.allocate(13 + operationNameLength);
		buffer.put((byte) (13 + operationNameLength));
		buffer.put((byte) opCode);
		buffer.putInt(operand1);
		buffer.putInt(operand2);
		buffer.putShort((short) requestId);
		buffer.put((byte) operationNameLength);
		buffer.put(operationNameBytes);

		return buffer.array();
	}

	private static String getOperationName(int opCode) {
		// Return the operation name based on the operation code
		switch (opCode) {
			case 0: return "multiplication";
			case 1: return "division";
			case 2: return "or";
			case 3: return "and";
			case 4: return "subtraction";
			case 5: return "addition";
			default: return "";
		}
	}

	private static void sendRequest(DatagramSocket clientSocket, InetAddress serverAddress, int serverPort, byte[] requestData) throws Exception {
		System.out.println("Request in HEX: " + bytesToHex(requestData));

		// Prepare and send the request packet
		DatagramPacket sendPacket = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);
		clientSocket.send(sendPacket);
	}

	private static void handleResponse(DatagramSocket clientSocket) throws Exception {
		byte[] receiveData = new byte[8];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);

		// Parse the response packet
		ByteBuffer wrapped = ByteBuffer.wrap(receiveData);
		byte tml = wrapped.get();
		int result = wrapped.getInt();
		byte errorCode = wrapped.get();
		short responseRequestId = wrapped.getShort();

		// Print the response
		System.out.println("Response in HEX" + bytesToHex(receiveData));
		System.out.println("Request ID: " + responseRequestId);
		System.out.println("Result: " + result);
		System.out.println("Error Code: " + (errorCode == 0 ? "Ok" : errorCode));
	}

	private static String bytesToHex(byte[] bytes) {
		// Convert byte array to hex string
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}
}
