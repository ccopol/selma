/**
 * 
 */
package com.github.federvieh.selma;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.federvieh.selma.assimillib.AssimilDatabase;

/**
 * @author frank
 *
 */
public class CourseListAdapter extends ArrayAdapter<String> {

	private ArrayList<String> allCourses;
	private NavigationDrawerFragment callback;

	/**
	 * @param allCourses
	 */
	public CourseListAdapter(Context ctxt, ArrayList<String> allCourses, NavigationDrawerFragment navigationDrawerFragment) {
		super(ctxt, R.layout.course_navigation_item, R.id.courseName, allCourses);
		this.allCourses = allCourses;
		this.callback = navigationDrawerFragment;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = parent.getContext();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.course_navigation_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.courseName);
		String current = allCourses.get(position);
		textView.setText(current);
		rowView.setClickable(false);
		//Set OnClickListeners
		View tvAll = rowView.findViewById(R.id.textViewAllLessons);
		View tvStarred = rowView.findViewById(R.id.textViewStarredLessons);
		tvAll.setOnClickListener(new CourseSelectOnClickListener(current, false, callback));
		tvStarred.setOnClickListener(new CourseSelectOnClickListener(current, true, callback));
		if(current.equals(AssimilDatabase.getLang())){
			if(AssimilDatabase.isStarredOnly()){
				if(android.os.Build.VERSION.SDK_INT>=16){
					tvAll.setBackground(tvAll.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
					tvStarred.setBackground(tvStarred.getResources().getDrawable(R.drawable.abc_list_selector_background_transition_holo_dark));
				}
				else{
					tvAll.setBackgroundDrawable(tvAll.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
					tvStarred.setBackgroundDrawable(tvStarred.getResources().getDrawable(R.drawable.abc_list_selector_background_transition_holo_dark));
				}
			}
			else{
				if(android.os.Build.VERSION.SDK_INT>=16){
					tvStarred.setBackground(tvStarred.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
					tvAll.setBackground(tvAll.getResources().getDrawable(R.drawable.abc_list_selector_background_transition_holo_dark));
				}
				else{
					tvStarred.setBackgroundDrawable(tvStarred.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
					tvAll.setBackgroundDrawable(tvAll.getResources().getDrawable(R.drawable.abc_list_selector_background_transition_holo_dark));
				}
			}
		}
		else{
			if(android.os.Build.VERSION.SDK_INT>=16){
				tvAll.setBackground(tvAll.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
				tvStarred.setBackground(tvStarred.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
			}
			else{
				tvAll.setBackgroundDrawable(tvAll.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
				tvStarred.setBackgroundDrawable(tvStarred.getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
			}
		}

		return rowView;
	}
}
