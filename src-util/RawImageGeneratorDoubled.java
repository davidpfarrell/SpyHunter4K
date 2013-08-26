import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public final class RawImageGeneratorDoubled
{
	static class RawImage
	{
		int[] data;
		
		Rectangle r;

		boolean doubleFlag;
		boolean xFlipFlag;

		public RawImage(Rectangle r, boolean doubleFlag, boolean xFlipFlag)
		{
			this.r = r;
			this.doubleFlag = doubleFlag;
			this.xFlipFlag = xFlipFlag;

			data = new int[r.width * r.height];
		}

		public void setColor(int x, int y, int color)
		{
			if (x < 0 || x > r.width)
			{
				throw new IllegalArgumentException("x(" + x + ") <> " + r.width);
			}
			if (y < 0 || y > r.height)
			{
				throw new IllegalArgumentException("y(" + y + ") <> " + r.height);
			}

			data[x + (y * r.width)] = color & 0xff;
		}
		
		public void write8bit(OutputStream os) throws Exception
		{
			int w = (r.width  & 0x7f) | (doubleFlag ? 0x80 : 0x00);
			int h = (r.height & 0x7f) | (xFlipFlag  ? 0x80 : 0x00);
			
			os.write(w);
			os.write(h);

			for (int x = 0; x < r.width; ++x)
			{
				for (int y = 0; y < r.height; ++y)
				{
					int color = data[x + (r.width * y)] & 0x0f;
					os.write(color);
				}
			}
		}

		public void write4bit(OutputStream os) throws Exception
		{
			int w = (r.width  & 0x7f) | (doubleFlag ? 0x80 : 0x00);
			int h = (r.height & 0x7f) | (xFlipFlag  ? 0x80 : 0x00);

			os.write(w);
			os.write(h);
			
			int bit = 0;

			int out = 0;

			for (int x = 0; x < r.width; ++x)
			{
				for (int y = 0; y < r.height; ++y)
				{
					int color = data[x + (r.width * y)] & 0x0f;
					
					if (bit == 0)
					{
						out = (color << 4);
						bit = 1;
					}
					else
					{
						out = out | color;
						bit = 0;
						os.write(out);
					}
				}
			}
			// dangler?
			if (bit == 1)
			{
				os.write(out);
			}
		}
	}

	static Rectangle[] spritePosList = new Rectangle[]
	{
		new Rectangle(  0, 0, 16, 64), // van
		new Rectangle( 17, 0, 14, 58), // enforcer
 		new Rectangle( 32, 0, 13, 41), // road lord
		new Rectangle( 46, 0 ,13, 41), // switch blade
		new Rectangle( 60, 0, 12, 41), // z28
		new Rectangle( 73, 0, 13, 39), // red
		new Rectangle( 87, 0, 12, 32), // bug
		new Rectangle(100, 0,  9, 32), // cycle
		new Rectangle(127, 0, 10,  7), // blade
		new Rectangle(138, 0, 15,  7), // shooter
		new Rectangle(110, 0, 16, 33), // tree
	};

	static boolean[] spriteDoubleFlags = 
     {
		true,  // van
		true,  // enforcer
		true,  // road lord
		true,  // switch blade
		true,  // z28
		true,  // red
		true,  // bug
		true,  // cycle
		false, // blade
		false, // shooter
		true,  // tree
     };

	static boolean[] spriteXFlipFlags = 
	 {
		false, // van
		false, // enforcer
		false, // road lord
		false, // switch blade
		false, // z28
		false, // red
		false, // bug
		false, // cycle
		true,  // blade
		true,  // shooter
		false, // tree
	 };
	
	static int[] rgbList = new int[16];
	static int colorIndex = 0;

	static List rawImages = new ArrayList();

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

		BufferedImage bi = ImageIO.read(in);

		for (int index = 0; index < spritePosList.length; index++)
		{
			System.out.println("Processing Sprite at index " + index);
			
			boolean doubleX = spriteDoubleFlags[index];
			System.out.println("DoubleX = " + doubleX);
			Rectangle r = spritePosList[index];
			System.out.println("r = " + r);
			Rectangle r2;
			
			if (doubleX)
			{
				r2 = new Rectangle(r.x, r.y, r.width * 2, r.height);
			}
			else
			{
				r2 = r;
			}
			System.out.println("r2 = " + r2);

			RawImage ri = new RawImage(r2, false, spriteXFlipFlags[index]);

			for (int x = 0; x < r.width; ++x)
			{
				for (int y = 0; y < r.height; ++y)
				{
					int rgb = bi.getRGB(r.x + x, r.y +y);
					int colorIndex = findColorIndex(rgb);
//					System.out.println("Setting " + x + ", " + y + " to " + colorIndex);
					ri.setColor(x, y, colorIndex);
					if (doubleX)
					{
//						System.out.println("Setting " + (r2.width - x - 1) + ", " + y + " to " + colorIndex);
						ri.setColor(r2.width - x - 1 , y, 0);
					}
				}
			}

			rawImages.add(ri);
		}

		FileOutputStream fo = new FileOutputStream("raw2.out");

		for (int i = 0; i < 16; i++)
		{
			int color;

			if (i < colorIndex)
			{
				color = rgbList[i];
			}
			else
			{
				color = 0;
			}

			int r = (color >> 16) & 0xff;
			int g = (color >>  8) & 0xff;
			int b = (color      ) & 0xff;
			fo.write(r);
			fo.write(g);
			fo.write(b);
		}
		
		for(Iterator i = rawImages.iterator(); i.hasNext();)
		{
			RawImage ri = (RawImage)i.next();
			ri.write8bit(fo);
		}

		fo.close();
	}
	
	private static int findColorIndex(int rgb)
	{
		int result = -1;

		for (int i = 0; i < colorIndex; ++i)
		{
			if (rgbList[i] == rgb)
			{
//				System.out.println("found color " + rgb + " at index " + i);
				result = i;
				break;
			}
		}

		if (result == -1)
		{
			result = colorIndex;
			System.out.println("adding color " + rgb + " at index " + result);
			rgbList[result] = rgb;
			++colorIndex;
		}

		return result;
	}

	private static void showImage(Image image)
	{
		JFrame f = new JFrame("SpyHunter4K");

		f.setDefaultCloseOperation(3);
		f.setResizable(false);
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(480, 480));
		f.getContentPane().add(p, "Center");
		f.pack();
		Dimension dimension = f.getToolkit().getScreenSize();
		f.setLocation((dimension.width >> 1) - (f.getWidth() >> 1),
		    (dimension.height >> 1) - (f.getHeight() >> 1));
		f.show();

		while (p.isVisible())
		{
			Graphics gf = p.getGraphics();

			gf.drawImage(image, 0, 0, null);

			gf.dispose();

			try
			{
			    Thread.sleep(15L);
			}
			catch (InterruptedException e)
			{
				;
			}
		}
	}

	private static void help()
	{
		System.err.println("usage: RawImageGenerator input-file");
	}
}
