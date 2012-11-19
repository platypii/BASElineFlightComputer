package com.platypii.baseline.ui;

import com.platypii.baseline.data.Export;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.R;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class JumpFragment extends Fragment {

    private Context context;
    private Jump jump;
    
    // Views
    private JumpPlot jumpPlot;
    private ProgressBar jumpLoading;
    private TextView idLabel;
    private TextView startLabel;
    private TextView endLabel;
    private TextView exitAltLabel;
    private TextView freefallTimeLabel;
    
    
    // Create a new instance
    public static JumpFragment newInstance(Jump jump) {
        JumpFragment frag = new JumpFragment();
        // Supply jump input as an argument
        Bundle args = new Bundle();
        args.putParcelable("jump", jump);
        frag.setArguments(args);
        return frag;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        bundle = this.getArguments();
        jump = bundle.getParcelable("jump");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.jump, container, false);
        
        // Find views
        jumpPlot = (JumpPlot) view.findViewById(R.id.jumpView);
        jumpLoading = (ProgressBar) view.findViewById(R.id.jumpLoading);
        idLabel = (TextView) view.findViewById(R.id.idLabel);
        startLabel = (TextView) view.findViewById(R.id.startLabel);
        endLabel = (TextView) view.findViewById(R.id.endLabel);
        exitAltLabel = (TextView) view.findViewById(R.id.exitAltLabel);
        freefallTimeLabel = (TextView) view.findViewById(R.id.freefallTimeLabel);
        Button kmlButton = (Button) view.findViewById(R.id.exportKmlButton);
        Button csvButton = (Button) view.findViewById(R.id.exportCsvButton);
        Button postButton = (Button) view.findViewById(R.id.shareJumpButton);

        // Load jump data into view
        update();
        	
        // Load jump in background
    	new AsyncTask<Void,Void,Void>() {
    		@Override
    		protected void onPreExecute() {
    			jumpLoading.setVisibility(View.VISIBLE);
    		}
			@Override
			protected Void doInBackground(Void... arg0) {
		    	jumpPlot.loadJump(jump);
				return null;
			}
			@Override
            protected void onPostExecute(Void result) {
				update();
    			jumpLoading.setVisibility(View.INVISIBLE);
           }
    	}.execute();

    	
    	// Called when the user clicks Export KML
        kmlButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Export.writeKml(context, jump);
            }
        });
                
        // Called when the user clicks Export CSV
        csvButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Export.writeCsv(context, jump);
            }
        });
        
        // Called when the user clicks Share Jump
        postButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Export.writeCsv(context, jump);
                String filename = jump.jumpName + ".csv";
                String response = AjaxPostJump.postJump(filename);
            }
        });
        
        return view;
    }

    private void update() {
        // Load jump into view
        if(jump != null) {
        	jumpPlot.invalidate();
            idLabel.setText(jump.jumpName);
            // start
            if(jump.dataStart != Long.MAX_VALUE)
            	startLabel.setText(DateFormat.format("MM/dd/yyyy\nkk:mm:ss", jump.dataStart));
            else if(jump.timeStart <= 0)
            	startLabel.setText("...\n");
            else
            	startLabel.setText(DateFormat.format("MM/dd/yyyy\nkk:mm:ss", jump.timeStart));
            // end
            if(jump.dataEnd != Long.MIN_VALUE)
            	endLabel.setText(DateFormat.format("MM/dd/yyyy\nkk:mm:ss", jump.dataEnd));
            else if(jump.timeEnd == Long.MAX_VALUE)
            	endLabel.setText("...\n");
            else
            	endLabel.setText(DateFormat.format("MM/dd/yyyy\nkk:mm:ss", jump.timeEnd));
            exitAltLabel.setText("Max alt: " + Convert.distance(jump.exitAlt));
            freefallTimeLabel.setText("Freefall time: " + Convert.time3(jump.freefallTime));
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is killed and restarted.
      savedInstanceState.putParcelable("jump", jump);
      super.onSaveInstanceState(savedInstanceState);
    }

}



