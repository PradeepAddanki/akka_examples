package lambdasinaction.chap2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Testing {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

		MessageDigest md = MessageDigest.getInstance("SHA-256"); // SHA, MD2, MD5, SHA-256, SHA-384...
		long time = System.currentTimeMillis();
		String hex = checksum("C:\\Users\\kumar\\Downloads\\test.pdf", md);
		System.out.println((System.currentTimeMillis()-time));
		System.out.println(hex);
		Path path = new File("C:\\Users\\kumar\\Downloads\\test_2.pdf").toPath();

		try {
			UserDefinedFileAttributeView view  = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
			if(view != null) {
				String value = getAttrValue(view, "Testing");
				System.out.println(value);
			}
			time = System.currentTimeMillis();
		view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
		view.write("Testing", Charset.defaultCharset().encode("TTTTTTTTTTTTTTTTTTTTTT@@@@TTTTTTT"));
		System.out.println((System.currentTimeMillis()-time));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		time = System.currentTimeMillis();
		String hex1 = checksum("C:\\Users\\kumar\\Downloads\\test_2.pdf", md);
		System.out.println((System.currentTimeMillis()-time));
		System.out.println(hex1);
	}
	private static String getAttrValue(UserDefinedFileAttributeView view, String name) throws IOException {
        int attrSize = view.size(name);
		ByteBuffer buffer = ByteBuffer.allocateDirect(attrSize);
        view.read(name, buffer);
        buffer.flip();
        return Charset.defaultCharset().decode(buffer).toString();
    }
	private static String checksum(String filepath, MessageDigest md) throws IOException {

		// file hashing with DigestInputStream
		try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
			while (dis.read() != -1)
				; // empty loop to clear the data
			md = dis.getMessageDigest();
		}

		// bytes to hex
		StringBuilder result = new StringBuilder();
		for (byte b : md.digest()) {
			result.append(String.format("%02x", b));
		}
		return result.toString();
	}

}
