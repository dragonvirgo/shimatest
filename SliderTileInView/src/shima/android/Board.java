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
		Matrix m = Utils.adjustingMatrix(bitmap.getWidth(), bitmap.getHeight(), viewWidth, viewHeight);
		tiles = createTiles(rows, cols, bitmap.getWidth(), bitmap.getHeight(), m);
	}
	Rect getMovables(Point p, List<Tile> movables, Point limiter) {
		LogicalTile lt = null;;
		for (Tile t : tiles) {
			if (t.dst.contains(p.x, p.y)) { lt = t.logicalTile; break; }
		}
		if (lt == null) return null;
		List<LogicalTile> lgTiles = logicalBoard.getMovables(lt);
		// logicalTile-->Tileのマップが必要。
		// lgTilesからtilesを抽出。リストの最後はholeなので除外
		// limiterを計算
		if (lgTiles == null) return null;
		return null;
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
}