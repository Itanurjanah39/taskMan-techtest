package com.technicaltest.taskman.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.databinding.ItemTaskBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskResponse> taskList = new ArrayList<>();
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onEditClick(TaskResponse task);
        void onDeleteClick(TaskResponse task);
    }

    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<TaskResponse> tasks) {
        this.taskList.clear();
        if (tasks != null) {
            this.taskList.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(taskList.get(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;

        public TaskViewHolder(@NonNull ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TaskResponse task) {
            binding.tvTitle.setText(task.getTitle());
            binding.tvDescription.setText(task.getDescription());

            // Format deadline date if possible
            binding.tvDeadline.setText(formatDate(task.getDeadline()));

            // Setup status styling
            String status = task.getStatus() != null ? task.getStatus().trim() : "Pending";
            if (status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Selesai")) {
                binding.tvStatus.setText("Done");
                binding.tvStatus.setBackgroundResource(R.drawable.bg_done);
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.done_text));
                binding.statusStrip.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.success));
            } else {
                binding.tvStatus.setText("Pending");
                binding.tvStatus.setBackgroundResource(R.drawable.bg_pending);
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.pending_text));
                binding.statusStrip.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.warning));
            }

            // Click Listeners
            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(task);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(task);
                }
            });
        }

        private String formatDate(String rawDate) {
            if (rawDate == null || rawDate.isEmpty()) return "";
            try {
                // Try standard API format e.g. "2024-05-20T10:00:00Z" or "2024-05-20"
                SimpleDateFormat parser;
                if (rawDate.contains("T")) {
                    parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                } else {
                    parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }
                Date date = parser.parse(rawDate);
                if (date != null) {
                    // Format as e.g. "20 Mei 2024"
                    Locale idLocale = new Locale("in", "ID");
                    SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy", idLocale);
                    return formatter.format(date);
                }
            } catch (Exception e) {
                // Fallback to raw string if parsing fails
            }
            return rawDate;
        }
    }
}
