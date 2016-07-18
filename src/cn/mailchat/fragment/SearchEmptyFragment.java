package cn.mailchat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.mailchat.R;

public class SearchEmptyFragment extends Fragment {
	private static final String EXTRA_NODATA_TIPS = "extra_nodata_tips";

	public static SearchEmptyFragment newInstance(String tips) {
		SearchEmptyFragment fragment = new SearchEmptyFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_NODATA_TIPS, tips);
		fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view=inflater.inflate(R.layout.include_empty_view, null);
    	TextView tv=(TextView) view.findViewById(R.id.tv_empty_view);
    	tv.setText( getArguments().getString(EXTRA_NODATA_TIPS));
        return view;
    }

}