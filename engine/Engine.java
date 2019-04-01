package engine;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.event.*;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Engine {
	private int screenWidth, screenHeight;
	private int fov;
	private int fps;
	private String screenTitle;

	private JFrame frame;
	private Panel  panel;

	private boolean mousePressed = false;

	private Point mousePosition = new Point(0, 0);

	private float[][] perspectiveMatrix;

	protected ArrayList<Triangle> triangles = new ArrayList<>();
	protected ArrayList<Light> lights = new ArrayList<>();

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

		this.frame.setVisible(true);
	}

	protected void run() {
		long defaultWaitTimeMs = (long) (1 / (float) this.fps * 1000);

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

	private void cleanUp() {
		Camera.rotation.x %= (2 * Math.PI);
		Camera.rotation.y %= (2 * Math.PI);
		Camera.rotation.z %= (2 * Math.PI);
	}

	// Setters

	protected void setScreenWidth(int _screenWidth) {
		this.screenWidth = _screenWidth;

		this.frame.setSize(this.screenWidth, this.screenHeight);
		this.perspectiveMatrix = MatrixOperations.createPerspectiveMatrix(this.screenWidth, this.screenHeight, this.fov, 0.01f, 1000f);
	}

	protected void setScreenHeight(int _screenHeight) {
		this.screenHeight = _screenHeight;

		this.frame.setSize(this.screenWidth, this.screenHeight);
		this.perspectiveMatrix = MatrixOperations.createPerspectiveMatrix(this.screenWidth, this.screenHeight, this.fov, 0.01f, 1000f);
	}

	protected void setScreenTitle(String _screenTitle) {
		this.screenTitle = _screenTitle;

		this.frame.setTitle(this.screenTitle);
	}

	// Getters

	protected int getScreenWidth()    { return this.screenWidth;  }
	protected int getScreenHeight()   { return this.screenHeight; }
	protected String getScreenTitle() { return this.screenTitle;  }

	protected boolean isMousePressed() { return this.mousePressed; }

	protected Point getMousePosition() { return this.mousePosition; }

	protected void loadModel(String fileLocation, boolean randomColors) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
			String line;
			ArrayList<Vector> vertices = new ArrayList<Vector>();

			while ((line = br.readLine()) != null) {
				String[] elements = line.split(" ");

				switch (elements[0]) {
					case "v":
						Vector vertex = new Vector((float) Double.parseDouble(elements[1]),
												   (float) Double.parseDouble(elements[2]),
									               (float) Double.parseDouble(elements[3]));

						vertices.add(vertex);
						break;

					case "f":
						Color color;
						if (randomColors) {
							color = new Color(ThreadLocalRandom.current().nextInt(0, 256), ThreadLocalRandom.current().nextInt(0, 256), ThreadLocalRandom.current().nextInt(0, 256));
						} else {
							color = new Color(255, 255, 255);
						}

						Vector v1 = vertices.get((Integer.parseInt(elements[1].split("/")[0]) - 1));
						Vector v2 = vertices.get((Integer.parseInt(elements[2].split("/")[0]) - 1));
						Vector v3 = vertices.get((Integer.parseInt(elements[3].split("/")[0]) - 1));

						Vector[] positions = new Vector[] {v1, v2, v3};

						triangles.add(new Triangle(positions, color));
						break;
				}
			}
		} catch (java.io.FileNotFoundException e) {
			System.out.println("FileNotFoundException");
		} catch (java.io.IOException e) {
			System.out.println("IOException");
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

	private static class MatrixOperations {
		static float[][] createPerspectiveMatrix(float _width, float _height, float _fov, float _zNear, float _zFar) {
			float aspectRatio = _height / (float) _width;
			float a = (float) (1.f / Math.tan(_fov * 0.5 * 180.f / 3.1415926));

			if (a > 0) a = -a;

			return new float[][] {
					{aspectRatio * a, 0, 0, 0},
					{0, a, 0, 0},
					{0, 0, _zFar / (_zFar - _zNear), 1},
					{0, 0, (-_zFar * _zNear) / (_zFar - _zNear), 0}
			};
		}

		static Vector multiplyMatrixByVector(float[][] _m, Vector _v) {
			float x = _m[0][0] * _v.x + _m[1][0] * _v.y + _m[2][0] * _v.z + _m[3][0];
			float y = _m[0][1] * _v.x + _m[1][1] * _v.y + _m[2][1] * _v.z + _m[3][1];
			float z = _m[0][2] * _v.x + _m[1][2] * _v.y + _m[2][2] * _v.z + _m[3][2];
			float w = _m[0][3] * _v.x + _m[1][3] * _v.y + _m[2][3] * _v.z + _m[3][3];

			return new Vector(x, y, z, w);
		}

		static float[][] getRotationXMatrix(float cosX, float sinX) {
			return new float[][] {
				{1, 0,     0,    0},
				{0, cosX,  sinX, 0},
				{0, -sinX, cosX, 0},
				{0, 0,     0,    1}
			};
		}

		static float[][] getRotationYMatrix(float cosY, float sinY) {
			return new float[][] {
				{cosY, 0, -sinY, 0},
				{0,    1, 0,     0},
				{sinY, 0, cosY,  0},
				{0,    0, 0,     1}
			};
		}

		static float[][] getRotationZMatrix(float cosZ, float sinZ) {
			return new float[][] {
				{cosZ,  sinZ, 0, 0},
				{-sinZ, cosZ, 0, 0},
				{0,     0,    1, 0},
				{0,     0,    0, 1}
			};
		}

	}

	protected static class Vector {
		public float x, y, z, w = 1;

		public Vector() {this.x = 0; this.y = 0; this.z = 0; }
		public Vector(float _x, float _y) { this.x = _x; this.y = _y; this.z = 0; }
		public Vector(float _x, float _y, float _z) { this.x = _x; this.y = _y; this.z = _z; }
		public Vector(float _x, float _y, float _z, float _w) { this.x = _x; this.y = _y; this.z = _z; this.w = _w; }

		public Vector copy() { return new Vector(this.x, this.y, this.z); }

		public void add(float _x, float _y, float _z) { this.x += _x; this.y += _y; this.z += _z; }
		public void sub(float _x, float _y, float _z) { this.x -= _x; this.y -= _y; this.z -= _z; }
		public void mul(float _x, float _y, float _z) { this.x *= _x; this.y *= _y; this.z *= _z; }
		public void div(float _x, float _y, float _z) { this.x /= _x; this.y /= _y; this.z /= _z; }

		public void add(float _n) { this.add(_n, _n, _n); }
		public void sub(float _n) { this.sub(_n, _n, _n); }
		public void mul(float _n) { this.mul(_n, _n, _n); }
		public void div(float _n) { this.div(_n, _n, _n); }

		public void add(Vector _v) { this.add(_v.x, _v.y, _v.z); }
		public void sub(Vector _v) { this.sub(_v.x, _v.y, _v.z); }
		public void mul(Vector _v) { this.mul(_v.x, _v.y, _v.z); }
		public void div(Vector _v) { this.div(_v.x, _v.y, _v.z); }

		public float getLength() { return (float) Math.sqrt( this.x * this.x + this.y * this.y + this.z * this.z ); }
		public void  normalize() { this.div(this.getLength()); }

		public static Vector normalize(Vector _v) { Vector result = _v.copy(); result.normalize(); return result; }

		public static float dotProduct(Vector _a, Vector _b) { return _a.x * _b.x + _a.y * _b.y + _a.z * _b.z; }

		//TBI - To Be Improved

		public static Vector add(Vector _v, float _n) { Vector result = _v.copy(); result.add(_n); return result; }
		public static Vector sub(Vector _v, float _n) { Vector result = _v.copy(); result.sub(_n); return result; }
		public static Vector mul(Vector _v, float _n) { Vector result = _v.copy(); result.mul(_n); return result; }
		public static Vector div(Vector _v, float _n) { Vector result = _v.copy(); result.div(_n); return result; }

		public static Vector add(Vector _a, Vector _b) { Vector result = _a.copy(); result.add(_b); return result; }
		public static Vector sub(Vector _a, Vector _b) { Vector result = _a.copy(); result.sub(_b); return result; }
		public static Vector mul(Vector _a, Vector _b) { Vector result = _a.copy(); result.mul(_b); return result; }
		public static Vector div(Vector _a, Vector _b) { Vector result = _a.copy(); result.div(_b); return result; }
	}

	protected static class Camera {
		public static Vector position = new Vector(0, 1, -5);
		public static Vector rotation = new Vector(0, 0, 0);
	}

	protected static class Triangle {
		public Vector[] vertices;
		public Vector rotation;
		public Vector rotationMidPoint = new Vector(0, 0, 0);

		public Color color;

		public Triangle(Vector[] _vertices, Color _color) {
			this.vertices = _vertices;
			this.color = _color;
			this.rotation = new Vector(0, 0, 0);
		}

		public static Vector getSurfaceNormal(Vector[] vertices) {
			//https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal

			Vector U = Vector.sub(vertices[1], vertices[0]);
			Vector V = Vector.sub(vertices[2], vertices[0]);

			float nX = U.y * V.z - U.z * V.y;
			float nY = U.z * V.x - U.x * V.z;
			float nZ = U.x * V.y - U.y * V.x;

			return Vector.normalize(new Vector(nX, nY, nZ));
		}

	}

	private static class TriangleRender2D {
		int[] xPoints;
		int[] yPoints;
		float z;

		Color color;

		TriangleRender2D(Vector[] _vertices, Color _color) {
			this.xPoints = new int[] { (int) _vertices[0].x, (int) _vertices[1].x, (int) _vertices[2].x };
			this.yPoints = new int[] { (int) _vertices[0].y, (int) _vertices[1].y, (int) _vertices[2].y };

			this.z = _vertices[0].z + _vertices[1].z + _vertices[2].z; // No Need to Divide By Three
			this.color = _color;
		}

		void render(Graphics g) {
			g.setColor(color);
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}

	public static class Light {
		public Vector position;
		public Color color;

		public float intensity;

		public Light(Vector _position, Color _color, float _intensity) {
			this.position = _position;
			this.color = _color;
			this.intensity = _intensity;
		}
	}

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

	private static class SortByDistance implements Comparator<TriangleRender2D> {
		@Override
		public int compare(TriangleRender2D a, TriangleRender2D b) {
			if (a.z < b.z) return 1;
			if (a.z > b.z) return -1;

			return 0;
		}
	}

	private class Panel extends JPanel {
		private static final long serialVersionUID = 1L;

		private boolean isRotatedTriangleFacingCamera(Vector[] _rotatedVertices, Vector _surfaceNormal) {
			Vector triangleRotatedCenter = Vector.div(Vector.add(Vector.add(_rotatedVertices[0], _rotatedVertices[1]), _rotatedVertices[2]), 3);
			Vector triangleToCamera = Vector.normalize(Vector.sub(Camera.position, triangleRotatedCenter));

			return (Vector.dotProduct(_surfaceNormal, triangleToCamera) > 0.f);
		}

		private float getIllumination(Vector[] _rotatedVertices, Vector _surfaceNormal) {
			// Total Brightness
			float summedBrightness = 0;

			Vector triangleRotatedCenter = Vector.div(Vector.add(Vector.add(_rotatedVertices[0], _rotatedVertices[1]), _rotatedVertices[2]), 3);

			// For Every Light
			for (Light light : lights) {
				// No Need To Translate The Light's position and the triangle's position because it would cancel out
				Vector triangleToLight = Vector.sub(light.position, triangleRotatedCenter);

				float distanceSquared = (float) Math.pow(triangleToLight.getLength(), 2);

				triangleToLight.normalize();

				float dotProductTriangleAndLight = Vector.dotProduct(triangleToLight, _surfaceNormal);

				if (dotProductTriangleAndLight > 0f) {
					dotProductTriangleAndLight = Math.abs(dotProductTriangleAndLight);
				} else {
					dotProductTriangleAndLight = 0f;
				}

				summedBrightness += (dotProductTriangleAndLight * light.intensity) / distanceSquared; // Inverse Square Law
			}

			// Limit the brightness to 1
			return (summedBrightness <= 1f) ? summedBrightness : 1f;
		}

		public void paintComponent(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, screenWidth, screenHeight);

			try {
				ArrayList<TriangleRender2D> trianglesToRender = new ArrayList<>();

				float[][] cameraRotationXMatrix = MatrixOperations.getRotationXMatrix((float) Math.cos(-Camera.rotation.x), (float) Math.sin(-Camera.rotation.x));
				float[][] cameraRotationYMatrix = MatrixOperations.getRotationYMatrix((float) Math.cos(-Camera.rotation.y), (float) Math.sin(-Camera.rotation.y));
				float[][] cameraRotationZMatrix = MatrixOperations.getRotationZMatrix((float) Math.cos(-Camera.rotation.z), (float) Math.sin(-Camera.rotation.z));

				for (Triangle triangle : triangles) {
					Vector[] rotatedVertices = new Vector[3];

					// Rotation Matrices : https://www.siggraph.org/education/materials/HyperGraph/modeling/mod_tran/3drota.htm#Z

					float[][] triangleRotationXMatrix = MatrixOperations.getRotationXMatrix((float) Math.cos(triangle.rotation.x), (float) Math.sin(triangle.rotation.x));
					float[][] triangleRotationYMatrix = MatrixOperations.getRotationYMatrix((float) Math.cos(triangle.rotation.y), (float) Math.sin(triangle.rotation.y));
					float[][] triangleRotationZMatrix = MatrixOperations.getRotationZMatrix((float) Math.cos(triangle.rotation.z), (float) Math.sin(triangle.rotation.z));

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

							// World To Screen
							manipulatedVertices[i].x = ((manipulatedVertices[i].x - 1f) / -2f) * this.getWidth();
							manipulatedVertices[i].y = ((manipulatedVertices[i].y + 1f) /  2f) * this.getHeight();
						}

						// If The Triangle Is In Front Of The Camera
						if (!triangleBehindCamera) {
							// Calculate Triangle Color with respect to lighting
							float illumination = getIllumination(rotatedVertices, surfaceNormal);

							// Don't render the triangle if the illumination is 0
							if (illumination > 0) {
								int newRed   = (int) (triangle.color.getRed()   * illumination);
								int newGreen = (int) (triangle.color.getGreen() * illumination);
								int newBlue  = (int) (triangle.color.getBlue()  * illumination);

								Color triangleColorWithLighting = new Color(newRed, newGreen, newBlue);

								// Add Triangle to the render queue
								trianglesToRender.add(new TriangleRender2D(manipulatedVertices, triangleColorWithLighting));
							}
						}
					}
				}

				// Sort Triangles By Distance
				trianglesToRender.sort(new SortByDistance());

				// Render Triangles
				for (TriangleRender2D triangle : trianglesToRender) {
					// Render The Triangle
					triangle.render(g);
				}
			} catch (java.util.ConcurrentModificationException e) {
				e.printStackTrace();
			}
		}
	}
}