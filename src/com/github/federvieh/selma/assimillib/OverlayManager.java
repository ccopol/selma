/**
 * 
 */
package com.github.federvieh.selma.assimillib;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.github.federvieh.selma.R;

/**
 * @author frank
 *
 */
public class OverlayManager {
	//TODO: Add method for resetting

	private static final String OVERLAY_PLAYSHOWN = "com.github.federvieh.selma.assimillib.OVERLAY_PLAYSHOWN";
	private static final String OVERLAY_HINTDISPLAYED = "com.github.federvieh.selma.assimillib.OVERLAY_HINTDISPLAYED";
	private static boolean initialized = false;
	private static void init(Context context){
		if(!initialized){
			SharedPreferences settings = context.getSharedPreferences("selma", Context.MODE_PRIVATE);
			playShown = settings.getBoolean(OVERLAY_PLAYSHOWN, false);
			hintDisplayed = settings.getInt(OVERLAY_HINTDISPLAYED, 0);
			initialized = true;
		}
	}

	private static boolean playShown = false;
	public static void showPlayOverlay(Context context){
		init(context);
		if(!playShown){
			final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
			dialog.setContentView(R.layout.overlay_view_playbar_text);
			LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.overlayLayout);

			layout.setOnClickListener(new OnClickListener() {

				@Override

				public void onClick(View arg0) {
					dialog.dismiss();
				}
			});
			dialog.show();
			playShown=true;
			//Store SharedPreferences
			SharedPreferences settings = context.getSharedPreferences("selma", Context.MODE_PRIVATE);
			Editor edit = settings.edit();
			edit.putBoolean(OVERLAY_PLAYSHOWN, playShown);
			edit.commit();
		}
	}

	private static int hintDisplayed = 0;
	public static void showOverlayLessonList(Context context){
		init(context);
		if(hintDisplayed==0){

			final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
			dialog.setContentView(R.layout.overlay_view_starred);
			LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.overlayLayout);
			hintDisplayed = 1;

			layout.setOnClickListener(new OnClickListener() {

				@Override

				public void onClick(View arg0) {
					LinearLayout layout;
					Context ctxt = dialog.getContext();
					switch(hintDisplayed){
					case 1:
						dialog.setContentView(R.layout.overlay_view_top_spinner);
						layout = (LinearLayout) dialog.findViewById(R.id.overlayLayout);
						layout.setOnClickListener(this);
						break;
					case 2:
						dialog.setContentView(R.layout.overlay_view_playbar_buttons);
						layout = (LinearLayout) dialog.findViewById(R.id.overlayLayout);
						layout.setOnClickListener(this);
						break;
					default:
						dialog.dismiss();
					}
					hintDisplayed++;
					//Store SharedPreferences
					SharedPreferences settings = ctxt.getSharedPreferences("selma", Context.MODE_PRIVATE);
					Editor edit = settings.edit();
					edit.putInt(OVERLAY_HINTDISPLAYED, hintDisplayed);
					edit.commit();
				}
			});

			dialog.show();
		}
	}

	public static void resetOverlays() {
		playShown = false;
		hintDisplayed = 0;
	}
}
