package com.github.federvieh.selma;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import com.github.federvieh.selma.assimillib.*;

/**
 * A fragment representing a list of lesson tracks.
 */
public class ShowLessonFragment extends Fragment {

    public static final String LIST_MODE = "LIST_MODE";

    private static final String ARG_LESSON_ID = "ShowLessonFragment.ARG_LESSON_ID";
    private static final String ARG_TRACK_NUMBER = "ShowLessonFragment.ARG_TRACK_NUMBER";

    private AssimilLesson lesson;
    private int tracknumber = -1;

    protected RecyclerView mRecyclerView;
    protected AssimilShowLessonListAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;


    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        private long lastPlayedLessonId = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            long lessonId = intent.getLongExtra(AssimilOnClickListener.EXTRA_LESSON_ID, -1);
            Log.d("LT", "ShowLessonFragment.messageReceiver.onReceive() got called with lessonId " + lessonId +
                    ". Current lesson's ID is " + lesson.getHeader().getId() + ". Last lesson ID is " + lastPlayedLessonId);
            long curShownLessonId = lesson.getHeader().getId();
            if (lessonId == curShownLessonId) {
                //Might now be playing a new track, update the list in order to highlight the current track
                mAdapter.notifyItemChanged(LessonPlayer.getPreviousTrack());
                mAdapter.notifyItemChanged(LessonPlayer.getTrackNumber(null));
            } else if (lessonId != lastPlayedLessonId) {
                //Currently one item is shown in bold, but we are now playing a different
                //lesson. So, the list has to be re-drawn.
                //TODO: Test me!
                mAdapter.notifyDataSetChanged();
            }
            lastPlayedLessonId = lessonId;
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
        Log.i("LT", this.getClass().getSimpleName() + ".onCreate(); savedInstanceState=" + savedInstanceState);

        if (getArguments() != null) {
            long lessonId = getArguments().getLong(ARG_LESSON_ID);
            lesson = AssimilDatabase.getLesson(lessonId, getActivity());
            tracknumber = getArguments().getInt(ARG_TRACK_NUMBER);
        }

        ListTypes lt = LessonPlayer.getListType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
//        rootView.setTag(TAG);//FIXME: What is this?

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        setRecyclerViewLayoutManager();

        mAdapter = new AssimilShowLessonListAdapter(lesson, displayMode, LessonPlayer.getListType());
        // Set AssimilShowLessonListAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        // needed to indicate that the back
        // button in action bar is used
        setHasOptionsMenu(true);


        return rootView;
    }


    /**
     * Set RecyclerView's LayoutManager to the one given.
     */
    public void setRecyclerViewLayoutManager() {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (tracknumber >= 0) {
            //FIXME: Something to do?
//            this.setSelection(tracknumber);
//            tracknumber = -1;
        }
        registerForContextMenu(mRecyclerView);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ShowLessonFragmentListener) activity;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater mi = new MenuInflater(v.getContext());
        mi.inflate(R.menu.translate, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int pos = info.position;
        switch (item.getItemId()) {
            case R.id.add_translation:
            case R.id.add_original_text:
            case R.id.add_literal: {
                final EditText translateEditText = new EditText(getActivity());
                final Context ctxt = getActivity();
                int title = R.string.change_translation;
                DisplayMode dm = DisplayMode.TRANSLATION;
                DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lesson.setTranslateText(pos, translateEditText.getText().toString(), ctxt);
                    }
                };
                if (item.getItemId() == R.id.add_literal) {
                    title = R.string.change_literal;
                    dm = DisplayMode.LITERAL;
                    ocl = new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            lesson.setLiteralText(pos, translateEditText.getText().toString(), ctxt);
                        }
                    };
                }
                if (item.getItemId() == R.id.add_original_text) {
                    title = R.string.change_original_text;
                    dm = DisplayMode.ORIGINAL_TEXT;
                    ocl = new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            lesson.setOriginalText(pos, translateEditText.getText().toString(), ctxt);
                        }
                    };
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(title);
                builder.setMessage(lesson.getTextList(DisplayMode.ORIGINAL_TEXT)[pos]);
                translateEditText.setText(lesson.getTextList(dm)[pos]);
                builder.setView(translateEditText);
                builder.setPositiveButton(getText(R.string.ok), ocl);
                builder.setNegativeButton(getText(R.string.cancel), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Nothing to do
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReceiver,
                new IntentFilter(LessonPlayer.PLAY_UPDATE_INTENT));

        String number = lesson.getNumber();
        listener.onResumedTitleUpdate(number);
    }

    public void updateListType(ListTypes lt) {
        LessonPlayer.setListType(lt);
        Editor editor = getActivity().getSharedPreferences("selma", Context.MODE_PRIVATE).edit();
        editor.putInt(LIST_MODE, lt.ordinal());
        editor.commit();
        Log.d("LT", "ShowLesson.updateListType(); lt=" + lt.ordinal());

        mAdapter = new AssimilShowLessonListAdapter(lesson, displayMode, lt);
        // Set AssimilShowLessonListAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
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
                displayMode = DisplayMode.TRANSLATION;
                updateListType(LessonPlayer.getListType());
                return true;
            case R.id.view_literal:
                displayMode = DisplayMode.LITERAL;
                updateListType(LessonPlayer.getListType());
                return true;
            case R.id.view_original_translation:
                displayMode = DisplayMode.ORIGINAL_TRANSLATION;
                updateListType(LessonPlayer.getListType());
                return true;
            case R.id.view_original_literal:
                displayMode = DisplayMode.ORIGINAL_LITERAL;
                updateListType(LessonPlayer.getListType());
                return true;
//            case R.id.add_to_flashcard: {
//                final ContentResolver cr = getActivity().getContentResolver();
//                /*
//                 * First let's look for the model, in case we have to add a new note.
//                 */
//                String[] columns = {FlashCardsContract.Model._ID, FlashCardsContract.Model.NAME};
//                // Query all available models
//                final Cursor allModelsCursor = cr.query(FlashCardsContract.Model.CONTENT_URI, columns, null, null, null);
//                if (allModelsCursor == null) {
//                    //No models: It's very likely that the user does not have AnkiDroid installed
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                    builder.setTitle(getText(R.string.note));
//                    builder.setMessage(getText(R.string.no_flashcard_app));
//                    builder.setPositiveButton(getText(R.string.install), new OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            try {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ichi2.anki")));
//                            } catch (android.content.ActivityNotFoundException anfe) {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.ichi2.anki")));
//                            }
//                        }
//                    });
//                    builder.setNegativeButton(getText(R.string.cancel), new OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            //Nothing to do
//                        }
//                    });
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                    return true;
//                }
//                int idColumnIndex = allModelsCursor.getColumnIndexOrThrow(FlashCardsContract.Model._ID);
//                int nameColumnIndex = allModelsCursor.getColumnIndexOrThrow(FlashCardsContract.Model.NAME);
//                long modelId = -1;
//                try {
//                    while (allModelsCursor.moveToNext()) {
//                        String modelName = allModelsCursor.getString(nameColumnIndex);
//                        if (!modelName.equals(AnkiInterface.MODELNAME)) {
//                            continue;
//                        }
//                        //else we found the right model
//                        modelId = allModelsCursor.getLong(idColumnIndex);
//                    }
//                } finally {
//                    allModelsCursor.close();
//                }
//                if (modelId < 0) {
//                    //FIXME: We should insert the model, for now just show a dialog and exit
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                    builder.setTitle(getText(R.string.note));
//                    builder.setMessage("No model found, cannot copy data to flash card.");//FIXME: This part of the code is to be removed or strings to be added to xml
//                    builder.setNegativeButton(getText(R.string.cancel), new OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            //Nothing to do
//                        }
//                    });
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                    return true;
//                }
//
//                return true;
//            }
        }
        return false;
    }
}
