package com.example.storysphere_appbar;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PopupUtil {

    /** เรียกด้วย Activity ได้เลย */
    public static void showConfirm(Activity activity, String message, Runnable onConfirm) {
        if (activity == null || activity.isFinishing()) return;
        View root = activity.findViewById(android.R.id.content);
        showConfirm(root, message, onConfirm);
    }

    /** เรียกด้วย View root ก็ได้ (เผื่อบางหน้าใช้ root) */
    public static void showConfirm(View rootView, String message, Runnable onConfirm) {
        if (rootView == null) return;

        View popupView = rootView.findViewById(R.id.confirm_popup_container);
        if (popupView == null) return;

        TextView tvMessage = popupView.findViewById(R.id.tv_popup_message);
        Button btnYes      = popupView.findViewById(R.id.btn_popup_yes);
        Button btnNo       = popupView.findViewById(R.id.btn_popup_no);

        tvMessage.setText(message);
        btnYes.setText("ใช่");
        btnNo.setText("ไม่");

        // ดัน overlay ขึ้นบนสุดแล้วแสดง
        popupView.bringToFront();
        popupView.setVisibility(View.VISIBLE);
        popupView.requestLayout();

        btnNo.setOnClickListener(v -> popupView.setVisibility(View.GONE));
        btnYes.setOnClickListener(v -> {
            popupView.setVisibility(View.GONE);
            if (onConfirm != null) onConfirm.run();
        });
    }
}
