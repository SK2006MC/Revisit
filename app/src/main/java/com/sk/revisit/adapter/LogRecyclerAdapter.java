package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;

import java.util.List;

public class LogRecyclerAdapter extends RecyclerView.Adapter<LogRecyclerAdapter.LogViewHolder> {

	private List<String[]> logs;

	public LogRecyclerAdapter(List<String[]> logs) {
		this.logs = logs;
	}

	@NonNull
	@Override
	public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
		return new LogViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
		String[] log = logs.get(position);
		holder.bind(log);
	}

	@Override
	public int getItemCount() {
		return logs.size();
	}

	public void setLogs(List<String[]> logs) {
		this.logs = logs;
	}

	public class LogViewHolder extends RecyclerView.ViewHolder {

		private final TextView logTagTextView;
		private final TextView logMessageTextView;
		private final TextView logExceptionTextView;

		LogViewHolder(@NonNull View itemView) {
			super(itemView);
			logTagTextView = itemView.findViewById(R.id.log_tag_text_view);
			logMessageTextView = itemView.findViewById(R.id.log_message_text_view);
			logExceptionTextView = itemView.findViewById(R.id.log_exception_text_view);
		}

		void bind(String[] log) {
			logTagTextView.setText(log[0]);
			logMessageTextView.setText(log[1]);

			if (log.length > 2 && log[2] != null && !log[2].isEmpty()) {
				logExceptionTextView.setText(log[2]);
				logExceptionTextView.setVisibility(View.VISIBLE);
			} else {
				logExceptionTextView.setVisibility(View.GONE);
			}
		}
	}
}