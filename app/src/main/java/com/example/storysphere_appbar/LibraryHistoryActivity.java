package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class LibraryHistoryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private EditText etSearch;
    private LibraryTabFragment libFrag;
    private HistoryTabFragment histFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_history);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        libFrag  = new LibraryTabFragment();
        histFrag = new HistoryTabFragment();
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull @Override public Fragment createFragment(int position) {
                return position == 0 ? libFrag : histFrag;
            }
            @Override public int getItemCount() { return 2; }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) ->
                tab.setText(pos == 0 ? getString(R.string.tab_library) : getString(R.string.tab_history))
        ).attach();

        // ----- Search -----
        View searchPill = findViewById(R.id.searchView_inline);
        if (searchPill != null) {
            etSearch = searchPill.findViewById(R.id.etSearch);
        }
        if (etSearch != null) {
            etSearch.addTextChangedListener(new SimpleWatcher(s -> {
                String q = s == null ? "" : s.toString();
                if (viewPager.getCurrentItem() == 0) libFrag.setQuery(q);
                else                                  histFrag.setQuery(q);
            }));
        }

        // ----- Bottom Navigation -----
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_library);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            } else if (id == R.id.nav_library) {
                return true;
            } else if (id == R.id.nav_writing) {
                startActivity(new Intent(this, activity_writing.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            } else if (id == R.id.nav_activity) {
                startActivity(new Intent(this, UserActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }
            return false;
        });
    }

    // ---------- helper ----------
    static class SimpleWatcher implements TextWatcher {
        interface On { void on(CharSequence s); }
        private final On on;
        SimpleWatcher(On on){ this.on = on; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { on.on(s); }
        @Override public void afterTextChanged(Editable s) {}
    }

    // =============================================================================================
    // Library Tab (Bookmarks)
    // =============================================================================================
    public static class LibraryTabFragment extends Fragment {
        private RecyclerView rvLibrary;
        private BookmarksAdapter adapter;
        private String query = "";

        public void setQuery(String q) { this.query = q == null ? "" : q; reload(); }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_library, container, false);
            rvLibrary = v.findViewById(R.id.rvLibrary);
            rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new BookmarksAdapter(getContext(), new ArrayList<>());
            rvLibrary.setAdapter(adapter);
            return v;
        }

        @Override public void onResume() { super.onResume(); reload(); }

        private void reload() {
            if (getContext() == null) return;
            DBHelper db = new DBHelper(getContext());
            String email = safeEmail(db.getLoggedInUserEmail());
            List<WritingItem> data = db.getBookmarkedWritings(email, query);
            adapter.replace(data);
        }

        private String safeEmail(String e){ return (e==null || e.trim().isEmpty()) ? "guest" : e; }
    }

    // =============================================================================================
    // History Tab
    // =============================================================================================
    public static class HistoryTabFragment extends Fragment {
        private RecyclerView rvHistory;
        private HistoryAdapter adapter;
        private String query = "";

        public void setQuery(String q) { this.query = q == null ? "" : q; reload(); }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_history, container, false);
            rvHistory = v.findViewById(R.id.rvHistory);
            rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new HistoryAdapter(getContext(), new ArrayList<>());
            rvHistory.setAdapter(adapter);
            return v;
        }

        @Override public void onResume() { super.onResume(); reload(); }

        private void reload() {
            if (getContext() == null) return;
            DBHelper db = new DBHelper(getContext());
            String email = safeEmail(db.getLoggedInUserEmail());
            List<DBHelper.HistoryItem> items = db.getReadingHistory(email, query, 200);
            adapter.replace(items);
        }

        private String safeEmail(String e){ return (e==null || e.trim().isEmpty()) ? "guest" : e; }
    }
}
