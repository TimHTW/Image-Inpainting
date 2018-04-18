import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

public class RestoreImage {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		/*--START declaration zone--*/
		PrintWriter logfile = new PrintWriter("logfile.txt", "UTF-8");
		int counter = 0;
		int counter2 = 0;
		
		BufferedImage img = null;
		BufferedImage resultImg = null;
		try {
			img = ImageIO.read(new File("res\\img\\norway_java.png"));
			resultImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		double[][] I_redScreen = new double [width][height];
		double[][] I_greenScreen = new double [width][height];
		double[][] I_blueScreen = new double [width][height];
		
		boolean[][] paintedArea = new boolean [width][height];
		boolean[][] borderPixel = new boolean [width][height];
		
		double [][] Wred = new double [width][height];
		double [][] Wgreen = new double [width][height];
		double [][] Wblue = new double [width][height];
		
		double [][] Ix_red = new double [width][height];
		double [][] Iy_red = new double [width][height];
		double [][] Ix_green = new double [width][height];
		double [][] Iy_green = new double [width][height];
		double [][] Ix_blue = new double [width][height];
		double [][] Iy_blue = new double [width][height];
		double [][] Ix_rgb = new double [width][height];
		double [][] Iy_rgb = new double [width][height];
		
		double [][] Xscalar_red = new double [width][height];
		double [][] Xscalar_green = new double [width][height];
		double [][] Xscalar_blue = new double [width][height];
		
		double [][] Yscalar_red = new double [width][height];
		double [][] Yscalar_green = new double [width][height];
		double [][] Yscalar_blue = new double [width][height];
		
		double I_redOld;
		double I_greenOld;
		double I_blueOld;
		
		boolean finish = false;
		
		double t = 0;
		double z = 0;
		double tau = 0.01;
		double tau2 = 0.01;
		int alpha = 10;
		int v = 5;
		/*--END declaration zone--*/
		
		/*--START Fill Matrixes with values--*/
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				
				Color c = new Color(img.getRGB(i,j));
				I_redScreen[i][j] = c.getRed();
				I_greenScreen[i][j] = c.getGreen();
				I_blueScreen[i][j] = c.getBlue();
				
				Ix_red[i][j] = Iy_red[i][j] = Ix_green[i][j] = Iy_green[i][j] = Ix_blue[i][j] = Iy_blue[i][j] = Ix_rgb[i][j] = Iy_rgb[i][j] = 0;
				Xscalar_red[i][j] = Xscalar_green[i][j] = Xscalar_blue[i][j] = Yscalar_red[i][j] = Yscalar_green[i][j] = Yscalar_blue[i][j] = 0;
				
				borderPixel[i][j] = false;
				if (I_redScreen[i][j] == 255 && I_greenScreen[i][j] == 0 && I_blueScreen[i][j] == 0) {
					paintedArea[i][j] = true;
					//borderPixel[i][j] = true;
					borderPixel[i+1][j] = true;
					borderPixel[i-1][j] = true;
					borderPixel[i][j+1] = true;
					borderPixel[i][j-1] = true;
				}
			}
		}
		/*--END Fill Matrixes with values--*/
		
		/*--START first value for W--*/
		for(int i=1; i<width-1; i++) {
			for(int j=1; j<height-1; j++) {
				
				Wred[i][j] = (I_redScreen[i-1][j]-2*I_redScreen[i][j]+I_redScreen[i+1][j]) + (I_redScreen[i][j-1]-2*I_redScreen[i][j]+I_redScreen[i][j+1]);
				Wgreen[i][j] = (I_greenScreen[i-1][j]-2*I_greenScreen[i][j]+I_greenScreen[i+1][j]) + (I_greenScreen[i][j-1]-2*I_greenScreen[i][j]+I_greenScreen[i][j+1]);
				Wblue[i][j] = (I_blueScreen[i-1][j]-2*I_blueScreen[i][j]+I_blueScreen[i+1][j]) + (I_blueScreen[i][j-1]-2*I_blueScreen[i][j]+I_blueScreen[i][j+1]);
			}
		}
		
		//TODO Rand von W auf 0 setzen
		
		/*--START Euler outer loop--*/
		while(t<50) {
			for(int i=0; i<width; i++) {
				for(int j=0; j<height; j++) {
					if(paintedArea[i][j]) {
						
						I_redOld = I_redScreen[i][j];
						I_greenOld = I_greenScreen[i][j];
						I_blueOld = I_blueScreen[i][j];
						
						if(t == 0) {
							borderPixel[i][j] = false;
						}
						
						/*--START Upwind--*/
						Iy_red[i][j] = (I_redScreen[i][j-1] - I_redScreen[i][j+1]) / 2;
						Ix_red[i][j] = -(I_redScreen[i+1][j] - I_redScreen[i-1][j]) / 2;
						Iy_green[i][j] = (I_greenScreen[i][j-1] - I_greenScreen[i][j+1]) / 2;
						Ix_green[i][j] = -(I_greenScreen[i+1][j] - I_greenScreen[i-1][j]) / 2;
						Iy_blue[i][j] = (I_blueScreen[i][j-1] - I_blueScreen[i][j+1]) / 2;
						Ix_blue[i][j] = -(I_blueScreen[i+1][j] - I_blueScreen[i-1][j]) / 2;
						
						Ix_rgb[i][j] = (Ix_red[i][j] + Ix_green[i][j] + Ix_blue[i][j]) / 3;
						Iy_rgb[i][j] = (Iy_red[i][j] + Iy_green[i][j] + Iy_blue[i][j]) / 3;
						
						if (Ix_rgb[i][j] > 0) {
							Xscalar_red[i][j] = Ix_rgb[i][j] * ( (Wred[i][j]-Wred[i][j+1]) /2 );
							Xscalar_green[i][j] = Ix_rgb[i][j] * ( (Wgreen[i][j]-Wgreen[i][j+1]) /2 );
							Xscalar_blue[i][j] = Ix_rgb[i][j] * ( (Wblue[i][j]-Wblue[i][j+1]) /2 );
						} else if (Ix_rgb[i][j] < 0){
							Xscalar_red[i][j] = Ix_rgb[i][j] * ( (Wred[i][j-1]-Wred[i][j]) /2 );
							Xscalar_green[i][j] = Ix_rgb[i][j] * ( (Wgreen[i][j-1]-Wgreen[i][j]) /2 );
							Xscalar_blue[i][j] = Ix_rgb[i][j] * ( (Wblue[i][j-1]-Wblue[i][j]) /2 );
						}
						
						if (Iy_rgb[i][j] > 0) {
							Yscalar_red[i][j] = Iy_rgb[i][j] * ( (Wred[i][j]-Wred[i-1][j]) /2 );
							Yscalar_green[i][j] = Iy_rgb[i][j] * ( (Wgreen[i][j]-Wgreen[i-1][j]) /2 );
							Yscalar_blue[i][j] = Iy_rgb[i][j] * ( (Wblue[i][j]-Wblue[i-1][j]) /2 );
						} else if (Iy_rgb[i][j] < 0) {
							Yscalar_red[i][j] = Iy_rgb[i][j] * ( (Wred[i+1][j]-Wred[i][j]) /2 );
							Yscalar_green[i][j] = Iy_rgb[i][j] * ( (Wgreen[i+1][j]-Wgreen[i][j]) /2 );
							Yscalar_blue[i][j] = Iy_rgb[i][j] * ( (Wblue[i+1][j]-Wblue[i][j]) /2 );
						}
						
						
						double scalar_red = Xscalar_red[i][j] + Yscalar_red[i][j];
						double scalar_green = Xscalar_green[i][j] + Yscalar_green[i][j];
						double scalar_blue = Xscalar_blue[i][j] + Yscalar_blue[i][j];
						/*--END Upwind--*/
											
						Wred[i][j] = tau*(v*(Wred[i][j-1] - 4*Wred[i][j] + Wred[i][j+1] + Wred[i-1][j] + Wred[i+1][j]) - scalar_red ) + Wred[i][j];
						Wgreen[i][j] = tau*(v*(Wgreen[i][j-1] - 4*Wgreen[i][j] + Wgreen[i][j+1] + Wgreen[i-1][j] + Wgreen[i+1][j]) - scalar_green ) + Wgreen[i][j];
						Wblue[i][j] = tau*(v*(Wblue[i][j-1] - 4*Wblue[i][j] + Wblue[i][j+1] + Wblue[i-1][j] + Wblue[i+1][j]) - scalar_blue ) + Wblue[i][j];
						
											
					}
				}
			}
			
			/*--START Euler inner loop--*/
			while(z<50) {
				for(int m=0; m<width; m++) {
					for(int n=0; n<height; n++) {
						if(paintedArea[m][n]) {

							double redOld = I_redScreen[m][n];
							double greenOld = I_greenScreen[m][n];
							double blueOld = I_blueScreen[m][n];
							
							I_redScreen[m][n] = tau2*(alpha*( (I_redScreen[m][n-1] - 4*I_redScreen[m][n] + I_redScreen[m][n+1] + I_redScreen[m-1][n] + I_redScreen[m+1][n]) - Wred[m][n])) + redOld;
							I_greenScreen[m][n] = tau2*(alpha*( (I_greenScreen[m][n-1] - 4*I_greenScreen[m][n] + I_greenScreen[m][n+1] + I_greenScreen[m-1][n] + I_greenScreen[m+1][n]) - Wgreen[m][n])) + greenOld;
							I_blueScreen[m][n] = tau2*(alpha*( (I_blueScreen[m][n-1] - 4*I_blueScreen[m][n] + I_blueScreen[m][n+1] + I_blueScreen[m-1][n] + I_blueScreen[m+1][n]) - Wblue[m][n])) + blueOld;
							
							if(counter2 == 50 && m == 213 && n == 161) {
								System.out.println(tau2+"*("+alpha+"*( ("+I_redScreen[213][160]+"-"+4*I_redScreen[213][161] +"+"+ I_redScreen[213][162] +"+"+ I_redScreen[212][161] +"+"+ I_redScreen[214][161]+")"+"-"+Wred[213][161]+"))+"+I_redScreen[213][161]);
								System.out.println(I_redScreen[213][161]);
							}
							
							if(m == 213 && n == 161) {
								logfile.println(I_redScreen[m][n]);
							}	
							
						}
					}
				}
				z= z + tau2;
				counter2++;
			}
			/*--END Euler inner loop--*/
			
			logfile.close();
			
			/*--START W on border--*/
			for(int i=0; i<width; i++) {
				for(int j=0; j<height; j++) {
					if(borderPixel[i][j]) {
						
						Wred[i][j] = I_redScreen[i-1][j] - 4*I_redScreen[i][j] + I_redScreen[i+1][j] + I_redScreen[i][j-1] + I_redScreen[i][j+1];
						Wgreen[i][j] = I_greenScreen[i-1][j] - 4*I_greenScreen[i][j] + I_greenScreen[i+1][j] + I_greenScreen[i][j-1] + I_greenScreen[i][j+1];
						Wblue[i][j] = I_blueScreen[i-1][j] - 4*I_blueScreen[i][j] + I_blueScreen[i+1][j] + I_blueScreen[i][j-1] + I_blueScreen[i][j+1];
					}
				}
			}
			/*--END W on border--*/
			t = t + tau;
			counter++;
		}
		/*--END Euler outer loop--*/
		
		/*--START put screens together to create result image--*/
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				
				int r = (int)I_redScreen[i][j];
				int g = (int)I_greenScreen[i][j];
				int b = (int)I_blueScreen[i][j];
				
				if (r < 0) r=0;
				if (g < 0) g=0;
				if (b < 0) b=0;
				
				if (r > 255) r=255;
				if (g > 255) g=255;
				if (b > 255) b=255;
				
				Color newColor = new Color(r,g,b);
				resultImg.setRGB(i, j, newColor.getRGB());
			}
		}
		try {
			ImageIO.write(resultImg, "png", new File("result\\result.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*--END put screens together to create result image--*/
		
	}
	
}
