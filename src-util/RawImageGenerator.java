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

public final class RawImageGenerator
{
	static final String FILENAME = "HalfMontage6-16color";

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
			int w = (r.width  & 0x7f);// | (doubleFlag ? 0x40 : 0x00);
			int h = (r.height & 0x7f);// | (xFlipFlag  ? 0x40 : 0x00);

			os.write(w);
			os.write(h);

			for (int x = 0; x < r.width; ++x)
			{
				for (int y = 0; y < r.height; ++y)
				{
					int color = (data[x + (r.width * y)] & 0x0f) | 0x40;
					os.write(color);
				}
			}
		}

	}
	
	static final String INPUT_FILE  = FILENAME + ".gif";
	static final String OUTPUT_FILE = FILENAME + ".raw";

	static Rectangle[] spritePosList = new Rectangle[]
	{
		new Rectangle( 60, 0 , 12, 41), // z28
		new Rectangle( 87, 0 , 12, 32), // bug
		new Rectangle(100, 0 ,  9, 32), // cycle
		new Rectangle( 73, 0 , 13, 39), // vette
 		new Rectangle( 32, 0 , 13, 41), // road lord
		new Rectangle( 46, 0 , 13, 41), // switch blade
		new Rectangle(110, 0 , 16, 33), // tree
		new Rectangle(130, 8 , 5 , 7 ), // Bullet
		new Rectangle(128, 16, 7 , 14), // icon
		new Rectangle(128, 32, 22, 8),  // blade1
		new Rectangle(120, 40, 22, 8),  // blade1
//		new Rectangle( 17, 0, 14, 58), // enforcer
//		new Rectangle(  0, 0, 16, 63), // van
//		new Rectangle(127, 0, 10,  7), // blade
//		new Rectangle(138, 0, 15,  7), // shooter
	};

	static boolean[] spriteDoubleFlags = 
     {
		false,  // z28
		false,  // bug
		false,  // cycle
		false,  // vette
		false,  // road lord
		false,  // switch blade
		true,  // tree
		false, // bullet
		false, // icon
		false, // icon
		false, // icon
//		false,  // enforcer
//		false,  // van
//		false, // blade
//		false, // shooter
     };

//	static boolean[] spriteXFlipFlags = 
//	 {
//		false, // tree
//		false, // z28
//		false, // bug
//		false, // cycle
//		false, // road lord
////		false, // van
////		false, // enforcer
////		false, // switch blade
////		false, // vette
////		true,  // blade
////		true,  // shooter
//	 };
	
	static int[] rgbList = new int[16];
	static int colorIndex = 0;

	static List rawImages = new ArrayList();

	/**
	 * main
	 */
	public static void main(String[] args) throws Exception
	{
		
		System.out.println("Trying to convert file: " + INPUT_FILE);

		InputStream in;

		File file = new File(INPUT_FILE);

		if (file.isFile() == false)
		{
			URL url = FileToJavaString.class.getClassLoader().getResource(INPUT_FILE);

			if (url == null)
			{
				System.err.println("File not found: " + INPUT_FILE);
				help();
				return;
			}
			
			in = url.openStream();
		}
		else
		if (file.canRead() == false)
		{
			System.err.println("File not readable: " + INPUT_FILE);
			help();
			return;
		}
		else
		{
			in = new FileInputStream(file);
		}

		BufferedImage bi = ImageIO.read(in);
		
		// We want to guarantee that transparant black is first
		findColorIndex(0x00000000);

		for (int index = 0; index < spritePosList.length; index++)
		{
			Rectangle r = spritePosList[index];
			System.out.println("r = " + r);
			Rectangle r2 = r;
			if (spriteDoubleFlags[index])
			{
				r2 = new Rectangle(r);
				r2.width  <<= 1;
				r2.height <<= 1;
			}

			RawImage ri = new RawImage(r2, false, false);

			for (int x = 0; x < r.width; ++x)
			{
				for (int y = 0; y < r.height; ++y)
				{
					int rgb = bi.getRGB(r.x + x, r.y +y);
					int colorIndex = findColorIndex(rgb);

					if (spriteDoubleFlags[index])
					{
						ri.setColor((x << 1) + 0, (y << 1) + 0, colorIndex);
						ri.setColor((x << 1) + 1, (y << 1) + 0, colorIndex);
						ri.setColor((x << 1) + 0, (y << 1) + 1, colorIndex);
						ri.setColor((x << 1) + 1, (y << 1) + 1, colorIndex);
					}
					else
					{
						ri.setColor(x, y, colorIndex);
					}
				}
			}

			rawImages.add(ri);
		}

		System.out.println("Trying to write file: " + OUTPUT_FILE);

		FileOutputStream fo = new FileOutputStream(OUTPUT_FILE);
		
		System.out.println("Saving " + colorIndex + " Palette entries");

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

			int r = ((color >> 16) & 0xff) >> 1;
			
			r += (r == 0x7f ? -1 : (r == 0x00 ? 1 : 0));

			int g = ((color >>  8) & 0xff) >> 1;

			g += (g == 0x7f ? -1 : (g == 0x00 ? 1 : 0));
	
			int b = ((color      ) & 0xff) >> 1;

			b += (b == 0x7f ? -1 : (b == 0x00 ? 1 : 0));

			fo.write(r);
			fo.write(g);
			fo.write(b);
		}

		System.out.println("Saving " + rawImages.size() + " Sprites");
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
			System.out.println("adding color " + Integer.toHexString(rgb) + " at index " + result);
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
