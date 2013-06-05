package org.wordpress.android.ui.accounts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import org.wordpress.android.R;
import org.wordpress.android.util.WPViewPager;


public class NewAccountActivity extends FragmentActivity {
    /**
     * The number of pages (wizard steps)
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private WPViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    //keep references to single page here
    NewUserPageFragment userFragment;
    NewBlogPageFragment blogFragment;
    NewAccountReviewPageFragment accountReviewFragment;
    
    public String validatedUsername = null;
    public String validatedPassword = null;
    public String validatedEmail = null;
    public String validatedBlogURL = null;
    public String validatedBlogTitle = null;
    public String validatedLanguageID = null;
    public String validatedPrivacyOption = null;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_account_screen_pager);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (WPViewPager) findViewById(R.id.pager);
        mPagerAdapter = new NewAccountPagerAdapter( super.getSupportFragmentManager() );
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                NewAccountActivity.this.supportInvalidateOptionsMenu();
                
                if(position == 2)
                    accountReviewFragment.updateUI();
            }
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        if ( mPager.getCurrentItem() == 0 )
            return false; //Do not show the menu on the first page :)
        
        getMenuInflater().inflate(R.menu.activity_screen_slide, menu);

        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);

        // Add either a "next" or "finish" button to the action bar, depending on which page
        // is currently selected.
       /* MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                        ? R.string.action_finish
                        : R.string.action_next);
        //item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                //NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return false;

            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                return true;

            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    public void showNextItem() {
        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
    }
    
    public void showPrevItem() {
        if ( mPager.getCurrentItem() == 0 )
            return;
        mPager.setCurrentItem(mPager.getCurrentItem() - 1);
    }
    
    private class NewAccountPagerAdapter extends FragmentStatePagerAdapter {
       
        public NewAccountPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            NewAccountAbstractPageFragment currentPage = null;
            Bundle args = new Bundle();
            args.putInt(NewAccountAbstractPageFragment.ARG_PAGE, position);
            
            switch (position) {
                case 0:
                    userFragment = new NewUserPageFragment();
                    currentPage = userFragment;
                    break;
                case 1:
                    blogFragment = new NewBlogPageFragment();
                    currentPage = blogFragment;
                    break;
                case 2:
                    accountReviewFragment = new NewAccountReviewPageFragment();
                    currentPage = accountReviewFragment;
                    break;
                default:
                    currentPage = new NewBlogPageFragment();
                    break;
            }

            currentPage.setArguments(args);
            return currentPage;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}