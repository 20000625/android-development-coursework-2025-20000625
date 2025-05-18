package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import uk.ac.hope.mcse.android.coursework.databinding.ActivityMainBinding;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding; // ViewBinding instance


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Finds the NavController from NavHostFragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // This defines top-level destinations. The Up button will not be shown for these.
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.FirstFragment)
                .build();

        // This connects the NavController to the ActionBar.
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if (binding.fab != null) {
            binding.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavController currentNavController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);

                    if (currentNavController.getCurrentDestination() != null &&
                            currentNavController.getCurrentDestination().getId() == R.id.FirstFragment) {


                        currentNavController.navigate(R.id.action_FirstFragment_to_SecondFragment);
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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
            Toast.makeText(this, "Settings clicked (Not implemented)", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}