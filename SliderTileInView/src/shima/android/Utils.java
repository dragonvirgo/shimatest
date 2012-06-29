package shima.android;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;

public class Utils {
	public static Matrix adjustingMatrix(float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
		float scale;
		float dx = 0f, dy = 0f;
		if (dstWidth/dstHeight > srcWidth/srcHeight) {	// vertically fit
			scale = dstHeight/srcHeight;
			dx = (dstWidth - scale*srcWidth) / 2f;
		} else {										// horizontally fit
			scale = dstWidth/srcWidth;
			dy = (dstHeight - scale*srcHeight) / 2f;
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		matrix.postTranslate(dx, dy);
		return matrix;
	}
	public static Point getAdjustedVector(Point sp, Point ep, Point limiter, int tolerance) {
		int x = ep.x - sp.x;
		int y = ep.y - sp.y;
		if (Math.abs(x) < tolerance || Math.abs(y) < tolerance) return null;
		Point vec = new Point(x, y);
		if (limiter.x == 0) {
			vec.x = 0;
			if (limiter.y < 0) {
				if (vec.y < limiter.y) vec.y = limiter.y;
				if (vec.y > 0) vec.y = 0;
			} else {
				if (vec.y > limiter.y) vec.y = limiter.y;
				if (vec.y < 0) vec.y = 0;
			}
		} else {
			vec.y = 0;
			if (limiter.x < 0) {
				if (vec.x < limiter.x) vec.x = limiter.x;
				if (vec.x > 0) vec.x = 0;
			} else {
				if (vec.x > limiter.x) vec.x = limiter.x;
				if (vec.x < 0) vec.x = 0;
			}
		}
		return vec;
	}
	public static Rect scaleByBorder(int borderWidth, Rect src, Rect dst) {
		dst.left	= -borderWidth + src.left;
		dst.top		= -borderWidth + src.top;
		dst.right	=  borderWidth + src.right;
		dst.bottom	=  borderWidth + src.bottom;
		return dst;
	}
	public static Rect translateByVector(Point vec, Rect src, Rect dst) {
		if (vec == null) return dst;
		dst.left	= vec.x + src.left;
		dst.top		= vec.y + src.top;
		dst.right	= vec.x + src.right;
		dst.bottom	= vec.y + src.bottom;
		return dst;
	}
	public static Rect translateByVector(Point vec, Rect rect) {
		if (vec == null) return rect;
		rect.left	+= vec.x;
		rect.top	+= vec.y;
		rect.right	+= vec.x;
		rect.bottom	+= vec.y;
		return rect;
	}
	public static boolean slided(Point vec, Point limiter) {
		return Math.abs(vec.x*2) > Math.abs(limiter.x) || Math.abs(vec.y*2) > Math.abs(limiter.y);
	}
//	public static Rect translated(Rect src, Point offset) {
//		Rect dst = new Rect(src);
//		if (offset == null) return dst;
//		dst.top		+= offset.y;
//		dst.left	+= offset.x;
//		dst.bottom	+= offset.y;
//		dst.right	+= offset.x;
//		return dst;
//	}
}
