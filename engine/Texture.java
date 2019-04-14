package engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Texture {
	private BufferedImage textureImage;
	
	private int textureWidth, textureHeight;
	
	public void setTexture(String _path) {
		try {
			this.textureImage = ImageIO.read(new File(_path));
			
			this.textureWidth  = this.textureImage.getWidth();
			this.textureHeight = this.textureImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setTexture(BufferedImage _textureImage) {
		this.textureImage = _textureImage;
		
		this.textureWidth  = this.textureImage.getWidth();
		this.textureHeight = this.textureImage.getHeight();
	}
	
	public Texture(String _path) { setTexture(_path); }
	
	public Texture(BufferedImage _textureImage) { setTexture(_textureImage); }
	
	public Vector sample(int _u, int _v) {
		if (_u >= 0 && _u < this.textureImage.getWidth()) {
			if (_v >= 0 && _v < this.textureImage.getHeight()) {
				return new Vector(new Color(textureImage.getRGB(_u, _v)));
			}
		}
					
		return new Vector(0, 0, 0);
	}
	
	public int getWidth()  { return this.textureWidth; }
	
	public int getHeight() { return this.textureHeight; }
}
