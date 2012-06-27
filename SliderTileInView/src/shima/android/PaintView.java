package shima.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class PaintView extends View {
	private static final String TAG = PaintView.class.getSimpleName();
	private static final float TOLERANCE = 6;
	private Bitmap offScreenBitmap;
	private Canvas offScreenCanvas;
	private ImageView backgroundView;
	private Paint paint;
	private Path path;
	public enum PenType { PEN, ERASER } PenType penType = PenType.PEN;
	private PorterDuffXfermode eraserMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); //ok
	private PorterDuffXfermode dstOver = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER); //ok
	private PorterDuffXfermode srcOver = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
	private PorterDuffXfermode dstATop = new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);
	private PorterDuffXfermode srcIn = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN); //ok
	
	private Point cp = new Point();
	private Point pp = new Point();
	private Board board;
	private List<Tile> movables = new ArrayList();
	private Rect invalidated;
	private Point limiter;
	private Point dt;
	
	public PaintView(Context context) { this(context, null); }
	public PaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.alpha(0));
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(4);
		paint.setColor(Color.RED);
		path = new Path();
	}
	@Override protected void onSizeChanged(int w, int h, int pw, int ph) {
		super.onSizeChanged(w, h, pw, ph);
		offScreenBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		offScreenCanvas = new Canvas(offScreenBitmap);
		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg1);
		board = new Board(bitmap, 4, 3);
		board.setTiles(w, h);
		board.shuffle();
		
	}
	Point getDt(Point prev, Point curr, Point limi) {
		
		return null;
	}
	@Override public boolean onTouchEvent(MotionEvent e) {
		cp.x = (int)e.getX(); cp.y = (int)e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
//			path.reset();
//			path.moveTo(x, y);
			invalidated = board.getMovables(cp, movables, limiter);
			break;
		case MotionEvent.ACTION_MOVE:
			if (invalidated == null) break;;
			if (Math.abs(cp.x - pp.x) < TOLERANCE) break;;
			if (Math.abs(cp.y - pp.y) < TOLERANCE) break;;
			Point dt = getDt(pp, cp, limiter);
			if (dt == null) break;
			break;
		case MotionEvent.ACTION_UP:
//			path.lineTo(x, y);
//			offScreenCanvas.drawPath(path, paint);
			invalidated = null;
			dt = null;
			break;
		}
		pp.x = cp.x; pp.y = cp.y;
		invalidate();
		return true;
	}
	@Override protected void onDraw(Canvas canvas) {
		offScreenCanvas.drawPath(path, paint);
		canvas.drawBitmap(offScreenBitmap, 0, 0, null);
		//--
		if (invalidated == null) {
			for (Tile t : board.tiles) {
				if (board.logicalBoard.hole == t.logicalTile) continue;
				canvas.drawBitmap(board.bitmap, t.src, t.dst, null);
			}
			paint.setXfermode(eraserMode);
			paint.setAlpha(0);
			for (Tile t : board.tiles) {
				canvas.drawRect(t.dst, paint);
			}
		} else {
			for (Tile t : movables) {
				Rect translated = Utils.translated(t.dst, dt);
				canvas.drawBitmap(board.bitmap, t.src, translated, null);
			}
		}
		
//		canvas.drawBitmap(board.bitmap, 0, 0, null);
	}

//	List<Tile> createTileRects(int rows, int cols, float w, float h, Matrix m) {
//		List<Tile> list = new ArrayList<Tile>();
//		float dx = w/cols, dy = h/rows;
//		float top=0f, bottom=dy;
//		for (int i=0; i<rows; i++) {
//			float left = 0f, right = dx;
//			for (int j=0; j<cols; j++) {
//				RectF src = new RectF(left, top, right, bottom);
//				RectF dst = new RectF();
//				m.mapRect(dst, src);
//				list.add(new Tile(src, dst));
//				left = right; right += dx;
//			}
//			top = bottom; bottom += dy;
//		}
//		return list;
//	}
//	Matrix adjustingMatrix(float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
//		float scale;
//		float dx = 0f, dy = 0f;
//		if (dstWidth/dstHeight > srcWidth/srcHeight) {	// vertically fit
//			scale = dstHeight/srcHeight;
//			dx = (dstWidth - scale*srcWidth) / 2f;
//		} else {										// horizontally fit
//			scale = dstWidth/srcWidth;
//			dy = (dstHeight - scale*srcHeight) / 2f;
//		}
//		Matrix matrix = new Matrix();
//		matrix.setScale(scale, scale);
//		matrix.postTranslate(dx, dy);
//		return matrix;
//	}

	boolean setPenType(PenType type) {
		if (type == penType) return false;
		switch (penType = type) {
		case PEN:
//			paint.setXfermode(null);
			paint.setXfermode(null);
			paint.setAlpha(255);
			break;
		case ERASER:
			paint.setXfermode(eraserMode);
			paint.setAlpha(0);
			
			paint.setXfermode(dstOver);
			paint.setXfermode(srcIn);
			paint.setAlpha(255);
			paint.setColor(Color.RED);
			break;
		}
		return true;
	}
	boolean setPenColor(int color) {
		color = Color.GREEN;
		if (penType == PenType.PEN) {
			paint.setColor(color);
			return true;
		}
		return false;
	}
	void setBgColor(int color) {
		color = Color.RED;
		if (backgroundView != null) {
			backgroundView.setImageDrawable(null);
			backgroundView.setBackgroundColor(color);
		}
	}
	void setBgImage(Drawable drawable) {
		if (backgroundView != null) {
			backgroundView.setImageDrawable(drawable);
		}
	}
}