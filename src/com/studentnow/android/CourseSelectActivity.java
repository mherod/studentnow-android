package com.studentnow.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.studentnow.Course;
import org.studentnow.Static.Fields;
import org.studentnow.api.AuthResponse;
import org.studentnow.api.CourseQuery;

import android.app.Activity;
import android.content.Context;
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

import com.studentnow.android.service.AccountModule;
import com.studentnow.android.service.LiveService;
import com.studentnow.android.service.UserSyncModule;

public class CourseSelectActivity extends Activity implements
		SearchView.OnQueryTextListener, Runnable, OnItemClickListener {

	private LiveServiceLink serviceLink = null;

	private boolean opened = false;

	ProgressBar progressSpinner;
	ListView resultsListView;

	private ResultsListAdapter resultsListAdapter;

	Thread searchThread;

	private String[] searchQuery = new String[] { "", "-" };

	private List<Course> mResults;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Bundle bundle = getIntent().getExtras();
		// searchInstitution = (Institution) bundle.getSerializable("inst");

		setContentView(R.layout.activity_list_loading);

		progressSpinner = (ProgressBar) findViewById(R.id.waitingProgressBar);

		mResults = new ArrayList<Course>();

		resultsListView = (ListView) findViewById(R.id.resultsListView);
		resultsListAdapter = new ResultsListAdapter(this);
		resultsListView.setAdapter(resultsListAdapter);
		resultsListView.setOnItemClickListener(this);

		registerForContextMenu(resultsListView);

		serviceLink = new LiveServiceLink(this);

		searchThread = new Thread(this);
		searchThread.start();

	}

	public void onResume() {
		super.onResume();
		serviceLink.start();
		opened = true;
	}

	public void onPause() {
		super.onPause();
		opened = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		serviceLink.stop();
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
		Course selection = mResults.get(position);
		if (selection == null) {
			return;
		}
		Toast.makeText(this, "Selected: " + selection.getName(),
				Toast.LENGTH_SHORT).show();

		LiveService mLiveService = serviceLink.getLiveService();
		UserSyncModule syncModule = (UserSyncModule) mLiveService
				.getServiceModule(UserSyncModule.class);

		syncModule.put(Fields.PROGRAMME_ID, selection.getProgrammeID());

		finish();
	}

	@Override
	public void run() {
		boolean retry = false;
		while (true) {
			if (serviceLink == null || serviceLink.getLiveService() == null) {

			} else if (retry || !searchQuery[1].equals(searchQuery[0])) {
				runOnUiThread(showProgress);

				retry = false;
				searchQuery[1] = searchQuery[0];

				mResults.clear();

				LiveService mLiveService = serviceLink.getLiveService();
				AccountModule mAccountModule = null;
				mAccountModule = (AccountModule) mLiveService
						.getServiceModule(AccountModule.class);
				AuthResponse authResponse = mAccountModule.getAuthResponse();

				List<Course> responseCourses = null;

				if (authResponse != null) {
					responseCourses = CourseQuery.query(authResponse,
							searchQuery[1]);
				}
				if (responseCourses == null) {

					retry = true;

				} else {

					mResults.addAll(responseCourses);

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
		resultsListView.setVisibility(View.VISIBLE);
		progressSpinner.setVisibility(View.GONE);
	}

	private void hideResultsList() {
		resultsListView.setVisibility(View.GONE);
		progressSpinner.setVisibility(View.VISIBLE);
	}

	class ResultsListAdapter extends BaseAdapter {

		Context context;
		List<Course> r = null;

		class ViewHolder {

		}

		// private LayoutInflater mInflater;

		public ResultsListAdapter(Context context) {
			this.context = context;
			r = Collections.synchronizedList(mResults);
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