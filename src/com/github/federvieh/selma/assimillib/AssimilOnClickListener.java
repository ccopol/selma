/**
 * 
 */
package com.github.federvieh.selma.assimillib;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * @author frank
 *
 */
public class AssimilOnClickListener implements OnClickListener {

	public static final String EXTRA_LESSON_POS = "com.github.federvieh.selma.assimillib.EXTRA_LESSON_OBJECT";
	private AssimilLesson lesson;
	private Context context;

	/**
	 * @param context 
	 * @param position 
	 * @param lt 
	 * @param current 
	 * 
	 */
	public AssimilOnClickListener(AssimilLesson lesson, Context context, int position, ListTypes lt) {
		this.lesson = lesson;
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		if(v instanceof ImageView){
			ImageView imageView = (ImageView) v;
			if(lesson.isStarred()){
				Log.i("LT", "unstarring");
				lesson.unstar();
				imageView.setImageResource(android.R.drawable.btn_star_big_off);
			}
			else{
				Log.i("LT", "starring");
				lesson.star();				
				imageView.setImageResource(android.R.drawable.btn_star_big_on);
			}
		}
		else{
			// Go to show lesson activity
	    	Intent intent = new Intent(context, ShowLesson.class);
	    	intent.putExtra(EXTRA_LESSON_POS, AssimilDatabase.getDatabase(null).indexOf(lesson));
	    	context.startActivity(intent);

		}
	}

}
