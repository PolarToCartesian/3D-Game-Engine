package engine;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Triangle {
	public Vector[] vertices;
	public Vector rotation;
	public Vector rotationMidPoint = new Vector(0, 0, 0);
	public Vector[] colors;
	
	public boolean doUseTexture = false;
	public int textureID = -1;

	public Triangle(Vector[] _vertices, Vector[] _colors) {
		this.vertices = _vertices;
		this.colors   = _colors;
		this.rotation = new Vector(0, 0, 0);
	}
	
	public Triangle(Vector[] _vertices, Vector[] _colors,int _textureID, boolean _doUseTexture) {
		this.vertices = _vertices;
		this.colors   = _colors;
		this.rotation = new Vector(0, 0, 0);
		
		this.textureID = _textureID;
		this.doUseTexture = _doUseTexture;
	}

	public static Vector getSurfaceNormal(Vector[] vertices) {
		//https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal

		Vector U = Vector.sub(vertices[1], vertices[0]);
		Vector V = Vector.sub(vertices[2], vertices[0]);

		double nX = U.y * V.z - U.z * V.y;
		double nY = U.z * V.x - U.x * V.z;
		double nZ = U.x * V.y - U.y * V.x;

		return Vector.normalize(new Vector(nX, nY, nZ));
	}
	
	public static void renderTexturedTriangle(Vector[] vertices, int textureID, BufferedImage pixelBuffer, int _screenWidth, int _screenHeight, float[] brightnesses, double[][] depthBuffer, ArrayList<Texture> textures) {
		// https://codeplea.com/triangular-interpolation
					
		// Step 1 : calculate denominator
					
		double denominator = (vertices[1].y - vertices[2].y) * (vertices[0].x - vertices[2].x) + (vertices[2].x - vertices[1].x) * (vertices[0].y - vertices[2].y);
					
		// Step 1.5 : precalculate values
					
		double preCalc1 = (vertices[1].y - vertices[2].y);
		double preCalc2 = (vertices[2].x - vertices[1].x);
		double preCalc3 = (vertices[2].y - vertices[0].y);
		double preCalc4 = (vertices[0].x - vertices[2].x);
		
		Vector t0 = vertices[0].copy();
		Vector t1 = vertices[1].copy();
		Vector t2 = vertices[2].copy();
		
		// left, right, top and bottom most points of triangle (for texturing (u;v))
					
		int left = (int) t0.x, right = (int) t0.x, top = (int) t0.y, bottom = (int) t0.y;
					
		if (t1.x < left)   { left   = (int) t1.x; } if (t2.x < left)   { left   = (int) t2.x; }
		if (t1.x > right)  { right  = (int) t1.x; } if (t2.x > right)  { left   = (int) t2.x; }
		if (t1.y < top)    { top    = (int) t1.y; } if (t2.y < top)    { top    = (int) t2.y; }
		if (t1.y > bottom) { bottom = (int) t1.y; } if (t2.y > bottom) { bottom = (int) t2.y; }
					
		int deltaX = right - left, deltaY = bottom - top;
		
		// https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling
					
		if (t0.y == t1.y && t0.y == t2.y) return;

		if (t0.y > t1.y) { Vector temp = t0.copy(); t0 = t1.copy(); t1 = temp.copy(); }
		if (t0.y > t2.y) { Vector temp = t0.copy(); t0 = t2.copy(); t2 = temp.copy(); }
		if (t1.y > t2.y) { Vector temp = t2.copy(); t2 = t1.copy(); t1 = temp.copy(); }
					
		int total_height = (int) (t2.y - t0.y);

		if (total_height >= _screenHeight) { total_height = _screenHeight - 1; }
					
		for (int i = 0; i < total_height + 1; i++) {
			boolean second_half = i > t1.y - t0.y || t1.y == t0.y;

			int segment_height = (int) (second_half ? t2.y - t1.y : t1.y - t0.y);

			double alpha = i / (double) total_height;
			double beta  = (i - (second_half ? t1.y - t0.y : 0)) / segment_height;

			Vector A = Vector.add( t0, Vector.mul( Vector.sub(t2, t0) , alpha ) );
			Vector B = second_half ?  Vector.add( t1, Vector.mul( Vector.sub(t2, t1) , beta ) ) : Vector.add( t0, Vector.mul( Vector.sub(t1, t0), beta ));

			if (A.x > B.x) { Vector temp = A.copy(); A = B.copy(); B = temp.copy(); }

			int xStart = (int) ((A.x > 0) ? A.x : 0);
			int xEnd   = (int) ((B.x < _screenWidth - 1) ? B.x : _screenWidth - 1);
										
			for (int x = xStart; x <= xEnd; x++) {
				int y = (int) (t0.y + i);

				// Check If Pixel Is Within the rendered area
				if (y >= 0 && y < _screenHeight) {
					// https://codeplea.com/triangular-interpolation
								
					double preCalc5 = (x - vertices[2].x);
					double preCalc6 = (y - vertices[2].y);
								
					double[] VertexPositionWeights = new double[] {
						(preCalc1 * preCalc5 + preCalc2 * preCalc6) / denominator, 
						(preCalc3 * preCalc5 + preCalc4 * preCalc6) / denominator, 
						0, 
					};
								
					VertexPositionWeights[2] = 1 - VertexPositionWeights[0] - VertexPositionWeights[1];
								
					double VertexPositionWeightSum = VertexPositionWeights[0] + VertexPositionWeights[1] + VertexPositionWeights[2];
								
					// Pixel Depth (w)
					double w = 0;
								
					// For every vertex
					for (int c = 0; c < 3; c++) {
						w += vertices[c].w * VertexPositionWeights[c];
					}
								
					w /= VertexPositionWeightSum;
								
					// Pixel Color
					Vector color = new Vector(0, 0, 0);
								
					// If the pixel is in front
					if (w < depthBuffer[x][y] || depthBuffer[x][y] == 0) {
						depthBuffer[x][y] = w;
									
						// Get (u;v) coordinates
						int u = (int) (((x - left) / (float) deltaX) * (textures.get(textureID).getWidth()  - 1));
						int v = (int) (((y - top)  / (float) deltaY) * (textures.get(textureID).getHeight() - 1));

						// Sample color from texture
						color = textures.get(textureID).sample(u, v);
													
						float brightness = 0f;
						
						for (int c = 0; c < 3; c++) {
							brightness += brightnesses[c] * VertexPositionWeights[c];
						}
						
						brightness /= VertexPositionWeightSum;
						
						color.mul(brightness);
						
						// Limit values between 0 and 255
						color.constrain();
									
						// Set the pixel to the right color
						pixelBuffer.setRGB(x, y, color.getColor().getRGB());
					}
				}							
			}
		}
	}
	
	public static void renderColoredTriangle(Vector[] vertices, BufferedImage pixelBuffer, int _screenWidth, int _screenHeight, Vector[] brightnedColors, double[][] depthBuffer) {
		// https://codeplea.com/triangular-interpolation
		
		// Step 1 : calculate denominator
		
		double denominator = (vertices[1].y - vertices[2].y) * (vertices[0].x - vertices[2].x) + (vertices[2].x - vertices[1].x) * (vertices[0].y - vertices[2].y);
		
		// Step 1.5 : precalculate values
					
		double preCalc1 = (vertices[1].y - vertices[2].y);
		double preCalc2 = (vertices[2].x - vertices[1].x);
		double preCalc3 = (vertices[2].y - vertices[0].y);
		double preCalc4 = (vertices[0].x - vertices[2].x);
		
		Vector t0 = vertices[0].copy();
		Vector t1 = vertices[1].copy();
		Vector t2 = vertices[2].copy();
					
		// https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling
					
		if (t0.y == t1.y && t0.y == t2.y) return;

		if (t0.y > t1.y) { Vector temp = t0.copy(); t0 = t1.copy(); t1 = temp.copy(); }
		if (t0.y > t2.y) { Vector temp = t0.copy(); t0 = t2.copy(); t2 = temp.copy(); }
		if (t1.y > t2.y) { Vector temp = t2.copy(); t2 = t1.copy(); t1 = temp.copy(); }
					
		int total_height = (int) (t2.y - t0.y);

		if (total_height >= _screenHeight) { total_height = _screenHeight - 1; }
					
		for (int i = 0; i < total_height; i++) {
			boolean second_half = i > t1.y - t0.y || t1.y == t0.y;

			int segment_height = (int) (second_half ? t2.y - t1.y : t1.y - t0.y);

			double alpha = i / (double) total_height;
			double beta  = (i - (second_half ? t1.y - t0.y : 0)) / segment_height;

			Vector A = Vector.add( t0, Vector.mul( Vector.sub(t2, t0) , alpha ) );
			Vector B = second_half ? Vector.add( t1, Vector.mul( Vector.sub(t2, t1) , beta ) ) : Vector.add( t0, Vector.mul( Vector.sub(t1, t0), beta ));

			if (A.x > B.x) { Vector temp = A.copy(); A = B.copy(); B = temp.copy(); }

			int xStart = (int) ((A.x > 0) ? A.x : 0);
			int xEnd   = (int) ((B.x < _screenWidth - 1) ? B.x : _screenWidth - 1);
											
			boolean doBreak = false;
						
			for (int x = xStart; x <= xEnd; x++) {
				int y = (int) (t0.y + i);

				if (y >= 0 && y < _screenHeight) {
					// https://codeplea.com/triangular-interpolation
								
					double preCalc5 = (x - vertices[2].x);
					double preCalc6 = (y - vertices[2].y);
						
					double[] VertexPositionWeights = new double[] {
						(preCalc1 * preCalc5 + preCalc2 * preCalc6) / denominator, 
						(preCalc3 * preCalc5 + preCalc4 * preCalc6) / denominator, 
						0, 
					};
								
					VertexPositionWeights[2] = 1 - VertexPositionWeights[0] - VertexPositionWeights[1];
								
					double VertexPositionWeightSum = VertexPositionWeights[0] + VertexPositionWeights[1] + VertexPositionWeights[2];
								
					// Pixel Depth (w)
					double w = 0;
								
					// For every vertex
					for (int c = 0; c < 3; c++) {
						w += vertices[c].w * VertexPositionWeights[c];
					}
								
					w /= VertexPositionWeightSum;
								
					// Pixel Color
					Vector color = new Vector(0, 0, 0);
								
					// If the pixel is in front
					if (w < depthBuffer[x][y] || depthBuffer[x][y] == 0) {
						depthBuffer[x][y] = w;
									
						// Triangulate the pixel color (pun intended)
						// For every vertex
						for (int c = 0; c < 3; c++) {
							color.add(Vector.mul(brightnedColors[c], VertexPositionWeights[c]));
						}
						
						color.div(VertexPositionWeightSum);
									
						// Limit values between 0 and 255
						color.constrain();
										
						// Set the pixel to the right color
						pixelBuffer.setRGB(x, y, color.getColor().getRGB());
					}
				} else {
					doBreak = true;
					break;
				}
			}
						
			if (doBreak) break;
		}
	}	
}
