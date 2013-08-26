import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public final class FileToJavaString
{
	/**
	 * main
	 */
	public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			help();
			return;
		}
		
		System.out.println("// " + args[0]);

		InputStream in;

		File file = new File(args[0]);

		if (file.isFile() == false)
		{
			URL url = FileToJavaString.class.getClassLoader().getResource(args[0]);

			if (url == null)
			{
				System.err.println("File not found: " + args[0]);
				help();
				return;
			}
			
			in = url.openStream();
		}
		else
		if (file.canRead() == false)
		{
			System.err.println("File not readable: " + args[0]);
			help();
			return;
		}
		else
		{
			in = new FileInputStream(file);
		}
		
		int counter = 0;
		int bytesOut = 0;

		System.out.print("\"");
		boolean quit = false;

		StringBuffer sb = new StringBuffer();

		while (quit == false)
		{
			int b1 = in.read();
			
			if (b1 == -1)
			{
				quit = true;
				continue;
			}
			
			if (counter >= 256)
			{
				System.out.println("\" +");
				System.out.print("\"");
				counter = 0;
			}

			System.out.print("\\" + Integer.toOctalString(b1));
			counter++;
			bytesOut++;
		}
		System.out.println("\";");
		System.out.println("// " + bytesOut + " bytes total");

	}
	private static void help()
	{
		System.err.println("usage: FileToJavaString input-file");
	}

	private static String byteToHex(byte b)
	{
		// Returns hex String representation of byte b
		char hexDigit[] =
		{
				'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
		};
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}
}
