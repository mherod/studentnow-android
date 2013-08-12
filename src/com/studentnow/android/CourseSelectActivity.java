package com.studentnow.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.studentnow.Course;
import org.studentnow.Institution;
import org.studentnow.api.CourseQuery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class CourseSelectActivity extends Activity implements
		SearchView.OnQueryTextListener, Runnable, OnItemClickListener {

	private Institution searchInstitution = null;

	private boolean opened = false;

	ProgressBar progressSpinner;
	ListView resultsListView;

	private ResultsListAdapter resultsListAdapter;

	Thread searchThread;

	private String[] searchQuery = new String[] { "", "-" };

	private List<Course> _results;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		searchInstitution = (Institution) bundle.getSerializable("inst");

		setContentView(R.layout.activity_list_loading);

		progressSpinner = (ProgressBar) findViewById(R.id.waitingProgressBar);

		_results = new ArrayList<Course>();

		resultsListView = (ListView) findViewById(R.id.resultsListView);
		resultsListAdapter = new ResultsListAdapter(this);
		resultsListView.setAdapter(resultsListAdapter);
		resultsListView.setOnItemClickListener(this);

		registerForContextMenu(resultsListView);

		searchThread = new Thread(this);
		searchThread.start();

	}

	public void onResume() {
		super.onResume();
		opened = true;
	}

	public void onPause() {
		super.onPause();
		opened = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_search_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(this);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}

	public boolean onQueryTextChange(String query) {
		searchQuery[0] = query;
		return true;
	}

	public boolean onQueryTextSubmit(String query) {
		searchQuery[0] = query;
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			final int position, long id) {
		Course selection = _results.get(position);
		if (selection == null) {
			return;
		}
		Toast.makeText(this, "Selected: " + selection.getName(),
				Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK,
				new Intent().putExtra("course", (Course) selection));
		finish();
	}

	@Override
	public void run() {
		boolean retry = false;
		while (true) {
			if (retry || !searchQuery[1].equals(searchQuery[0])) {
				runOnUiThread(showProgress);

				retry = false;
				searchQuery[1] = searchQuery[0];

				_results.clear();

				List<Course> resCourses = CourseQuery.query(
						searchInstitution.getCode(), searchQuery[1]);

				if (resCourses == null) {
					retry = true;
				} else {
					_results.addAll(resCourses);
					runOnUiThread(notifyDataSetChanged);					
				}
			} else if (progressSpinner.getVisibility() == View.VISIBLE) {
				runOnUiThread(hideProgress);
			}
			do {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			} while (!opened);
		}
	}

	final Runnable notifyDataSetChanged = new Runnable() {
		@Override
		public void run() {
			resultsListAdapter.notifyDataSetChanged();
		}
	};

	final Runnable hideProgress = new Runnable() {
		@Override
		public void run() {
			showResultsList();
		}
	};

	final Runnable showProgress = new Runnable() {
		@Override
		public void run() {
			hideResultsList();
		}
	};

	private void showResultsList() {
		progressSpinner.setVisibility(View.GONE);
		resultsListView.setVisibility(View.VISIBLE);
	}

	private void hideResultsList() {
		progressSpinner.setVisibility(View.VISIBLE);
		resultsListView.setVisibility(View.GONE);
	}

	class ResultsListAdapter extends BaseAdapter {

		Context context;
		List<Course> r = null;

		class ViewHolder {

		}

		// private LayoutInflater mInflater;

		public ResultsListAdapter(Context context) {
			this.context = context;
			r = Collections.synchronizedList(_results);
			// mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return r.size();
		}

		@Override
		public Object getItem(int arg0) {
			return r.get(arg0);
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
			a.setText(r.get(position).getName());
			a.setPadding(5, 5, 5, 5);
			a.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

			convertView = a;

			h = new ViewHolder();
			convertView.setTag(h);

			return convertView;
			// }

			// h = (ViewHolder) convertView.getTag();
			// return convertView;
		}
	}

}