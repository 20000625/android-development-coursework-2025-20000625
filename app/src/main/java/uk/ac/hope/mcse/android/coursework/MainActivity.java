// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/MainActivity.java
package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar; // Keep for optional messages
import androidx.appcompat.app.AppCompatActivity;
import android.view.View; // Keep for View.OnClickListener
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import uk.ac.hope.mcse.android.coursework.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Sets content description for accessibility
        binding.fab.setContentDescription(getString(R.string.fab_add_entry_description));

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ensures NavController is available
                NavController currentNavController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);
                if (currentNavController.getCurrentDestination() != null &&
                        currentNavController.getCurrentDestination().getId() == R.id.FirstFragment) {
                    currentNavController.navigate(R.id.action_FirstFragment_to_SecondFragment);
                } else {

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // TODO: Implement settings screen or action
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}