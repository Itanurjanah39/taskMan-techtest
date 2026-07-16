package com.technicaltest.taskman.ui.task;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.technicaltest.taskman.databinding.FragmentTaskBinding;

public class TaskFragment extends Fragment {

    private FragmentTaskBinding binding;

    public TaskFragment() {
        // Required empty public constructor
    }

    public static TaskFragment newInstance() {
        return new TaskFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
