import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public final class FileToText
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
			URL url = FileToText.class.getClassLoader().getResource(args[0]);

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
		
		boolean quit = false;

		while (quit == false)
		{
			int b1 = in.read();
			
			if (b1 == -1)
			{
				quit = true;
				continue;
			}

//			System.out.print( Character.forDigit( ((b1 >> 4) & 0x0f) | 0x00, 16) );
//			System.out.print( Character.forDigit( ((b1     ) & 0x0f) | 0x00, 16) );
//			System.out.print('.');
			System.out.print(  (char)(((b1 >> 4) & 0x0f) | 0x40) );
			System.out.print(  (char)(((b1     ) & 0x0f) | 0x40) );
		}
		
//		System.out.println();
		System.out.println("\n.");

	}
	private static void help()
	{
		System.err.println("usage: FileToText input-file");
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
