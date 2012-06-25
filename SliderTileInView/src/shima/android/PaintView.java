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
	private PointF prev = new PointF();
	public enum PenType { PEN, ERASER } PenType penType = PenType.PEN;
	private PorterDuffXfermode eraserMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); //ok
	private PorterDuffXfermode dstOver = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER); //ok
	private PorterDuffXfermode srcOver = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
	private PorterDuffXfermode dstATop = new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);
	private PorterDuffXfermode srcIn = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN); //ok
	
	Bitmap bitmap;
	float vw, vh;
	

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
		paint.setStrokeWidth(20);
		paint.setColor(Color.RED);
		path = new Path();
		Resources r = getResources();
//		bitmap = BitmapFactory.decodeResource(r, R.drawable.android);
		bitmap = BitmapFactory.decodeResource(r, R.drawable.bg1);

	}
	@Override protected void onSizeChanged(int w, int h, int pw, int ph) {
		super.onSizeChanged(w, h, pw, ph);
		offScreenBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		offScreenCanvas = new Canvas(offScreenBitmap);
		
		vw = w; vh = h;
	}
	@Override protected void onDraw(Canvas canvas) {
		offScreenCanvas.drawPath(path, paint);
		canvas.drawBitmap(offScreenBitmap, 0, 0, null);
		//--
		int rows = 4, cols = 3;
		float bw = bitmap.getWidth(); float bh = bitmap.getHeight();
		Log.d(TAG, "vw=" + vw + ", vh=" + vh);
		Log.d(TAG, "bw=" + bw + ", bh=" + bh);
		
		float ratiov = vw/vh;
		float ratiob = bw/bh;
		float scale;
		float dx = 0f, dy = 0f;
		if (ratiov > ratiob) { // tate fit
			scale = vh/bh;
			dx = (vw - scale*bw) / 2f;
		} else {
			scale = vw/bw;
			dy = (vh - scale*bh) / 2f;
		}
		Matrix m = new Matrix();
		m.setScale(scale, scale);
		m.postTranslate(dx, dy);
		List<TileRect> tileRects = createTileRects(rows, cols, bw, bh, m);
		for (TileRect t : tileRects) {
			canvas.drawBitmap(bitmap, t.src, t.dst, null);
		}
		paint.setXfermode(eraserMode);
		paint.setAlpha(0);
		for (TileRect t : tileRects) {
			canvas.drawRect(t.dst, paint);
		}
		
//		canvas.drawBitmap(bitmap, m, null);
		canvas.drawBitmap(bitmap, 0, 0, null);
	}
	List<TileRect> createTileRects(int rows, int cols, float w, float h, Matrix m) {
		List<TileRect> list = new ArrayList<TileRect>();
		float dx = w/cols, dy = h/rows;
		float top=0f, bottom=dy;
		for (int i=0; i<rows; i++) {
			float left = 0f;
			float right = dx;
			for (int j=0; j<cols; j++) {
				RectF src = new RectF(left, top, right, bottom);
				RectF dst = new RectF();
				m.mapRect(dst, src);
				list.add(new TileRect(src, dst));
				left = right;
				right += dx;
			}
			top = bottom;
			bottom += dy;
		}
		return list;
	}
	@Override public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX(); float y = e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			path.reset();
			path.moveTo(x, y);
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(x - prev.x) >= TOLERANCE || Math.abs(y - prev.y) >= TOLERANCE) {
				path.quadTo(prev.x, prev.y, (prev.x + x) / 2, (prev.y + y) / 2);
			}
			break;
		case MotionEvent.ACTION_UP:
			path.lineTo(x, y);
//			offScreenCanvas.drawPath(path, paint);
			break;
		}
		prev.x = x; prev.y = y;
		invalidate();
		return true;
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