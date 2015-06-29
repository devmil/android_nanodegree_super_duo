package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
    private final String save_tag = "Save Test";
    private PagerFragment my_main;

    private static final String ARG_DAY_OFFSET = "DAY_OFFSET";

    public static Intent createFillIntent(int dayOffset) {
        Bundle extras = new Bundle();
        extras.putInt(ARG_DAY_OFFSET, dayOffset);

        Intent result = new Intent();
        result.putExtras(extras);

        return result;
    }

    public static Intent createLaunchIntent(Context context, int dayOffest) {
        Intent result = new Intent(context, MainActivity.class);
        result.putExtra(ARG_DAY_OFFSET, dayOffest);
        return result;
    }

    public static Intent createLaunchIntent(Context context) {
        Intent result = new Intent(context, MainActivity.class);
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            int dayOffset = 0;
            if(getIntent() != null
                    && getIntent().hasExtra(ARG_DAY_OFFSET)) {
                dayOffset = getIntent().getIntExtra(ARG_DAY_OFFSET, 0);
            }

            my_main = new PagerFragment();
            Bundle args = new Bundle();
            args.putInt(PagerFragment.ARG_INITIAL_FRAGMENT_OFFSET, dayOffset);
            my_main.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_main_container, my_main)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(save_tag,"will save");
        Log.v(save_tag,"fragment: "+String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        outState.putInt("Pager_Current",my_main.mPagerHandler.getCurrentItem());
        getSupportFragmentManager().putFragment(outState,"my_main",my_main);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        Log.v(save_tag,"will retrive");
        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt("Pager_Current")));
        Log.v(save_tag,"selected id: "+savedInstanceState.getInt("Selected_match"));
        my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,"my_main");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
