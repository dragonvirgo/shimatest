package shima.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class DoodleWithEraserActivity extends Activity {
	public PaintView paintView;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		paintView = (PaintView)findViewById(R.id.paintView);
		new SpinnerListener();
	}
	private class SpinnerListener implements AdapterView.OnItemSelectedListener {
		SpinnerListener() {
			int[] spinners = {    R.id.penOrEraser,    R.id.penColors,    R.id.bgColors };
			int[] options =  { R.array.penOrEraser, R.array.penColors, R.array.bgColors };
			for (int i=0; i<spinners.length; i++) {
				ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
				HashMap<String, Object> map = new HashMap<String, Object>();
				TypedArray tArray = getResources().obtainTypedArray(options[i]);
				for (int j=0; j<tArray.length(); j++) {
					map = new HashMap<String, Object>();
					map.put("Image", tArray.getResourceId(j, 0));
					map.put("Spinner", spinners[i]);
					list.add(map);
				}
				Spinner spinner = (Spinner)findViewById(spinners[i]);
				SpinnerAdapter adapter = new SpinnerAdapter(getApplicationContext(), list, R.layout.spinner,
						new String[] { "Image" }, new int[] { R.id.imageView });
				spinner.setAdapter(adapter);
				spinner.setOnItemSelectedListener(this);
			}
		}
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Spinner spinner = (Spinner) parent;
			Map<String, Object> item = (Map<String, Object>)spinner.getSelectedItem();
			int optionId = (Integer)item.get("Image");
			int spinnerId = (Integer)item.get("Spinner");
			Log.d("TAG", "spinnerId=" + spinnerId + ", optionID=" + optionId);
			switch (spinnerId) {
			case R.id.penOrEraser:
				Spinner penColorsSpinner = (Spinner)findViewById(R.id.penColors);
				switch (optionId) {
				case R.drawable.pen:
					paintView.setPenType(PaintView.PenType.PEN);
					penColorsSpinner.setEnabled(true);
					break;
				case R.drawable.eraser:
					paintView.setPenType(PaintView.PenType.ERASER);
					penColorsSpinner.setEnabled(false);
					break;
				}
				break;
			case R.id.penColors:
				switch (optionId) {
				case R.drawable.red:	paintView.setPenColor(Color.RED); break;
				case R.drawable.green:	paintView.setPenColor(Color.GREEN); break;
				case R.drawable.blue:	paintView.setPenColor(Color.BLUE); break;
				case R.drawable.black:	paintView.setPenColor(Color.BLACK); break;
				}
				break;
			case R.id.bgColors:
				switch (optionId) {
				case R.drawable.red:	paintView.setBgColor(Color.RED); break;
				case R.drawable.green:	paintView.setBgColor(Color.GREEN); break;
				case R.drawable.blue:	paintView.setBgColor(Color.BLUE); break;
				case R.drawable.black:	paintView.setBgColor(Color.BLACK); break;
				case R.drawable.bg1:	paintView.setBgImage(getResources().getDrawable(R.drawable.bg1)); break;
				case R.drawable.bg2:	paintView.setBgImage(getResources().getDrawable(R.drawable.bg2)); break;
				case R.drawable.bg3:	paintView.setBgImage(getResources().getDrawable(R.drawable.bg3)); break;
				}
				//Toast.makeText(DoodleWithEraserActivity.this, "‚Í‚©‚¹", Toast.LENGTH_SHORT).show();
				break;
			}
		}
		public void onNothingSelected(AdapterView<?> parent) {}
	}
	private class SpinnerAdapter extends SimpleAdapter {
		int resourceId;
		public SpinnerAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			resourceId = resource;
		}
		@Override public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) { convertView = getLayoutInflater().inflate(resourceId, null); }
			HashMap<String, Object> data = (HashMap<String, Object>)getItem(position);
			int id = (Integer)data.get("Image");
			((ImageView)convertView.findViewById(R.id.imageView)).setImageResource(id);
			return convertView;
		}
	}
}