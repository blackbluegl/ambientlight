package org.ambient.control.sceneries;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.rest.RestClient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class SceneriesFragment extends Fragment {

	public static final String BUNDLE_SELECTED_ROOM_SERVER = "selectedRoomServer";

	ArrayAdapter<String> sceneriesAdapter;

	private ListView sceneriesListView;


	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		String selectedRoomServer = getArguments().getString(BUNDLE_SELECTED_ROOM_SERVER);

		final String[] sceneryNames = getSceneryNames(selectedRoomServer);

		View sceneriesContainerView = inflater.inflate(R.layout.layout_sceneries_main, container, false);

		sceneriesListView = (ListView) sceneriesContainerView.findViewById(R.id.listViewSceneries);

		sceneriesAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, sceneryNames);
		sceneriesListView.setAdapter(sceneriesAdapter);
		registerForContextMenu(sceneriesListView);
		sceneriesListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg3) {
				String scenery = sceneryNames[position];
				RestClient.setSceneryActive(((MainActivity) getActivity()).getSelectedRoomServer(), scenery,
						((MainActivity) getActivity()).getHomeRefreshCallback());
			}
		});

		return sceneriesContainerView;
	}


	public void updateSceneriesList(String roomServer) {
		final String[] sceneryNames = getSceneryNames(((MainActivity) getActivity()).getSelectedRoomServer());
		sceneriesAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, sceneryNames);
		this.sceneriesListView.setAdapter(sceneriesAdapter);
		sceneriesListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg3) {
				String scenery = sceneryNames[position];
				RestClient.setSceneryActive(((MainActivity) getActivity()).getSelectedRoomServer(), scenery,
						((MainActivity) getActivity()).getHomeRefreshCallback());
			}
		});
	}


	private String[] getSceneryNames(String roomServer) {
		String[] items = null;
		try {
			items = RestClient.getSceneriesForRoom(roomServer);
		} catch (Exception e) {

		}
		return items;
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listViewSceneries) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			if (((TextView) info.targetView).getText().equals(((MainActivity) getActivity()).getSelectedScenario()) == false) {
				menu.setHeaderTitle("Was ist zu tun?");
				menu.add(Menu.NONE, 0, 0, "löschen");
			}
		}
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		if(item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo == false){
			return super.onContextItemSelected(item); 
		}
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == 0) {
			RestClient.deleteScenarioFromRoom(((MainActivity) getActivity()).getSelectedRoomServer(),
					(String) ((TextView) info.targetView).getText());
			updateSceneriesList(((MainActivity) getActivity()).getSelectedRoomServer());
		}
		return true;
	}

}
