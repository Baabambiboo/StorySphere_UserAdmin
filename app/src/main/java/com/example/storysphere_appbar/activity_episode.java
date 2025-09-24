package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

public class activity_episode extends AppCompatActivity {

    private DBHelper db;
    private DrawerLayout drawer;
    private RecyclerView rvEpisodes;

    // BottomSheet Comment
    private BottomSheetBehavior<FrameLayout> commentsSheet;
    private RecyclerView rvComments;

    private TextView tvEpisodeNumber, tvEpisodeTitle, tvEpisodeHeading, tvEpisodeContent;

    private int writingId;
    private int currentEpisodeId;
    private List<Episode> episodeList;   // ✅ ใช้ Episode ภายนอก
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);

        // ปุ่ม back มุมซ้ายบน (ใน Toolbar custom)
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent up = new Intent(activity_episode.this, ReadingMainActivity.class);
                up.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, writingId);
                up.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(up);
            });
        }

        db = new DBHelper(this);

        // รับพารามิเตอร์
        writingId        = getIntent().getIntExtra(ReadingMainActivity.EXTRA_WRITING_ID, -1);
        currentEpisodeId = getIntent().getIntExtra(ReadingMainActivity.EXTRA_EPISODE_ID, -1);

        // Bind views
        drawer            = findViewById(R.id.drawerLayout);
        rvEpisodes        = findViewById(R.id.rvEpisodes);
        tvEpisodeNumber   = findViewById(R.id.tvEpisodeNumber);
        tvEpisodeTitle    = findViewById(R.id.tvEpisodeTitle);
        tvEpisodeHeading  = findViewById(R.id.tvEpisodeHeading);
        tvEpisodeContent  = findViewById(R.id.tvEpisodeContent);

        // Drawer toggle
        View actionEpisodes = findViewById(R.id.actionEpisodes);
        if (actionEpisodes != null) actionEpisodes.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        View btnClose = findViewById(R.id.btnCloseDrawer);
        if (btnClose != null) btnClose.setOnClickListener(v -> drawer.closeDrawer(GravityCompat.START));

        // โหลดตอนทั้งหมดของเรื่อง
        episodeList = db.getEpisodesByWritingId(writingId); // ✅ คืน List<Episode>

        // รายการตอนใน Drawer
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setAdapter(new EpisodeDrawerAdapter(episodeList, ep -> {
            drawer.closeDrawer(GravityCompat.START);
            setPosition(indexOfEpisode(ep.episodeId));
        }));

        // ตำแหน่งเริ่มต้น
        if ((currentEpisodeId <= 0) && episodeList != null && !episodeList.isEmpty()) {
            currentEpisodeId = episodeList.get(0).episodeId;
        }
        setPosition(indexOfEpisode(currentEpisodeId));

        // ปุ่ม Back / Next
        View prev = findViewById(R.id.actionPrev);
        View next = findViewById(R.id.actionNext);
        if (prev != null) prev.setOnClickListener(v -> { if (position > 0) setPosition(position - 1); });
        if (next != null) next.setOnClickListener(v -> {
            if (episodeList != null && position < episodeList.size() - 1) setPosition(position + 1);
        });

        // ตั้งค่า BottomSheet คอมเมนต์
        setupCommentsSheet();
    }

    /** เตรียม BottomSheet คอมเมนต์ + เดินสายปุ่ม */
    private void setupCommentsSheet() {
        FrameLayout sheet = findViewById(R.id.bottomSheetComments);
        if (sheet == null) return;

        commentsSheet = BottomSheetBehavior.from(sheet);
        sheet.setVisibility(View.VISIBLE);
        commentsSheet.setHideable(true);
        commentsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        rvComments = sheet.findViewById(R.id.rvComments);
        if (rvComments != null) rvComments.setLayoutManager(new LinearLayoutManager(this));

        View actionComment = findViewById(R.id.actionComment);
        if (actionComment != null) {
            actionComment.setOnClickListener(v -> {
                if (rvComments != null) {
                    rvComments.setAdapter(new CommentAdapter(loadComments(currentEpisodeId)));
                }
                commentsSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            });
        }
    }

    /** แสดงตอนตาม id + บันทึกประวัติอ่าน (History) */
    private void showEpisode(int episodeId) {
        Episode ep = db.getEpisodeById(episodeId); // ✅ ได้ Episode ภายนอก
        if (ep == null) return;

        currentEpisodeId = ep.episodeId;
        writingId        = ep.writingId;

        if (tvEpisodeNumber != null) tvEpisodeNumber.setText("#" + ep.episodeNo);
        if (tvEpisodeTitle  != null) tvEpisodeTitle.setText(ep.title);
        if (tvEpisodeHeading!= null) tvEpisodeHeading.setText(ep.title);

        // แปลง HTML ให้ TextView
        String html = ep.contentHtml == null ? "" : ep.contentHtml;
        html = normalizeHtmlForTextView(html);
        Spanned sp = androidx.core.text.HtmlCompat.fromHtml(html, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY);

        if (tvEpisodeContent != null) {
            tvEpisodeContent.setText(sp);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                tvEpisodeContent.setJustificationMode(Layout.JUSTIFICATION_MODE_NONE);
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                tvEpisodeContent.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
            }
        }

        // เลื่อนกลับบนสุด
        NestedScrollView ns = findViewById(R.id.scrollContent);
        if (ns != null) ns.smoothScrollTo(0, 0);

        // ✅ บันทึก History ทุกครั้งที่เปิดตอน (ลายเซ็นใหม่: email, writingId, episodeId)
        String email = db.getLoggedInUserEmail();
        if (email == null || email.trim().isEmpty()) email = "guest";
        db.addHistory(email, writingId, ep.episodeId);
    }

    /** ตั้งค่าตำแหน่งตอนใหม่ + อัปเดตหน้าจอ + ปุ่มขอบ */
    private void setPosition(int newPos) {
        if (episodeList == null || episodeList.isEmpty()) return;
        if (newPos < 0) newPos = 0;
        if (newPos > episodeList.size() - 1) newPos = episodeList.size() - 1;

        position = newPos;
        Episode ep = episodeList.get(position);
        showEpisode(ep.episodeId);
        updatePrevNextState();
    }

    /** หา index ของ episodeId ใน list */
    private int indexOfEpisode(int episodeId) {
        if (episodeList == null) return 0;
        for (int i = 0; i < episodeList.size(); i++) {
            if (episodeList.get(i).episodeId == episodeId) return i;
        }
        return 0;
    }

    /** ทำปุ่ม Back/Next จางและ disabled เมื่อถึงขอบ */
    private void updatePrevNextState() {
        View prev = findViewById(R.id.actionPrev);
        View next = findViewById(R.id.actionNext);
        boolean canPrev = position > 0;
        boolean canNext = episodeList != null && position < episodeList.size() - 1;

        if (prev != null) { prev.setEnabled(canPrev); prev.setAlpha(canPrev ? 1f : 0.4f); }
        if (next != null) { next.setEnabled(canNext); next.setAlpha(canNext ? 1f : 0.4f); }
    }

    /** mock data คอมเมนต์ – เปลี่ยนเป็นโหลดจาก DB/Server ได้ */
    private java.util.List<CommentAdapter.Comment> loadComments(int episodeId) {
        java.util.ArrayList<CommentAdapter.Comment> list = new java.util.ArrayList<>();
        list.add(new CommentAdapter.Comment("ak0mlnw5087", "สนุกสุด ๆ เมื่อไหร่จะมาต่อครับ", false));
        list.add(new CommentAdapter.Comment("HANAmai", "ขอบคุณค่ะ อัปโหลดวันละตอนค่ะ", true));
        list.add(new CommentAdapter.Comment("ak0mlnw5087", "รอติดตามครับ!", false));
        return list;
    }

    @Override
    public void onBackPressed() {
        if (commentsSheet != null && commentsSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            commentsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // แปลง <p style="text-align:center/right">...</p> ให้เป็นแท็กที่ TextView รองรับ
    private String normalizeHtmlForTextView(String html) {
        if (html == null) return "";
        html = html.replaceAll("(?is)<p[^>]*text-align\\s*:\\s*center[^>]*>(.*?)</p>", "<center>$1</center>");
        html = html.replaceAll("(?is)<div[^>]*text-align\\s*:\\s*center[^>]*>(.*?)</div>", "<center>$1</center>");
        html = html.replaceAll("(?is)<p[^>]*text-align\\s*:\\s*right[^>]*>(.*?)</p>", "<div align='right'>$1</div>");
        html = html.replaceAll("(?is)<div[^>]*text-align\\s*:\\s*right[^>]*>(.*?)</div>", "<div align='right'>$1</div>");
        html = html.replaceAll("(?is)<span[^>]*text-align\\s*:\\s*center[^>]*>(.*?)</span>", "<center>$1</center>");
        html = html.replaceAll("(?is)<span[^>]*text-align\\s*:\\s*right[^>]*>(.*?)</span>", "<div align='right'>$1</div>");
        return html;
    }
}
