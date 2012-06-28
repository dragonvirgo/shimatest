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
	Rect moving;
	LogicalTile logicalTile;
	Tile(RectF srcF, RectF dstF, LogicalTile lt) {
		srcF.round(src = new Rect());
		dstF.round(dst = new Rect());
		logicalTile = lt;
		moving = dst;
	}
	@Override public String toString() {
		return "logicalTile={" + logicalTile.toString() + "}, src=" + src + ", dst=" + dst;
	}
}
public class Board {
	Bitmap bitmap;
	int rows;
	int cols;
	List<Tile> tiles;
	LogicalBoard logicalBoard;
	Map<Point, Rect> dstRectsMap;
	Map<LogicalTile, Tile> tilesMap;
	
	Board(Bitmap b, int r, int c) {
		bitmap = b;
		logicalBoard = new LogicalBoard(rows=r, cols=c);
	}
	void setTiles(int viewWidth, int viewHeight) {
		Matrix m = Utils.adjustingMatrix(bitmap.getWidth(), bitmap.getHeight(), viewWidth, viewHeight);
		tiles = createTiles(rows, cols, bitmap.getWidth(), bitmap.getHeight(), m);
	}
	Rect getMovables(Point p, List<Tile> movables, Point limiter) {
		movables.clear();
		limiter.x = limiter.y = 0;
		LogicalTile selected = null;;
		for (Tile t : tiles) {
			if (t.dst.contains(p.x, p.y)) { selected = t.logicalTile; break; }
		}
		if (selected == null) return null;
		List<LogicalTile> lgTiles = logicalBoard.getMovables(selected);
		if (lgTiles == null) return null;
		Rect rect = new Rect();
		for (LogicalTile lg : lgTiles) {
			Tile tile = tilesMap.get(lg);
			rect.union(tile.dst);
			movables.add(tile);
		}
		Tile hole = tilesMap.get(logicalBoard.hole);
		rect.union(hole.dst);
		// limiterを計算
		Direction d = logicalBoard.getDirection(selected);
		switch (d) {
		case UP:	limiter.y = -hole.dst.height();	break;
		case DOWN:	limiter.y =  hole.dst.height();	break;
		case LEFT:	limiter.x = -hole.dst.width();	break;
		case RIGHT:	limiter.x =  hole.dst.width();	break;
		}
		return rect;
	}
	void shuffle() {
		logicalBoard.shuffle();
		for (Tile tile : tiles) {
			tile.dst = dstRectsMap.get(tile.logicalTile.lp);
		}
	}
	private List<Tile> createTiles(int rows, int cols, float w, float h, Matrix m) {
		dstRectsMap = new HashMap<Point, Rect>();
		tilesMap = new HashMap<LogicalTile, Tile>();
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
				dstRectsMap.put(logicalTile.lp, tile.dst);
				tilesMap.put(logicalTile, tile);
				list.add(tile);
				left = right; right += dx;
			}
			top = bottom; bottom += dy;
		}
		return list;
	}
}