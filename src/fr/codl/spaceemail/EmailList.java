package fr.codl.spaceemail;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

public class EmailList extends Fragment implements OnRefreshListener {
	private SwipeRefreshLayout swipe;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_list_fragment, container, false);
        swipe = (SwipeRefreshLayout) view;
        swipe.setColorSchemeResources(R.color.se_orange, R.color.dark_grey, R.color.se_orange, R.color.dark_grey);
        swipe.setOnRefreshListener(this);
        
        return view;
    }
	
	

	@Override
	public void onRefresh() {
		Log.i("EmailList", "Refreshing…");
		MainActivity main = (MainActivity) getActivity();
		if(main != null) main.refresh();
		else swipe.setRefreshing(false);
	}
}
