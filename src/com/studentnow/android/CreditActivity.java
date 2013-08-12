package com.studentnow.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CreditActivity extends Activity implements OnItemClickListener {

	ListView creditsListView;
	CreditsListAdapter creditsListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit);

		setupActionBar();

		creditsListView = (ListView) findViewById(R.id.creditsList);
		creditsListAdapter = new CreditsListAdapter(this);
		creditsListView.setAdapter(creditsListAdapter);
		creditsListView.setOnItemClickListener(this);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			final int position, long id) {

	}

	class CreditsListAdapter extends BaseAdapter {

		String[] credits = getResources().getStringArray(
				R.array.credits);

		Context context;

		private LayoutInflater mInflater;

		public CreditsListAdapter(Context context) {
			this.context = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return credits.length;
		}

		@Override
		public Object getItem(int arg0) {
			return credits[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			ViewHolder h;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.lvi_credit, null);
				h = new ViewHolder();
				h.tv = new TextView[3];
				h.tv[0] = (TextView) convertView
						.findViewById(R.id.lvi_credit_line1);
				h.tv[1] = (TextView) convertView
						.findViewById(R.id.lvi_credit_line2);
				h.tv[2] = (TextView) convertView
						.findViewById(R.id.lvi_credit_line3);
				convertView.setTag(h);
			} else {
				h = (ViewHolder) convertView.getTag();
			}
			String[] b = credits[pos].split(";");
			for (int i = 0; i < 3; i++) {
				if (b.length > i) {
					h.tv[i].setVisibility(View.VISIBLE);
					h.tv[i].setText(b[i]);
				} else {
					h.tv[i].setVisibility(View.GONE);
					h.tv[i].setText("");
				}
			}
			return convertView;
		}

		class ViewHolder {
			TextView[] tv;
		}
	}

}
