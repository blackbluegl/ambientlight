package org.ambient.control;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.ambient.control.home.HomeFragment;
import org.ambient.control.rest.URLUtils;
import org.ambient.control.sceneries.NewSceneryDialogFragment;
import org.ambient.control.sceneries.SceneriesFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	String selectedRoomServer;
	
	String selectedScenario;


	ViewPager viewPager;

	SceneriesFragment sceneriesFragment;
	HomeFragment homeFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.selectedRoomServer = getAllRoomServers().get(0);

		setContentView(R.layout.activity_main);

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When swiping between pages, select the corresponding tab.
				getActionBar().setSelectedNavigationItem(position);
			}
		});

		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getActionBar().addTab(getActionBar().newTab().setText(R.string.title_section_home).setTabListener(this));
		getActionBar().addTab(getActionBar().newTab().setText(R.string.title_activity_sceneries).setTabListener(this));

		Bundle argsSceneries = new Bundle();
		argsSceneries.putString(SceneriesFragment.BUNDLE_SELECTED_ROOM_SERVER, this.selectedRoomServer);
		sceneriesFragment = new SceneriesFragment();
		sceneriesFragment.setArguments(argsSceneries);

		Bundle argsHome = new Bundle();
		argsHome.putStringArrayList(HomeFragment.BUNDLE_HOST_LIST, getAllRoomServers());
		argsHome.putString(HomeFragment.BUNDLE_SELECTED_ROOM_SERVER, this.selectedRoomServer);
		homeFragment = new HomeFragment();
		homeFragment.setArguments(argsHome);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return homeFragment;
			case 1:
				return sceneriesFragment;
			default:
				break;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section_home);
			case 1:
				return getString(R.string.title_activity_sceneries);
			}
			return null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_save:
			FragmentManager fm = getSupportFragmentManager();
			NewSceneryDialogFragment newSceneriesDialog = new NewSceneryDialogFragment();
			Bundle args = new Bundle();
			args.putString(NewSceneryDialogFragment.BUNDLE_SCENERY_NAME, this.selectedScenario);
			newSceneriesDialog.setArguments(args);
			newSceneriesDialog.show(fm, "new Scenery Title");
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// TODO this here should discover real servers in future
	public ArrayList<String> getAllRoomServers() {
		ArrayList<String> result = new ArrayList<String>();
		result.add(URLUtils.ANDROID_ADT_HOSTNAME);
		return result;
	}

	public String getSelectedRoomServer() {
		return selectedRoomServer;
	}

	public void updateSceneriesForSelectedRoomServer(String hostName) {
		this.selectedRoomServer = hostName;
		sceneriesFragment.updateSceneriesList(hostName);
	}

	public void updateHome(String sceneryName) throws InterruptedException, ExecutionException {
		homeFragment.refreshRoomContent(getSelectedRoomServer());
	}


	public String getSelectedScenario() {
		return selectedScenario;
	}

	public void setSelectedScenario(String selectedScenario) {
		this.selectedScenario = selectedScenario;
	}
}
