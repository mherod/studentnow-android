package com.studentnow.android;

import java.util.Locale;

import org.herod.studentnow.service.CourseSelectionModule;
import org.herod.studentnow.service.LiveService;
import org.studentnow.Course;
import org.studentnow.Institution;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.fima.cardsui.views.CardUI;

public class SetupActivity extends FragmentActivity implements
		ViewPager.OnPageChangeListener {

	private LiveServiceLink serviceLink = null;

	private Institution institutionSelection = null;
	private Course courseSelection = null;

	SectionsPagerAdapter mSectionsPagerAdapter;
	static ViewPager mViewPager;

	private static int enabledPages = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.setOnPageChangeListener(this);

		serviceLink = new LiveServiceLink();

	}

	@Override
	public void onResume() {
		super.onResume();
		serviceLink.start(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		serviceLink.stop(this);
	}

	private LiveService live() {
		return serviceLink.getLiveService();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageSelected(int arg0) {
		// invalidateOptionsMenu();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_setup_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_exit:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		/*
		 * if (mViewPager.getCurrentItem() == 2) {
		 * menu.findItem(R.id.action_search).setVisible(true); } else {
		 * menu.findItem(R.id.action_search).setVisible(false); }
		 */
		return true;
	}

	@Override
	public void onBackPressed() {
		if (mViewPager.getCurrentItem() > 0) {
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
			return;
		}
		super.onBackPressed();
	}

	protected void openInstitutionSelection() {
		startActivityForResult(
				new Intent(this, InstitutionSelectActivity.class), 5);
	}

	protected void openCourseSelection() {
		startActivityForResult(
				new Intent(this, CourseSelectActivity.class).putExtra("inst",
						institutionSelection), 6);
	}

	protected void finishSelections() {
		if (institutionSelection == null || courseSelection == null) {
			mViewPager.setCurrentItem(0);
			return;
		}
		CourseSelectionModule csm = (CourseSelectionModule) live()
				.getServiceModule(CourseSelectionModule.class);
		csm.setInstitutionSelection(institutionSelection);
		csm.setCourseSelection(courseSelection);

		Intent i = new Intent(this, CardActivity.class);
		startActivity(i);

		finish();
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == 5 && resultCode == RESULT_OK) {
			Institution in = (Institution) intent.getSerializableExtra("inst");
			if (in != null) {
				institutionSelection = in;
				if (enabledPages < 3) {
					enabledPages = 3;
					mViewPager.setCurrentItem(2);
				}
			}
		} else if (requestCode == 6 && resultCode == RESULT_OK) {
			Course course = (Course) intent.getSerializableExtra("course");
			if (course != null) {
				courseSelection = course;
				if (enabledPages < 4) {
					enabledPages = 4;
					mViewPager.setCurrentItem(3);
				}
			}
		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new WelcomeFragment();
			case 1:
				return new InstitutionSelectionFragment();
			case 2:
				return new CourseSelectionFragment();
			case 3:
				return new FinishFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return enabledPages;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.welcome).toUpperCase(l);
			case 1:
				return getString(R.string.setup_title_selectuni).toUpperCase(l);
			case 2:
				return getString(R.string.select_course).toUpperCase(l);
			case 3:
				return getString(R.string.finish).toUpperCase(l);
			}
			return null;
		}
	}

	public static class WelcomeFragment extends Fragment {

		private CardUI mCards;

		public WelcomeFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.activity_setup_welcome_fragment, container, false);
			mCards = (CardUI) rootView.findViewById(R.id.cardssetup);

			MyCard c1 = new MyCard(getString(R.string.app_name),
					getString(R.string.welcome_introduction));
			MyCard c2 = new MyCard(getString(R.string.swipe_setup),
					getString(R.string.swipe_setup_content));
			mCards.addCard(c1);
			mCards.addCard(c2);
			mCards.setSwipeable(false);
			mCards.refresh();
			return rootView;
		}
	}

	public static class InstitutionSelectionFragment extends Fragment {

		private CardUI mCards;

		public InstitutionSelectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.activity_setup_institution_fragment, container,
					false);

			String title = getString(R.string.setup_select_university);
			String content = getString(R.string.setup_select_university_content);
			mCards = (CardUI) rootView.findViewById(R.id.cardssetup);
			mCards.addCard(new MyCard(title, content));
			mCards.setSwipeable(false);
			mCards.refresh();

			Button choose = (Button) rootView
					.findViewById(R.id.action_select_university);
			choose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SetupActivity parent = (SetupActivity) getActivity();
					parent.openInstitutionSelection();
				}
			});
			return rootView;
		}
	}

	public static class CourseSelectionFragment extends Fragment {

		private CardUI mCards;

		public CourseSelectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.activity_setup_course_fragment, container, false);

			mCards = (CardUI) rootView.findViewById(R.id.cardssetup);
			MyCard c1 = new MyCard(getString(R.string.setup_select_course),
					getString(R.string.setup_select_course_content));
			mCards.addCard(c1);
			mCards.setSwipeable(false);
			mCards.refresh();

			Button choose = (Button) rootView
					.findViewById(R.id.action_select_course);
			choose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SetupActivity parent = (SetupActivity) getActivity();
					parent.openCourseSelection();
				}
			});
			return rootView;
		}
	}

	public static class FinishFragment extends Fragment {

		public FinishFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.activity_setup_finish_fragment, container, false);

			Button choose = (Button) rootView.findViewById(R.id.action_finish);
			choose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SetupActivity parent = (SetupActivity) getActivity();
					parent.finishSelections();
				}
			});
			return rootView;
		}
	}

	public static class CantFindDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.cantfind).setPositiveButton(
					R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dismiss();
						}
					});
			return builder.create();
		}
	}

}
