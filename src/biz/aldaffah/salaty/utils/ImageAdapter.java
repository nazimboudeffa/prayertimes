package biz.aldaffah.salaty.utils;

import biz.aldaffah.salaty.R;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class ImageAdapter extends BaseAdapter {
	int[] imageIDs = {
					R.drawable.pic1, 
					R.drawable.pic2, 
					R.drawable.pic3, 
					R.drawable.pic4, 
					R.drawable.pic5, 
					R.drawable.pic6,
					R.drawable.pic7,
					R.drawable.pic8,
					R.drawable.pic9,
					R.drawable.pic10,
					R.drawable.pic11,
					R.drawable.pic12,
					R.drawable.pic13,
					R.drawable.pic14,
					R.drawable.pic15,
					R.drawable.pic16,
					R.drawable.pic17
					};

	private Context context;
	
	public ImageAdapter(Context c){
		context = c;
	}
	
	
	public View getView(int position, View convertView, ViewGroup parent){
		ImageView imageView;
		if (convertView == null){
			
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(85,85));
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageView.setPadding(5, 5, 5, 5);			
		}else {
			imageView = (ImageView) convertView;
		}
		imageView.setImageResource(imageIDs[position]);
		return imageView;					
	}
@Override
public int getCount() {
	// TODO Auto-generated method stub
	return imageIDs.length;
}
@Override
public Object getItem(int arg0) {
	// TODO Auto-generated method stub
	return null;
}
@Override
public long getItemId(int arg0) {
	// TODO Auto-generated method stub
	return 0;
}
}
