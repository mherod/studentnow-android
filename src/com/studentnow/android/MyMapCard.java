package com.studentnow.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class MyMapCard extends Card {

	private Bitmap bitmap = null;

	public MyMapCard(String title, int image) {
		super(title, image);
	}

	public MyMapCard(String title, String desc, int image) {
		super(title, desc, image);
	}

	public MyMapCard(String title, String desc, Bitmap bitmap) {
		super(title, desc, 0);
		this.bitmap = bitmap;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_map,
				null);
		((TextView) view.findViewById(R.id.title)).setText(title);
		((TextView) view.findViewById(R.id.description)).setText(desc);
		
		Log.d("dddd", "herpe derpe");

		if (bitmap == null) {
			((ImageView) view.findViewById(R.id.imageView1))
					.setImageResource(image);
		} else {
			Log.d("dddd", "bitmap istn null");
			((ImageView) view.findViewById(R.id.imageView1))
					.setImageBitmap(bitmap);
		}

		return view;
	}

}
