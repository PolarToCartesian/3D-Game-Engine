package engine;

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
import java.io.FileReader;
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

	protected final int numTriangles() { 
		return this.triangles.size();
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
	
	protected final int numLights() { 
		return this.triangles.size();
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
	
	protected final int numTextures() { 
		return this.textures.size();
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
					if (Camera.isRotatedTriangleFacingCamera(rotatedVertices, surfaceNormal)) {
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
							// If the triangle should be rendered with a texture and that texture exists
							if (triangle.doUseTexture && triangle.textureID < textures.size()) {
								float[] brightnesses = Light.getIllimunation(rotatedVertices, surfaceNormal, lights);
								Triangle.renderTexturedTriangle(manipulatedVertices, triangle.textureID, pixelBuffer, screenWidth, screenHeight, brightnesses, depthBuffer, textures);
							} else { // Render the triangle with the vertices' color
								Vector[] brightenedColors = Light.getColorsWIllumination(rotatedVertices, surfaceNormal, triangle.colors, lights);
								Triangle.renderColoredTriangle(manipulatedVertices, pixelBuffer, screenWidth, screenHeight, brightenedColors, depthBuffer);
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