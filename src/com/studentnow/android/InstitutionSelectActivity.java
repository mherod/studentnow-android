package com.studentnow.android;

import java.util.ArrayList;
import java.util.List;

import org.studentnow.Institution;
import org.studentnow.api.InstitutionsQuery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
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

public class InstitutionSelectActivity extends Activity implements Runnable,
		OnItemClickListener {

	private View mLoadingView;
	private ListView mListView;

	private List<Institution> institutionList = null;
	private InstitutionListAdapter institutionListAdapter;

	private int mShortAnimationDuration = 700;

	private boolean opened = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_institution);

		mLoadingView = findViewById(R.id.loading_spinner);
		mListView = (ListView) findViewById(R.id.list_institution);
		institutionListAdapter = new InstitutionListAdapter(this);
		mListView.setAdapter(institutionListAdapter);
		mListView.setOnItemClickListener(this);

		registerForContextMenu(mListView);

		new Thread(this).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void crossfade(final View from, final View to) {
		to.setAlpha(0f);
		to.setVisibility(View.VISIBLE);
		to.animate().alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);
		from.animate().alpha(0f).setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						from.setVisibility(View.GONE);
					}
				});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			final int position, long id) {

		Institution selection = institutionList.get(position);

		Intent returnIntent = new Intent();
		returnIntent.putExtra("inst", (Institution) selection);

		setResult(RESULT_OK, returnIntent);
		finish();
	}

	final Runnable notifyDataSetChanged = new Runnable() {
		@Override
		public void run() {
			institutionListAdapter.notifyDataSetChanged();
		}

	};

	final Runnable hideProgress = new Runnable() {
		@Override
		public void run() {
			crossfade(mLoadingView, mListView);
		}
	};

	final Runnable showProgress = new Runnable() {
		@Override
		public void run() {
			crossfade(mListView, mLoadingView);
		}
	};

	public void onResume() {
		super.onResume();
		opened = true;
	}

	public void onPause() {
		super.onPause();
		opened = false;
	}

	@Override
	public void run() {
		while (true) {
			if (institutionList == null) {
				runOnUiThread(showProgress);
				institutionList = new ArrayList<Institution>();
			} else if (institutionList.size() == 0) {
				institutionList.clear();
				List<Institution> resCourses = InstitutionsQuery.query();
				institutionList.addAll(resCourses);
				runOnUiThread(notifyDataSetChanged);
			} else if (mLoadingView.getVisibility() == View.VISIBLE) {
				runOnUiThread(hideProgress);
			}
			do {
				try {
					Thread.sleep(mShortAnimationDuration + 200);
				} catch (Exception e) {
				}
			} while (!opened);
		}
	}

	class InstitutionListAdapter extends BaseAdapter {

		Context context;

		class ViewHolder {
		}

		private LayoutInflater mInflater;

		public InstitutionListAdapter(Context context) {
			this.context = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (institutionList == null) {
				return 0;
			}
			return institutionList.size();
		}

		@Override
		public Object getItem(int arg0) {
			if (institutionList == null) {
				return null;
			}
			return institutionList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder h;
			// if (convertView == null) {
			// convertView = mInflater.inflate(R.layout.item, null);

			TextView a = new TextView(context);
			a.setText(institutionList.get(position).getName());
			a.setPadding(20, 20, 20, 20);
			a.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

			convertView = a;

			h = new ViewHolder();
			convertView.setTag(h);

			return convertView;
		}
	}

}
