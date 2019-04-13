package engine;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ConcurrentModificationException;

public abstract class Engine {
	private int screenWidth, screenHeight;
	private int fov;
	private int fps;
	private String screenTitle;
	
	private JFrame frame;
	private Panel  panel;

	private boolean mousePressed = false;

	private Point mousePosition = new Point(0, 0);

	private double[][] perspectiveMatrix;
	private double[][] depthBuffer;
	
	private ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	private ArrayList<Light>    lights    = new ArrayList<Light>();
	private ArrayList<Texture>  textures  = new ArrayList<Texture>();

	private Color backgroundColor = Color.BLACK;
	
	private BufferedImage pixelBuffer;
	
	public Engine(int _screenWidth, int _screenHeight, String _screenTitle, int _fov, int _fps) {
		this.screenWidth  = _screenWidth;
		this.screenHeight = _screenHeight;
		this.screenTitle  = _screenTitle;
		this.fov = _fov;
		this.fps = _fps;

		this.perspectiveMatrix = MatrixOperations.createPerspectiveMatrix(this.screenWidth, this.screenHeight, this.fov, 0.01f, 1000f);
		
		this.panel = new Panel();
		this.frame = new JFrame(this.screenTitle);

		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(this.screenWidth, this.screenHeight);
		this.frame.addKeyListener(new KeyBoardListener());
		this.frame.setLocationRelativeTo(null);
		this.frame.setResizable(false);
		this.frame.setFocusable(true);
		this.frame.add(panel);
		this.frame.addMouseListener(new MouseListenerHandler());
		this.frame.addMouseMotionListener(new MouseMotionHandler());		
		this.panel.setSize(this.screenWidth, this.screenHeight);
	}

	protected final void run() {
		this.frame.setVisible(true);
		
		long defaultWaitTimeMs = (long) (1 / (double) this.fps * 1000);
		
		while (true) {
			long beforeMs = System.currentTimeMillis();

			// Render
			this.frame.repaint();

			long waitTimeMs = defaultWaitTimeMs - (System.currentTimeMillis() - beforeMs);

			// Update
			this.update(waitTimeMs);

			// Clean Up
			this.cleanUp();

			if (waitTimeMs > 0) {
				try {
					Thread.sleep(waitTimeMs);
				} catch (java.lang.InterruptedException e) {
					System.out.println("Error While Waiting");
				}
			}
		}
		
	}

	private final void cleanUp() {
		// Prevent Overflow
		Camera.rotation.x %= (2 * Math.PI);
		Camera.rotation.y %= (2 * Math.PI);
		Camera.rotation.z %= (2 * Math.PI);
	}

	// Setters

	protected final void setScreenWidth(int _screenWidth) {
		this.screenWidth = _screenWidth;

		this.frame.setSize(this.screenWidth, this.screenHeight);
		this.panel.setSize(this.screenWidth, this.screenHeight);
		
		this.perspectiveMatrix = MatrixOperations.createPerspectiveMatrix(this.screenWidth, this.screenHeight, this.fov, 0.01f, 1000f);
	}

	protected final void setScreenHeight(int _screenHeight) {
		this.screenHeight = _screenHeight;

		this.frame.setSize(this.screenWidth, this.screenHeight);
		this.panel.setSize(this.screenWidth, this.screenHeight);
		
		this.perspectiveMatrix = MatrixOperations.createPerspectiveMatrix(this.screenWidth, this.screenHeight, this.fov, 0.01f, 1000f);
	}

	protected final void setScreenTitle(String _screenTitle) {
		this.screenTitle = _screenTitle;

		this.frame.setTitle(this.screenTitle);
	}

	// Getters

	protected final int getScreenWidth()    { return this.screenWidth;  }
	protected final int getScreenHeight()   { return this.screenHeight; }
	protected final String getScreenTitle() { return this.screenTitle;  }

	protected final boolean isMousePressed() { return this.mousePressed; }

	protected final Point getMousePosition() { return this.mousePosition; }
	
	protected final int addTriangle(Triangle _triangle) {
		triangles.add(_triangle);
		
		return triangles.size() - 1;
	}
	
	protected final int[] addTriangles(Triangle[] _triangles) {
		int[] indices = new int[_triangles.length];
		
		for (int i = 0; i < _triangles.length; i++) {			
			indices[i] = addTriangle(_triangles[i]);
		}
				
		return indices;
	}
	
	protected final void removeTriangle(int _index) {
		triangles.set(_index, null);
	}
	
	protected final void setTriangle(int _index, Triangle _triangle) {
		triangles.set(_index, _triangle);
	}
	
	protected final Triangle getTriangle(int _index) {
		return triangles.get(_index);
	}

	protected final int addLight(Light _light) {
		lights.add(_light);
		
		return lights.size() - 1;
	}
	
	protected final int[] addLights(Light[] _lights) {
		int[] indices = new int[_lights.length];
		
		for (int i = 0; i < _lights.length; i++) {			
			indices[i] = addLight(_lights[i]);
		}
				
		return indices;
	}
	
	protected final void removeLight(int _index) {
		lights.set(_index, null);
	}
	
	protected final void setLight(int _index, Light _light) {
		lights.set(_index, _light);
	}
	
	protected final Light getLight(int _index) {
		return lights.get(_index);
	}
	
	protected final int addTexture(Texture _texture) {
		textures.add(_texture);
		
		return textures.size() - 1;
	}
	
	protected final int[] addTextures(Texture[] _texture) {
		int[] indices = new int[_texture.length];
		
		for (int i = 0; i < _texture.length; i++) {			
			indices[i] = addTexture(_texture[i]);
		}
				
		return indices;
	}
	
	protected final void removeTexture(int _index) {
		textures.set(_index, null);
	}
	
	protected final void setTexture(int _index, Texture _texture) {
		textures.set(_index, _texture);
	}
	
	protected final Texture getTexture(int _index) {
		return textures.get(_index);
	}
	
	protected final void setBackgroundColor(Color _color) {
		this.backgroundColor = _color;
	}
	
	protected final void setBackgroundColor(Vector _color) {
		this.backgroundColor = Vector.getColor(_color);
	}
	
	protected final void loadModel(String fileLocation, boolean randomColors) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
			String line;
			ArrayList<Vector> vertices = new ArrayList<Vector>();
			
			while ((line = br.readLine()) != null) {
				String[] elements = line.split(" ");

				switch (elements[0]) {
					case "v":						
						int startIndex = elements.length == 4 ? 1 : 2;
						
						Vector vertex = new Vector((double) Double.parseDouble(elements[startIndex]),
												   (double) Double.parseDouble(elements[startIndex+1]),
									               (double) Double.parseDouble(elements[startIndex+2]));

						vertices.add(vertex);
						break;

					case "f":
						Vector[] colors = new Vector[3];
						
						Vector v1 = vertices.get((Integer.parseInt(elements[1].split("/")[0]) - 1));
						Vector v2 = vertices.get((Integer.parseInt(elements[2].split("/")[0]) - 1));
						Vector v3 = vertices.get((Integer.parseInt(elements[3].split("/")[0]) - 1));

						Vector[] positions = new Vector[] {v1, v2, v3};
						
						for (int i = 0; i < 3; i++) {
							if (randomColors) {
								colors[i] = new Vector(ThreadLocalRandom.current().nextInt(0, 256), ThreadLocalRandom.current().nextInt(0, 256), ThreadLocalRandom.current().nextInt(0, 256));
							} else {
								colors[i] = new Vector(255, 255, 255);
							}
						}

						triangles.add(new Triangle(positions, colors, -1, false));
						break;
				}
			}
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		System.out.println("Loaded " + triangles.size() + " triangles (" + triangles.size() * 3 + " vertices)");
	}

	// To Be Overridden

	protected void update(long deltaTime) {}
	
	protected boolean onKeyDown(int keyCode) { return false; } // Return True if input is used, and false if it is not.
	
	protected void onMouseClick() {}
	
	protected void onMousePress() {}
	
	protected void onMouseRelease() {}

	// Classes

	private final static class MatrixOperations {
		static double[][] createPerspectiveMatrix(double _width, double _height, double _fov, double _zNear, double _zFar) {
			double aspectRatio = _height / (double) _width;
			double a = (double) (1.f / Math.tan(_fov * 0.5 * 180.f / 3.1415926));

			if (a > 0) a = -a;

			return new double[][] {
					{aspectRatio * a, 0, 0,                                    0},
					{0,               a, 0,                                    0},
					{0,               0, -_zFar / (_zFar - _zNear),            1},
					{0,               0, (-_zFar * _zNear) / (_zFar - _zNear), 0}
			};
		}

		static Vector multiplyMatrixByVector(double[][] _m, Vector _v) {
			double x = _m[0][0] * _v.x + _m[1][0] * _v.y + _m[2][0] * _v.z + _m[3][0];
			double y = _m[0][1] * _v.x + _m[1][1] * _v.y + _m[2][1] * _v.z + _m[3][1];
			double z = _m[0][2] * _v.x + _m[1][2] * _v.y + _m[2][2] * _v.z + _m[3][2];
			double w = _m[0][3] * _v.x + _m[1][3] * _v.y + _m[2][3] * _v.z + _m[3][3];

			return new Vector(x, y, z, w);
		}

		static double[][] getRotationXMatrix(double cosX, double sinX) {
			return new double[][] {
				{1, 0,     0,    0},
				{0, cosX,  sinX, 0},
				{0, -sinX, cosX, 0},
				{0, 0,     0,    1}
			};
		}

		static double[][] getRotationYMatrix(double cosY, double sinY) {
			return new double[][] {
				{cosY, 0, -sinY, 0},
				{0,    1, 0,     0},
				{sinY, 0, cosY,  0},
				{0,    0, 0,     1}
			};
		}

		static double[][] getRotationZMatrix(double cosZ, double sinZ) {
			return new double[][] {
				{cosZ,  sinZ, 0, 0},
				{-sinZ, cosZ, 0, 0},
				{0,     0,    1, 0},
				{0,     0,    0, 1}
			};
		}

	}

	protected static final class Vector {
		public double x, y, z, w = 1;

		public Vector() {this.x = 0; this.y = 0; this.z = 0; }
		public Vector(double _x, double _y) { this.x = _x; this.y = _y; this.z = 0; }
		public Vector(double _x, double _y, double _z) { this.x = _x; this.y = _y; this.z = _z; }
		public Vector(double _x, double _y, double _z, double _w) { this.x = _x; this.y = _y; this.z = _z; this.w = _w; }

		public Vector(Color _color) { this.x = _color.getRed(); this.y = _color.getGreen(); this.z = _color.getBlue(); }
		
		public Vector copy() { return new Vector(this.x, this.y, this.z); }

		public void add(double _x, double _y, double _z) { this.x += _x; this.y += _y; this.z += _z; }
		public void sub(double _x, double _y, double _z) { this.x -= _x; this.y -= _y; this.z -= _z; }
		public void mul(double _x, double _y, double _z) { this.x *= _x; this.y *= _y; this.z *= _z; }
		public void div(double _x, double _y, double _z) { this.x /= _x; this.y /= _y; this.z /= _z; }

		public void add(double _n) { this.add(_n, _n, _n); }
		public void sub(double _n) { this.sub(_n, _n, _n); }
		public void mul(double _n) { this.mul(_n, _n, _n); }
		public void div(double _n) { this.div(_n, _n, _n); }

		public void add(Vector _v) { this.add(_v.x, _v.y, _v.z); }
		public void sub(Vector _v) { this.sub(_v.x, _v.y, _v.z); }
		public void mul(Vector _v) { this.mul(_v.x, _v.y, _v.z); }
		public void div(Vector _v) { this.div(_v.x, _v.y, _v.z); }

		public Color getColor() { return new Color( (int) this.x, (int) this.y, (int) this.z ); }
		
		public void constrain() { // 0-255
			x = (x < 0) ? 0 : ( (x > 255) ? 255 : x );
			y = (y < 0) ? 0 : ( (y > 255) ? 255 : y );
			z = (z < 0) ? 0 : ( (z > 255) ? 255 : z );
		}
		
		public double getLength() { return (double) Math.sqrt( this.x * this.x + this.y * this.y + this.z * this.z ); }
		public void  normalize() { this.div(this.getLength()); }

		public static Vector normalize(Vector _v) { Vector result = _v.copy(); result.normalize(); return result; }

		public static double dotProduct(Vector _a, Vector _b) { return _a.x * _b.x + _a.y * _b.y + _a.z * _b.z; }

		//TBI - To Be Improved

		public static Vector add(Vector _v, double _n) { Vector result = _v.copy(); result.add(_n); return result; }
		public static Vector sub(Vector _v, double _n) { Vector result = _v.copy(); result.sub(_n); return result; }
		public static Vector mul(Vector _v, double _n) { Vector result = _v.copy(); result.mul(_n); return result; }
		public static Vector div(Vector _v, double _n) { Vector result = _v.copy(); result.div(_n); return result; }

		public static Vector add(Vector _a, Vector _b) { Vector result = _a.copy(); result.add(_b); return result; }
		public static Vector sub(Vector _a, Vector _b) { Vector result = _a.copy(); result.sub(_b); return result; }
		public static Vector mul(Vector _a, Vector _b) { Vector result = _a.copy(); result.mul(_b); return result; }
		public static Vector div(Vector _a, Vector _b) { Vector result = _a.copy(); result.div(_b); return result; }
	
		public static Color  getColor(Vector _v) { return _v.getColor(); } 
		public static Vector getConstrained(Vector _v) { Vector result = _v.copy(); result.constrain(); return result; }
	}

	protected static class Camera {
		public static Vector position = new Vector(0, 1, -5);
		public static Vector rotation = new Vector(0, 0, 0);
	}

	protected static class Triangle {
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
		
	}

	public static class Light {
		public Vector position;
		public Vector color;

		public double intensity;

		public Light(Vector _position, Vector _color, double _intensity) {
			this.position = _position;
			this.color = _color;
			this.intensity = _intensity;
		}
	}
	
	public static class Texture {
		public BufferedImage textureImage;
		
		public Texture(String _path) {
			try {
				this.textureImage = ImageIO.read(new File(_path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public Texture(BufferedImage _textureImage) {
			this.textureImage = _textureImage;
		}
		
		public Vector sample(int _u, int _v) {
			if (_u >= 0 && _u < this.textureImage.getWidth()) {
				if (_v >= 0 && _v < this.textureImage.getHeight()) {
					return new Vector(new Color(textureImage.getRGB(_u, _v)));
				}
			}
						
			return new Vector(0, 0, 0);
		}
	};

	private class KeyBoardListener implements KeyListener {
		public void keyTyped(KeyEvent e) {}

		public void keyPressed(KeyEvent e) {
			// Give the input to the user

			// If the user did not use the input, then perform the default operation for that key
			if (!onKeyDown(e.getKeyCode())) {
				switch (e.getKeyChar()) {
					case 'w':
						Camera.position.z += 0.1f;
						break;
					case 's':
						Camera.position.z -= 0.1f;
						break;
					case 'a':
						Camera.position.x -= 0.1f;
						break;
					case 'd':
						Camera.position.x += 0.1f;
						break;
					case ' ':
						Camera.position.y += 0.1f;
						break;
				}

				switch (e.getKeyCode()) {
					case 16:
						Camera.position.y -= 0.1f;
						break;
					case 37:
						Camera.rotation.y -= 0.1f;
						break;
					case 39:
						Camera.rotation.y += 0.1f;
						break;
					case 38:
						Camera.rotation.x -= 0.1f;
						break;
					case 40:
						Camera.rotation.x += 0.1f;
						break;
				}
			}
		}

		public void keyReleased(KeyEvent e) { }
	}

	private class MouseMotionHandler implements MouseMotionListener {
		public void mouseDragged(MouseEvent me) {}

		public void mouseMoved(MouseEvent me) {			
			mousePosition.x = me.getLocationOnScreen().x - frame.getX();
			mousePosition.y = me.getLocationOnScreen().y - frame.getY();		
		}
	}

	private class MouseListenerHandler implements MouseListener {
		public void mouseClicked(MouseEvent e)  { onMouseClick(); }
		public void mousePressed(MouseEvent e)  { onMousePress(); mousePressed = true; }
		public void mouseEntered(MouseEvent e)  {}
		public void mouseExited(MouseEvent e)   {}
		public void mouseReleased(MouseEvent e) { onMouseRelease(); mousePressed = false; }
	}
	
	private class Panel extends JPanel {
		private static final long serialVersionUID = 1L;

		private boolean isRotatedTriangleFacingCamera(Vector[] _rotatedVertices, Vector _surfaceNormal) {
			Vector triangleRotatedCenter = Vector.div(Vector.add(Vector.add(_rotatedVertices[0], _rotatedVertices[1]), _rotatedVertices[2]), 3);
			Vector triangleToCamera = Vector.normalize(Vector.sub(Camera.position, triangleRotatedCenter));

			return (Vector.dotProduct(_surfaceNormal, triangleToCamera) > 0.f);
		}

		private float[] getIllimunation(Vector[] _rotatedVertices, Vector _surfaceNormal) {
			float[] brightnesses = new float[3];
			
			for (int i = 0; i < 3; i++) {				
				// For Every Light
				for (Light light : lights) {
					// No Need To Translate The Light's position and the triangle's position because it would cancel out
					Vector vertexToLight = Vector.sub(light.position, _rotatedVertices[i]);
	
					double distance = (double) vertexToLight.getLength();
	
					vertexToLight.normalize();
	
					double dotProductTriangleAndLight = Vector.dotProduct(vertexToLight, _surfaceNormal);
						
					if (dotProductTriangleAndLight > 0f) {
						dotProductTriangleAndLight = Math.abs(dotProductTriangleAndLight);
					} else {
						dotProductTriangleAndLight = 0f;
					}
									
					brightnesses[i] += (dotProductTriangleAndLight * light.intensity) / (distance * distance);
				}
								
				if (brightnesses[i] > 1f) brightnesses[i] = 1f;
			}
			
			return brightnesses;
		}
		
		private Vector[] getColorsWIllumination(Vector[] _rotatedVertices, Vector _surfaceNormal, Vector[] _colors) {
			// Total Brightness
			Vector[] colorsWlighting = new Vector[3];
			
			float[] brightnesses = getIllimunation(_rotatedVertices, _surfaceNormal);

			// For Each Vertex
			for (int i = 0; i < 3; i++) {
				Vector vertexColor = Vector.mul(_colors[i], brightnesses[i]);
				
				// Limit color range (0-255)
				
				vertexColor.constrain();
				
				colorsWlighting[i] = vertexColor;
			}
			
			return colorsWlighting;
		}

		public void renderTexturedTriangle(Vector[] vertices, int textureID, BufferedImage pixelBuffer, int _screenWidth, int _screenHeight, float[] brightnesses) {
			// https://codeplea.com/triangular-interpolation
			
			Vector[] nonMutatedVertices = vertices.clone();
			
			// Step 1 : calculate denominator
						
			double denominator = (vertices[1].y - vertices[2].y) * (vertices[0].x - vertices[2].x) + (vertices[2].x - vertices[1].x) * (vertices[0].y - vertices[2].y);
						
			// Step 1.5 : precalculate values
						
			double preCalc1 = (vertices[1].y - vertices[2].y);
			double preCalc2 = (vertices[2].x - vertices[1].x);
			double preCalc3 = (vertices[2].y - vertices[0].y);
			double preCalc4 = (vertices[0].x - vertices[2].x);
						
			// left, right, top and bottom most points of triangle (for texturing)
						
			int left = (int) vertices[0].x, right = (int) vertices[0].x, top = (int) vertices[0].y, bottom = (int) vertices[0].y;
						
			if (vertices[1].x < left)   { left   = (int) vertices[1].x; } if (vertices[2].x < left)   { left   = (int) vertices[2].x; }
			if (vertices[1].x > right)  { right  = (int) vertices[1].x; } if (vertices[2].x > right)  { left   = (int) vertices[2].x; }
			if (vertices[1].y < top)    { top    = (int) vertices[1].y; } if (vertices[2].y < top)    { top    = (int) vertices[2].y; }
			if (vertices[1].y > bottom) { bottom = (int) vertices[1].y; } if (vertices[2].y > bottom) { bottom = (int) vertices[2].y; }
						
			int deltaX = right - left, deltaY = bottom - top;
			
			// https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling
						
			if (vertices[0].y == vertices[1].y && vertices[0].y == vertices[2].y) return;

			if (vertices[0].y > vertices[1].y) { Vector temp = vertices[0].copy(); vertices[0] = vertices[1].copy(); vertices[1] = temp.copy(); }
			if (vertices[0].y > vertices[2].y) { Vector temp = vertices[0].copy(); vertices[0] = vertices[2].copy(); vertices[2] = temp.copy(); }
			if (vertices[1].y > vertices[2].y) { Vector temp = vertices[2].copy(); vertices[2] = vertices[1].copy(); vertices[1] = temp.copy(); }
						
			int total_height = (int) (vertices[2].y - vertices[0].y);

			if (total_height >= _screenHeight) { total_height = _screenHeight - 1; }
						
			for (int i = 0; i < total_height; i++) {
				boolean second_half = i > vertices[1].y - vertices[0].y || vertices[1].y == vertices[0].y;

				int segment_height = (int) (second_half ? vertices[2].y - vertices[1].y : vertices[1].y - vertices[0].y);

				double alpha = i / (double) total_height;
				double beta  = (i - (second_half ? vertices[1].y - vertices[0].y : 0)) / segment_height;

				Vector A = Vector.add( vertices[0], Vector.mul( Vector.sub(vertices[2], vertices[0]) , alpha ) );
				Vector B = second_half ?  Vector.add( vertices[1], Vector.mul( Vector.sub(vertices[2], vertices[1]) , beta ) ) : Vector.add( vertices[0], Vector.mul( Vector.sub(vertices[1], vertices[0]), beta ));

				if (A.x > B.x) { Vector temp = A.copy(); A = B.copy(); B = temp.copy(); }

				int xStart = (int) ((A.x > 0) ? A.x : 0);
				int xEnd   = (int) ((B.x < _screenWidth - 1) ? B.x : _screenWidth - 1);
											
				for (int x = xStart; x <= xEnd; x++) {
					int y = (int) (vertices[0].y + i);

					if (y >= 0 && y < _screenHeight) {
						// https://codeplea.com/triangular-interpolation
									
						double preCalc5 = (x - nonMutatedVertices[2].x);
						double preCalc6 = (y - nonMutatedVertices[2].y);
									
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
							int u = (int) (((x - left) / (float) deltaX) * (textures.get(textureID).textureImage.getWidth()  - 1));
							int v = (int) (((y - top)  / (float) deltaY) * (textures.get(textureID).textureImage.getHeight() - 1));

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

		public void renderColoredTriangle(Vector[] vertices, BufferedImage pixelBuffer, int _screenWidth, int _screenHeight, Vector[] brightnedColors) {
			// https://codeplea.com/triangular-interpolation
			
			Vector[] nonMutatedVertices = vertices.clone();
			
			// Step 1 : calculate denominator
			
			double denominator = (vertices[1].y - vertices[2].y) * (vertices[0].x - vertices[2].x) + (vertices[2].x - vertices[1].x) * (vertices[0].y - vertices[2].y);
			
			// Step 1.5 : precalculate values
						
			double preCalc1 = (vertices[1].y - vertices[2].y);
			double preCalc2 = (vertices[2].x - vertices[1].x);
			double preCalc3 = (vertices[2].y - vertices[0].y);
			double preCalc4 = (vertices[0].x - vertices[2].x);
						
			// https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling
						
			if (vertices[0].y == vertices[1].y && vertices[0].y == vertices[2].y) return;

			if (vertices[0].y > vertices[1].y) { Vector temp = vertices[0].copy(); vertices[0] = vertices[1].copy(); vertices[1] = temp.copy(); }
			if (vertices[0].y > vertices[2].y) { Vector temp = vertices[0].copy(); vertices[0] = vertices[2].copy(); vertices[2] = temp.copy(); }
			if (vertices[1].y > vertices[2].y) { Vector temp = vertices[2].copy(); vertices[2] = vertices[1].copy(); vertices[1] = temp.copy(); }
						
			int total_height = (int) (vertices[2].y - vertices[0].y);

			if (total_height >= _screenHeight) { total_height = _screenHeight - 1; }
						
			for (int i = 0; i < total_height; i++) {
				boolean second_half = i > vertices[1].y - vertices[0].y || vertices[1].y == vertices[0].y;

				int segment_height = (int) (second_half ? vertices[2].y - vertices[1].y : vertices[1].y - vertices[0].y);

				double alpha = i / (double) total_height;
				double beta  = (i - (second_half ? vertices[1].y - vertices[0].y : 0)) / segment_height;

				Vector A = Vector.add( vertices[0], Vector.mul( Vector.sub(vertices[2], vertices[0]) , alpha ) );
				Vector B = second_half ? Vector.add( vertices[1], Vector.mul( Vector.sub(vertices[2], vertices[1]) , beta ) ) : Vector.add( vertices[0], Vector.mul( Vector.sub(vertices[1], vertices[0]), beta ));

				if (A.x > B.x) { Vector temp = A.copy(); A = B.copy(); B = temp.copy(); }

				int xStart = (int) ((A.x > 0) ? A.x : 0);
				int xEnd   = (int) ((B.x < _screenWidth - 1) ? B.x : _screenWidth - 1);
							
				boolean doBreak = false;
							
				for (int x = xStart; x <= xEnd; x++) {
					int y = (int) (vertices[0].y + i);

					if (y >= 0 && y < _screenHeight) {
						// https://codeplea.com/triangular-interpolation
									
						double preCalc5 = (x - nonMutatedVertices[2].x);
						double preCalc6 = (y - nonMutatedVertices[2].y);
							
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
		
		public void paintComponent(Graphics g) {
			pixelBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
			
			Graphics gc = pixelBuffer.getGraphics(); 
			
			gc.setColor(backgroundColor);
			gc.fillRect(0, 0, screenWidth, screenHeight);
			
			depthBuffer = new double[screenWidth][screenHeight];
			
			try {
				double[][] cameraRotationXMatrix = MatrixOperations.getRotationXMatrix((double) Math.cos(-Camera.rotation.x), (double) Math.sin(-Camera.rotation.x));
				double[][] cameraRotationYMatrix = MatrixOperations.getRotationYMatrix((double) Math.cos(-Camera.rotation.y), (double) Math.sin(-Camera.rotation.y));
				double[][] cameraRotationZMatrix = MatrixOperations.getRotationZMatrix((double) Math.cos(-Camera.rotation.z), (double) Math.sin(-Camera.rotation.z));

				for (Triangle triangle : triangles) {
					// If the triangle doesn't exist anymore, render the next one
					if (triangle == null) continue;
					
					Vector[] rotatedVertices = new Vector[3];

					// Rotation Matrices : https://www.siggraph.org/education/materials/HyperGraph/modeling/mod_tran/3drota.htm#Z

					double[][] triangleRotationXMatrix = MatrixOperations.getRotationXMatrix((double) Math.cos(triangle.rotation.x), (double) Math.sin(triangle.rotation.x));
					double[][] triangleRotationYMatrix = MatrixOperations.getRotationYMatrix((double) Math.cos(triangle.rotation.y), (double) Math.sin(triangle.rotation.y));
					double[][] triangleRotationZMatrix = MatrixOperations.getRotationZMatrix((double) Math.cos(triangle.rotation.z), (double) Math.sin(triangle.rotation.z));

					// Rotate each Vertex
					for (int i = 0; i < 3; i++) {
						rotatedVertices[i] = triangle.vertices[i].copy();

						// Object Rotation
						rotatedVertices[i].sub(triangle.rotationMidPoint);
						rotatedVertices[i] = MatrixOperations.multiplyMatrixByVector(triangleRotationXMatrix, rotatedVertices[i]);
						rotatedVertices[i] = MatrixOperations.multiplyMatrixByVector(triangleRotationYMatrix, rotatedVertices[i]);
						rotatedVertices[i] = MatrixOperations.multiplyMatrixByVector(triangleRotationZMatrix, rotatedVertices[i]);
						rotatedVertices[i].add(triangle.rotationMidPoint);

						// Camera Rotation
						rotatedVertices[i].sub(Camera.position);
						rotatedVertices[i] = MatrixOperations.multiplyMatrixByVector(cameraRotationXMatrix, rotatedVertices[i]);
						rotatedVertices[i] = MatrixOperations.multiplyMatrixByVector(cameraRotationYMatrix, rotatedVertices[i]);
						rotatedVertices[i] = MatrixOperations.multiplyMatrixByVector(cameraRotationZMatrix, rotatedVertices[i]);
						rotatedVertices[i].add(Camera.position);
					}

					Vector surfaceNormal = Triangle.getSurfaceNormal(rotatedVertices);

					// Attempt to render it if it is facing the camera
					if (this.isRotatedTriangleFacingCamera(rotatedVertices, surfaceNormal)) {
						Vector[] manipulatedVertices = new Vector[3];
						boolean triangleBehindCamera = false;

						// For Every Vertex
						for (int i = 0; i < 3; i++) {
							// Translate by the camera's position
							manipulatedVertices[i] = Vector.sub(rotatedVertices[i].copy(), Camera.position);

							// Apply Perspective part 1
							manipulatedVertices[i] = MatrixOperations.multiplyMatrixByVector(perspectiveMatrix, manipulatedVertices[i]);

							// Apply Perspective part 2 : divide (x, y, z) components by w
							if (manipulatedVertices[i].w > 0) {
								manipulatedVertices[i].div(manipulatedVertices[i].w);
							} else {
								// Don't bother to render the triangle if it is behind the camera
								triangleBehindCamera = true;
								break;
							}

							// World To Screen (between -1 and 1 to between 0 and 255)
							manipulatedVertices[i].x = ((manipulatedVertices[i].x - 1f) / -2f) * screenWidth;
							manipulatedVertices[i].y = ((manipulatedVertices[i].y + 1f) /  2f) * screenHeight;
						}

						// If The Triangle Is In Front Of The Camera
						if (!triangleBehindCamera) {
							if (triangle.doUseTexture) {
								float[] brightnesses = getIllimunation(rotatedVertices, surfaceNormal);
								renderTexturedTriangle(manipulatedVertices, triangle.textureID, pixelBuffer, screenWidth, screenHeight, brightnesses);
							} else {
								Vector[] brightenedColors = getColorsWIllumination(rotatedVertices, surfaceNormal, triangle.colors);
								renderColoredTriangle(manipulatedVertices, pixelBuffer, screenWidth, screenHeight, brightenedColors);
							}
						}
					}
				}
						
			} catch (ConcurrentModificationException e) {
				e.printStackTrace();
			}
			
			g.drawImage(pixelBuffer, 0, 0, screenWidth, screenHeight, null);
		}
	}
}