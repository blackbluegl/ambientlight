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

package org.ambient.control.home.roomitems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambient.control.R;
import org.ambient.control.home.RoomFragment;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.climate.Climate;
import org.ambientlight.room.entities.features.lightobject.Renderable;
import org.ambientlight.ws.Room;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Adapter to hold all clickable items in the room fragment. Each item can be clicked and long clicked. The behavior and the view
 * representation are hold in strategy classes in the same package.
 * 
 * @author Florian Bornkessel
 * 
 */
@SuppressLint("UseSparseArrays")
public class ItemAdapter extends BaseAdapter {

	Map<Integer, Entity> itemPositions = new HashMap<Integer, Entity>();
	Map<Entity, Strategy> items = new HashMap<Entity, Strategy>();

	Room room;

	private final RoomFragment context;


	public ItemAdapter(List<Entity> entities, Room room, RoomFragment context) {

		this.room = room;
		this.context = context;

		for (Entity entity : entities) {

			if (entity instanceof Renderable && entity instanceof Switchable) {

				RenderingProgramConfiguration sceneryConfig = ((Renderable) entity).getRenderingProgrammConfiguration();

				if (sceneryConfig instanceof SimpleColorRenderingProgramConfiguration) {
					this.addItem(entity, new SimpleColorLightObjectStrategy());
				}

				if (sceneryConfig instanceof TronRenderingProgrammConfiguration) {
					this.addItem(entity, new TronLightIObjectStrategy());
				}

				if (sceneryConfig instanceof SunSetRenderingProgrammConfiguration) {
					this.addItem(entity, new SunsetLightObjectStrategy());
				}

			} else if (entity instanceof Switchable) {
				if (entity.getId().domain.equals(EntityId.DOMAIN_SWITCH_VIRTUAL_MAIN) == false) {
					this.addItem(entity, new SwitchableStrategy());
				}

			} else if (entity instanceof Climate) {
				this.addItem(entity, new ClimateStrategy(room.climateManager));
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return items.size();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int at) {
		return itemPositions.get(at);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}


	/*
	 * create views from layout_room_item.xml. Each strategy holds the concrete view to render and will be put into the
	 * "linearLayoutItemContent" view. The id part of the entity will be set as name below the displayed content.
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Entity entity = itemPositions.get(position);
		final Strategy strategy = items.get(entity);

		LayoutInflater inflater = (LayoutInflater) context.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.layout_room_item, null);

		context.registerForContextMenu(itemView);

		TextView labelView = (TextView) itemView.findViewById(R.id.textRoomIten);
		labelView.setText(entity.getId().id);

		final LinearLayout itemContent = (LinearLayout) itemView.findViewById(R.id.linearLayoutItemContent);
		itemContent.addView(strategy.onCreateView(context, entity));

		itemContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				strategy.onClick(itemContent.getChildAt(0), room, context, entity);
			}
		});

		itemContent.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				strategy.onLongClick(itemContent.getChildAt(0), room, context, entity);
				return true;
			}
		});

		return itemView;
	}


	private void addItem(Entity entity, Strategy strategy) {
		itemPositions.put(items.size(), entity);
		items.put(entity, strategy);
	}
}
