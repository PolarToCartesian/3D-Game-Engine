package engine;

public class MatrixOperations {
	public static double[][] createPerspectiveMatrix(double _width, double _height, double _fov, double _zNear, double _zFar) {
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

	public static Vector multiplyMatrixByVector(double[][] _m, Vector _v) {
		double x = _m[0][0] * _v.x + _m[1][0] * _v.y + _m[2][0] * _v.z + _m[3][0];
		double y = _m[0][1] * _v.x + _m[1][1] * _v.y + _m[2][1] * _v.z + _m[3][1];
		double z = _m[0][2] * _v.x + _m[1][2] * _v.y + _m[2][2] * _v.z + _m[3][2];
		double w = _m[0][3] * _v.x + _m[1][3] * _v.y + _m[2][3] * _v.z + _m[3][3];

		return new Vector(x, y, z, w);
	}

	public static double[][] getRotationXMatrix(double cosX, double sinX) {
		return new double[][] {
			{1, 0,     0,    0},
			{0, cosX,  sinX, 0},
			{0, -sinX, cosX, 0},
			{0, 0,     0,    1}
		};
	}

	public static double[][] getRotationYMatrix(double cosY, double sinY) {
		return new double[][] {
			{cosY, 0, -sinY, 0},
			{0,    1, 0,     0},
			{sinY, 0, cosY,  0},
			{0,    0, 0,     1}
		};
	}

	public static double[][] getRotationZMatrix(double cosZ, double sinZ) {
		return new double[][] {
			{cosZ,  sinZ, 0, 0},
			{-sinZ, cosZ, 0, 0},
			{0,     0,    1, 0},
			{0,     0,    0, 1}
		};
	}
}
