package org.mozilla.reference.browser.assist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.mozilla.reference.browser.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private static final String LOGTAG = "QwantAssist";

    private static final String QWANT_HISTORY_FILENAME = "qwant_assist_history";
    private static final int QWANT_MAX_HISTORY = 100;
    private static final int QWANT_MAX_HISTORY_DISPLAY = 10;

    private final Assist assist_activity;
    private ArrayList<String> history_data = new ArrayList<>(QWANT_MAX_HISTORY);
    private final LinearLayout history_layout;

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout item_layout;
        final TextView item_text;

        HistoryViewHolder(LinearLayout v) {
            super(v);
            item_layout = v;
            item_text = item_layout.findViewById(R.id.suggest_text);
            ImageView item_logo = item_layout.findViewById(R.id.suggest_icon);
            item_logo.setImageResource(R.drawable.icon_clock);
        }
    }

    HistoryAdapter(Assist assist_activity, LinearLayout history_layout) {
        this.history_layout = history_layout;
        this.assist_activity = assist_activity;
        this.read_from_disk();
        if (history_data.isEmpty()) {
            this.history_layout.setVisibility(View.GONE);
        }
    }

    void add_history_item(String s) {
        // remove duplicate
        int duplicate_index = history_data.indexOf(s);
        if (duplicate_index != -1) {
            history_data.remove(duplicate_index);
        }
        // remove last item if Max limit reached
        if (history_data.size() == QWANT_MAX_HISTORY) {
            history_data.remove(QWANT_MAX_HISTORY - 1);
        }
        // push new item in front of the list
        history_data.add(0, s);

        this.notifyDataSetChanged();
        if (!history_data.isEmpty() && this.history_layout.getVisibility() == View.GONE) {
            this.history_layout.setVisibility(View.VISIBLE);
        }
    }

    void clear_history() {
        history_data.clear();
        this.write_on_disk();
        this.notifyDataSetChanged();
        if (this.history_layout.getVisibility() == View.VISIBLE) {
            this.history_layout.setVisibility(View.GONE);
        }
    }

    void write_on_disk() {
        try {
            FileOutputStream file_output_stream = this.assist_activity.getApplicationContext().openFileOutput(QWANT_HISTORY_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream object_output_stream = new ObjectOutputStream(file_output_stream);
            object_output_stream.writeObject(history_data);
            object_output_stream.flush();
            object_output_stream.close();
            file_output_stream.close();
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to save history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    void read_from_disk() {
        try {
            FileInputStream file_input_stream = this.assist_activity.getApplicationContext().openFileInput(QWANT_HISTORY_FILENAME);
            ObjectInputStream object_input_stream = new ObjectInputStream(file_input_stream);
            history_data = (ArrayList<String>) object_input_stream.readObject();
            object_input_stream.close();
            file_input_stream.close();

            // QWANT_MAX_HISTORY may change from one release to another, so this apply it on already saved history
            if (history_data.size() > QWANT_MAX_HISTORY) {
                history_data = new ArrayList<>(history_data.subList(0, QWANT_MAX_HISTORY - 1));
            }

            this.notifyDataSetChanged();
            if (!history_data.isEmpty() && this.history_layout.getVisibility() == View.GONE) {
                this.history_layout.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            Log.e(LOGTAG, "Failed reading history file: IO exception: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(LOGTAG, "Failed reading history file: Class not found: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed reading history file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    ArrayList<SuggestItem> filter_as_suggestitem(String filter_string) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        for (String history_item: history_data) {
            if (history_item.contains(filter_string)) result.add(new SuggestItem(SuggestItem.Type.HISTORY, history_item));
        }
        return result;
    }

    @NotNull
    @Override public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int view_type) {
        LinearLayout item_layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.assist_suggestlist_item, parent, false);
        return new HistoryViewHolder(item_layout);
    }

    @Override public void onBindViewHolder(HistoryViewHolder historyViewHolder, int i) {
        String item_text = history_data.get(i);
        historyViewHolder.item_text.setText(item_text);
        historyViewHolder.item_layout.setOnClickListener(v1 -> {
                this.assist_activity.launch_search(item_text);
        });
    }

    @Override public int getItemCount() {
        return Math.min(history_data.size(), QWANT_MAX_HISTORY_DISPLAY);
    }
}
