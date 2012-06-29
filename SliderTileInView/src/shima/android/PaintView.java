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
//	private static final float TOLERANCE = 6;
	private static final int TOLERANCE = 0;
	private static final int BORDER_WIDTH = 2;
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
	private Point dp = new Point();
	private Board board;
	private List<Tile> movables = new ArrayList<Tile>();
	private Rect invalidated;
	private Point limiter = new Point();;
	private Point vec;
	private int slidedTimes = 0;
	
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
	Rect translateRect(Rect r, Point offset) {
		Rect ret = new Rect(r);
		ret.left += offset.x;
		ret.top += offset.y;
		ret.right += offset.x;
		ret.bottom += offset.y;
		return ret;
	}
	@Override public boolean onTouchEvent(MotionEvent e) {
		cp.x = (int)e.getX(); cp.y = (int)e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			dp.x = cp.x; dp.y = cp.y;
			invalidated = board.getMovables(dp, movables, limiter);
			break;
		case MotionEvent.ACTION_MOVE:
			if (invalidated == null) break;;
			vec = Utils.getAdjustedVector(dp, cp, limiter, TOLERANCE);
			if (vec == null) break;
			invalidate(invalidated);
			break;
		case MotionEvent.ACTION_UP:
//			if (Utils.slided(vec, limiter)) {
//				for (Tile t : movables) {
//					Utils.translateByVector(limiter, t.src);
//					board.logicalBoard.slideTile(t.logicalTile);
//					slidedTimes++;
//				}
//			}
			vec = null;
			invalidate(invalidated);
			invalidated = null;
			break;
		}
		pp.x = cp.x; pp.y = cp.y;
		return true;
	}
	@Override protected void onDraw(Canvas canvas) {
		Rect dst = new Rect();
		if (invalidated == null) {
			for (Tile t : board.tiles) {
				if (board.logicalBoard.hole == t.logicalTile) continue;
				Utils.scaleByBorder(-BORDER_WIDTH, t.dst, dst);
				canvas.drawBitmap(board.bitmap, t.src, dst, null);
			}
		} else {
			for (Tile t : movables) {
				Log.d(TAG, "t=" + t);
				Utils.translateByVector(vec, t.dst, dst);
				Utils.scaleByBorder(-BORDER_WIDTH, dst, dst);
				canvas.drawBitmap(board.bitmap, t.src, dst, null);
			}
		}
	}
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