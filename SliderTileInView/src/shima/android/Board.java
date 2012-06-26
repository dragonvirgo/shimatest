package shima.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

class Tile {
	Rect src;
	Rect dst;
	LogicalTile logicalTile;
	Tile(RectF srcF, RectF dstF, LogicalTile lt) {
		srcF.round(src = new Rect());
		dstF.round(dst = new Rect());
		logicalTile = lt;
	}
}
public class Board {
	Bitmap bitmap;
	int rows;
	int cols;
	List<Tile> tiles;
	LogicalBoard logicalBoard;
	Map<Point, Rect> dstRects;
	
	Board(Bitmap b, int r, int c) {
		bitmap = b;
		logicalBoard = new LogicalBoard(rows=r, cols=c);
	}
	void setTiles(int viewWidth, int viewHeight) {
		Matrix m = adjustingMatrix(bitmap.getWidth(), bitmap.getHeight(), viewWidth, viewHeight);
		tiles = createTiles(rows, cols, bitmap.getWidth(), bitmap.getHeight(), m);
	}
	void shuffle() {
		logicalBoard.shuffle();
		for (Tile tile : tiles) {
			tile.dst = dstRects.get(tile.logicalTile.lp);
		}
	}
	private List<Tile> createTiles(int rows, int cols, float w, float h, Matrix m) {
		dstRects = new HashMap<Point, Rect>();
		List<Tile> list = new ArrayList<Tile>();
		float dx = w/cols, dy = h/rows;
		float top=0f, bottom=dy;
		for (int i=0; i<rows; i++) {
			float left = 0f, right = dx;
			for (int j=0; j<cols; j++) {
				RectF src = new RectF(left, top, right, bottom);
				RectF dst = new RectF();
				m.mapRect(dst, src);
				LogicalTile logicalTile = logicalBoard.tiles[i][j];
				Tile tile = new Tile(src, dst, logicalTile);
				dstRects.put(logicalTile.lp, tile.dst);
				list.add(tile);
				left = right; right += dx;
			}
			top = bottom; bottom += dy;
		}
		return list;
	}
	private static Matrix adjustingMatrix(float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
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
}