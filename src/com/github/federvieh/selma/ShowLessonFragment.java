package com.github.federvieh.selma;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import com.github.federvieh.selma.assimillib.AssimilDatabase;
import com.github.federvieh.selma.assimillib.AssimilLesson;
import com.github.federvieh.selma.assimillib.AssimilOnClickListener;
import com.github.federvieh.selma.assimillib.AssimilShowLessonListAdapter;
import com.github.federvieh.selma.assimillib.DisplayMode;
import com.github.federvieh.selma.assimillib.LessonPlayer;
import com.github.federvieh.selma.assimillib.ListTypes;
import com.github.federvieh.selma.assimillib.OverlayManager;

/**
 * A fragment representing a list of lesson tracks.
 */
public class ShowLessonFragment extends ListFragment {

	public static final String LIST_MODE = "LIST_MODE";

	private static final String ARG_LESSON_ID = "ShowLessonFragment.ARG_LESSON_ID";
	private static final String ARG_TRACK_NUMBER = "ShowLessonFragment.ARG_TRACK_NUMBER";

	private AssimilLesson lesson;
	private int tracknumber = -1;

	private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
			  long lessonId = intent.getLongExtra(AssimilOnClickListener.EXTRA_LESSON_ID, -1);
			  Log.d("LT", "ShowLessonFragment.messageReceiver.onReceive() got called with lessonId "+lessonId+". Current lesson's ID is "+lesson.getHeader().getId());
			  if(lessonId == lesson.getHeader().getId()){
				  //Might now be playing a new track, update the list in order to highlight the current track
				  getListView().invalidateViews();
			  }
		  }
		};
	private ShowLessonFragmentListener listener;

	//For now the display mode is not stored as a shared preference, so that
	//after (re-)starting the app, always the original text is shown.
	private static DisplayMode displayMode = DisplayMode.ORIGINAL_TEXT;

	public static ShowLessonFragment newInstance(long lessonId, int trackNumber, ShowLessonFragmentListener listener) {
		ShowLessonFragment fragment = new ShowLessonFragment();
		Bundle args = new Bundle();
		args.putLong(ARG_LESSON_ID, lessonId);
		args.putInt(ARG_TRACK_NUMBER, trackNumber);
		fragment.setArguments(args);
		fragment.listener = listener;
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ShowLessonFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			long lessonId = getArguments().getLong(ARG_LESSON_ID);
			lesson = AssimilDatabase.getLesson(lessonId, getActivity());
			tracknumber = getArguments().getInt(ARG_TRACK_NUMBER);
		}

		ListTypes lt = LessonPlayer.getListType();
		AssimilShowLessonListAdapter assimilShowLessonListAdapter;
		assimilShowLessonListAdapter = new AssimilShowLessonListAdapter(getActivity(), lesson, lt, displayMode);
		
		// needed to indicate that the back
		// button in action bar is used
	    setHasOptionsMenu(true); 

		setListAdapter(assimilShowLessonListAdapter);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		if(tracknumber >= 0){
			this.setSelection(tracknumber);
			tracknumber = -1;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Register mMessageReceiver to receive messages.
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReceiver,
				new IntentFilter(LessonPlayer.PLAY_UPDATE_INTENT));

		listener.onResumedTitleUpdate(lesson.getNumber());
	}

	public void updateListType(ListTypes lt){
    	LessonPlayer.setListType(lt);
		Editor editor = getActivity().getSharedPreferences("selma", Context.MODE_PRIVATE).edit();
		editor.putInt(LIST_MODE, lt.ordinal());
		editor.commit();
		Log.d("LT", "ShowLesson.updateListType(); lt="+lt.ordinal());

		AssimilShowLessonListAdapter assimilShowLessonListAdapter;
		assimilShowLessonListAdapter = new AssimilShowLessonListAdapter(getActivity(), lesson, lt, displayMode);
		setListAdapter(assimilShowLessonListAdapter);

		//FIXME: Contextmenu
//		registerForContextMenu(playbar.findViewById(R.id.playmode));
//		registerForContextMenu(listView);
		
		OverlayManager.showOverlayLessonContent(getActivity());
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.text_view, menu);
	    super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPause() {
	  // Unregister since the activity is not visible
	  LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiver);
	  super.onPause();
	} 
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
    	LessonPlayer.play(lesson, position, false, v.getContext());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {   
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            //called when the up affordance/carat in actionbar is pressed
	            getActivity().onBackPressed();
	            return true;
	        case R.id.view_original_text:
	            displayMode = DisplayMode.ORIGINAL_TEXT;
	            updateListType(LessonPlayer.getListType());
	            return true;
	        case R.id.view_translation:
	            displayMode  = DisplayMode.TRANSLATION;
	            updateListType(LessonPlayer.getListType());
	            return true;
	        case R.id.view_literal:
	            displayMode = DisplayMode.LITERAL;
	            updateListType(LessonPlayer.getListType());
	            return true;

	    }
		return false;
	}
}
