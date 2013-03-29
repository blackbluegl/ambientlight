package org.ambient.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;

/**
 * This class is a subclass of an imageView and provides additional infos for
 * the contextmenu item selection. Now it is possible to find out which icon the
 * uer clicked on to open the context menu.
 * 
 * @author florian
 * 
 */
public class ImageViewWithContextMenuInfo extends ImageView {
	public ImageViewWithContextMenuInfo(Context context) {
		super(context);
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return new ImageViewContextMenuInfo(this);
	}

	public ImageViewWithContextMenuInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ImageViewWithContextMenuInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public static class ImageViewContextMenuInfo implements ContextMenu.ContextMenuInfo {
		public ImageViewContextMenuInfo(View targetView) {
			this.targetView = (ImageView) targetView;
		}

		public ImageView targetView;
	}
}