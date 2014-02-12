/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambient.control.home;

import java.util.List;

import org.ambient.control.R;
import org.ambient.control.config.ConfigBindingHelper;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.rest.RestClient;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author Florian Bornkessel
 * 
 */
public class ActorConductEditFragment extends EditConfigHandlerFragment {

	final public static String ITEM_NAME = "itemName";
	String itemName = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		itemName = getArguments().getString(ITEM_NAME);
		return super.onCreateView(inflater, container, savedInstanceState);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem previewItem = menu.add("Vorschau");
		previewItem.setIcon(R.drawable.ic_preview);
		previewItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		previewItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				RestClient.setRenderingConfiguration(selectedServer, itemName,
						(RenderingProgramConfiguration) myConfigurationData);
				return true;
			}
		});
	}


	/**
	 * @param altValuesForListener
	 * @param alternativeValuesForDisplay
	 * @param myself
	 */
	private static void createNewConfigBean(final List<String> altValues, final CharSequence[] alternativeValuesForDisplay,
			final Fragment fragment, final String server, final RoomConfiguration roomConfig, final String itemName) {

		AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
		builder.setTitle("Bitte auswählen").setItems(alternativeValuesForDisplay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Bundle args = new Bundle();
				args.putString(ARG_CLASS_NAME, altValues.get(which));
				args.putString(ARG_SELECTED_SERVER, server);
				args.putBoolean(ARG_CREATE_MODE, true);
				args.putString(ITEM_NAME, itemName);
				args.putSerializable(ARG_ROOM_CONFIG, roomConfig);
				FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
				ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
				ActorConductEditFragment configHandler = new ActorConductEditFragment();
				configHandler.setTargetFragment(fragment, REQ_RETURN_OBJECT);
				ft.replace(R.id.LayoutMain, configHandler);
				ft.addToBackStack(null);
				configHandler.setArguments(args);
				configHandler.setTargetFragment(fragment, REQ_RETURN_OBJECT);
				ft.commit();
			}
		});
		builder.create().show();
	}


	public static void createNewConfigBean(Class clazz, final Fragment fragment, final String server,
			final RoomConfiguration roomConfiguration, final String itemName) {

		List<String> altValues = ConfigBindingHelper.getAlternativeValues(
				(AlternativeValues) clazz.getAnnotation(AlternativeValues.class), clazz.getName(), roomConfiguration);
		List<String> altValuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(
				(AlternativeValues) clazz.getAnnotation(AlternativeValues.class), clazz.getName(), roomConfiguration);
		createNewConfigBean(altValues, ConfigBindingHelper.toCharSequenceArray(altValuesToDisplay), fragment, server,
				roomConfiguration, itemName);
	}

}
