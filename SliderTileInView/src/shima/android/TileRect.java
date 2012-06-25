package shima.android;

import android.graphics.Rect;
import android.graphics.RectF;

public class TileRect {

	Rect src;
	Rect dst;
	TileRect(RectF srcF, RectF dstF) {
		srcF.round(src = new Rect());
		dstF.round(dst = new Rect());
	}
}
