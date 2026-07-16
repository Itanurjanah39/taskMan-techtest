package com.technicaltest.taskman;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.databinding.ActivityMainBinding;
import com.technicaltest.taskman.ui.auth.LoginActivity;
import com.technicaltest.taskman.ui.auth.ProfileFragment;
import com.technicaltest.taskman.ui.main.HomeFragment;
import com.technicaltest.taskman.ui.task.TaskFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // Disable icon tint to allow the active/inactive selector PNG drawables to display in their original colors
        binding.bottomNavigation.setItemIconTintList(null);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_task) {
                selectedFragment = new TaskFragment();
            } else if (itemId == R.id.nav_user) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Load the default fragment (HomeFragment) on first launch
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}