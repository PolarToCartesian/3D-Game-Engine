package engine;

import java.awt.Color;

public class Vector {
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
