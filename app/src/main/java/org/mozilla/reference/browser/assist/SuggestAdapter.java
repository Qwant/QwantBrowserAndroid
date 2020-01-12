package org.mozilla.reference.browser.assist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.mozilla.reference.browser.R;

import java.util.ArrayList;

class SuggestAdapter extends ArrayAdapter<SuggestItem> implements Filterable {
    private final static String LOGTAG = "QwantAssist";

    private ArrayList<SuggestItem> suggest_data;
    private HistoryAdapter history_adapter;
    private Context _context;

    SuggestAdapter(Context context, int resource, HistoryAdapter history_adapter) {
        super(context, resource);
        this.suggest_data = new ArrayList<>();
        this.history_adapter = history_adapter;
        _context = context;
    }

    @Override public int getCount() {
        return suggest_data.size();
    }
    @Override public SuggestItem getItem(int index) {
        return suggest_data.get(index);
    }

    @NotNull
    @Override public View getView(int position, View listItemView, ViewGroup parent) {
        if (listItemView == null) {
            listItemView = LayoutInflater.from(_context).inflate(R.layout.assist_suggestlist_item, parent, false);
        }

        SuggestItem item = suggest_data.get(position);

        ImageView image = listItemView.findViewById(R.id.suggest_icon);
        if (item.type == SuggestItem.Type.QWANT_SUGGEST) {
            image.setImageResource(R.drawable.icon_search);
        } else if (item.type == SuggestItem.Type.HISTORY) {
            image.setImageResource(R.drawable.icon_clock);
        } else {
            Log.w(LOGTAG, "unknown suggest type. Keeping default image");
        }

        TextView name = listItemView.findViewById(R.id.suggest_text);
        String display_text = (item.display_text.length() > Assist.MAX_SUGGEST_TEXT_LENGTH) ? item.display_text.substring(0, Assist.MAX_SUGGEST_TEXT_LENGTH) : item.display_text;
        name.setText(display_text);

        return listItemView;
    }

    // Get suggest data from qwant and local history, filtered by "constraint" string
    @NotNull
    @Override public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    try {
                        suggest_data = SuggestRequest.getSuggestions(constraint.toString());
                        suggest_data.addAll(history_adapter.filter_as_suggestitem(constraint.toString()));
                    } catch(Exception e) {
                        Log.e(LOGTAG, "suggest adapter filtering failed: " + e.getMessage());
                    }
                    filterResults.values = suggest_data;
                    filterResults.count = suggest_data.size();
                }
                return filterResults;
            }

            @Override protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
