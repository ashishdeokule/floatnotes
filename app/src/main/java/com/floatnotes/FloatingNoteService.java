package com.floatnotes;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import androidx.core.app.NotificationCompat;
import java.util.*;

public class FloatingNoteService extends Service {

    private static final String CHANNEL_ID = "float_notes_channel";
    private static final int NOTIF_ID = 42;

    private WindowManager windowManager;
    private View bubbleView;
    private List<FloatingNote> activeNotes = new ArrayList<>();
    private int noteCounter = 0;

    // Global settings
    private float globalAlpha = 0.92f;
    private float globalTextSize = 15f; // sp
    private int globalBgColor = Color.parseColor("#1A1A2E");

    // Note colors palette
    private int[] noteColors = {
        Color.parseColor("#1A1A2E"),
        Color.parseColor("#16213E"),
        Color.parseColor("#1F1B33"),
        Color.parseColor("#0F3460"),
        Color.parseColor("#1B2838"),
        Color.parseColor("#2C1810"),
    };
    private int[] noteAccents = {
        Color.parseColor("#7B5FFF"),
        Color.parseColor("#FF5F9E"),
        Color.parseColor("#5FFFC8"),
        Color.parseColor("#5FA8FF"),
        Color.parseColor("#FFC85F"),
        Color.parseColor("#E05FFF"),
    };

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());
        showLauncherBubble();
    }

    // ─── Launcher Bubble ───────────────────────────────────────────
    private void showLauncherBubble() {
        bubbleView = new View(this);

        // Draw the bubble as a gradient circle
        GradientDrawable bubble = new GradientDrawable();
        bubble.setShape(GradientDrawable.OVAL);
        bubble.setColors(new int[]{Color.parseColor("#7B5FFF"), Color.parseColor("#FF5F9E")});
        bubble.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        bubble.setOrientation(GradientDrawable.Orientation.TL_BR);
        bubbleView.setBackground(bubble);

        // Add "+" text
        TextView bubbleText = new TextView(this);
        bubbleText.setText("+");
        bubbleText.setTextColor(Color.WHITE);
        bubbleText.setTextSize(28f);
        bubbleText.setTypeface(null, Typeface.BOLD);
        bubbleText.setGravity(Gravity.CENTER);
        bubbleText.setBackground(bubble);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            dpToPx(60), dpToPx(60),
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dpToPx(8);
        params.y = dpToPx(120);

        bubbleText.setElevation(dpToPx(8));
        makeDraggableAndClickable(bubbleText, params, () -> spawnNote());

        windowManager.addView(bubbleText, params);
        bubbleView = bubbleText;
    }

    // ─── Spawn a Floating Note ──────────────────────────────────────
    private void spawnNote() {
        noteCounter++;
        int colorIdx = (noteCounter - 1) % noteColors.length;
        FloatingNote note = new FloatingNote(noteCounter, noteColors[colorIdx], noteAccents[colorIdx]);
        activeNotes.add(note);
        note.show();
    }

    // ─── FloatingNote inner class ───────────────────────────────────
    private class FloatingNote {
        int id;
        int bgColor, accentColor;
        View rootView;
        TextView titleView, bodyView;
        WindowManager.LayoutParams params;
        String titleText = "";
        String bodyText = "";

        FloatingNote(int id, int bgColor, int accentColor) {
            this.id = id;
            this.bgColor = bgColor;
            this.accentColor = accentColor;
        }

        void show() {
            // Root container
            LinearLayout root = new LinearLayout(FloatingNoteService.this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setClipToOutline(true);

            // Rounded background
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dpToPx(14));
            bg.setColor(bgColor);
            bg.setStroke(dpToPx(1), Color.argb(60, 
                Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
            root.setBackground(bg);
            root.setElevation(dpToPx(12));

            // ── Header ──
            LinearLayout header = new LinearLayout(FloatingNoteService.this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);
            header.setPadding(dpToPx(10), dpToPx(8), dpToPx(8), dpToPx(6));

            // Colored accent strip
            View strip = new View(FloatingNoteService.this);
            strip.setBackground(makeCircle(accentColor));
            LinearLayout.LayoutParams stripLp = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
            stripLp.setMarginEnd(dpToPx(8));
            strip.setLayoutParams(stripLp);
            header.addView(strip);

            // Title
            titleView = new TextView(FloatingNoteService.this);
            titleView.setText("Note " + id);
            titleView.setTextColor(Color.argb(180, 
                Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
            titleView.setTextSize(10f);
            titleView.setAllCaps(true);
            titleView.setLetterSpacing(0.08f);
            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            titleView.setLayoutParams(titleLp);
            header.addView(titleView);

            // Settings button (⚙)
            TextView settingsBtn = new TextView(FloatingNoteService.this);
            settingsBtn.setText("⚙");
            settingsBtn.setTextColor(Color.argb(150, 255, 255, 255));
            settingsBtn.setTextSize(14f);
            settingsBtn.setPadding(dpToPx(6), dpToPx(4), dpToPx(4), dpToPx(4));
            settingsBtn.setOnClickListener(v -> showSettingsPanel(this));
            header.addView(settingsBtn);

            // Close button (×)
            TextView closeBtn = new TextView(FloatingNoteService.this);
            closeBtn.setText("×");
            closeBtn.setTextColor(Color.argb(180, 255, 100, 100));
            closeBtn.setTextSize(20f);
            closeBtn.setPadding(dpToPx(4), 0, dpToPx(6), dpToPx(2));
            closeBtn.setOnClickListener(v -> remove());
            header.addView(closeBtn);

            root.addView(header);

            // ── Divider ──
            View divider = new View(FloatingNoteService.this);
            divider.setBackgroundColor(Color.argb(40, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
            root.addView(divider, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1)));

            // ── Body (EditText) ──
            EditText body = new EditText(FloatingNoteService.this);
            bodyView = body;
            body.setHint("Tap to type...");
            body.setHintTextColor(Color.argb(80, 255, 255, 255));
            body.setTextColor(Color.WHITE);
            body.setTextSize(globalTextSize);
            body.setLineSpacing(dpToPx(2), 1.3f);
            body.setBackground(null);
            body.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
            body.setGravity(Gravity.TOP | Gravity.START);
            body.setMinHeight(dpToPx(80));
            body.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            body.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            body.setMaxLines(20);
            root.addView(body, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // ── Resize & drag handle ──
            TextView resizeHandle = new TextView(FloatingNoteService.this);
            resizeHandle.setText("⠿");
            resizeHandle.setTextColor(Color.argb(60, 255, 255, 255));
            resizeHandle.setTextSize(14f);
            resizeHandle.setGravity(Gravity.END | Gravity.BOTTOM);
            resizeHandle.setPadding(0, 0, dpToPx(6), dpToPx(4));
            root.addView(resizeHandle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // ── Window params ──
            params = new WindowManager.LayoutParams(
                dpToPx(230), WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = dpToPx(30 + (id % 4) * 20);
            params.y = dpToPx(80 + (id % 5) * 40);
            params.alpha = globalAlpha;

            rootView = root;
            makeDraggableByHeader(header, root, params);

            windowManager.addView(root, params);
        }

        void updateAlpha(float alpha) {
            params.alpha = alpha;
            windowManager.updateViewLayout(rootView, params);
        }

        void updateTextSize(float size) {
            if (bodyView != null) ((EditText)bodyView).setTextSize(size);
        }

        void remove() {
            try { windowManager.removeView(rootView); } catch (Exception ignored) {}
            activeNotes.remove(this);
        }
    }

    // ─── Per-note Settings Panel ────────────────────────────────────
    private void showSettingsPanel(FloatingNote note) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(16));
        panel.setClipToOutline(true);

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setCornerRadius(dpToPx(16));
        panelBg.setColor(Color.parseColor("#0F0F1A"));
        panelBg.setStroke(dpToPx(1), Color.argb(80, 123, 95, 255));
        panel.setBackground(panelBg);
        panel.setElevation(dpToPx(20));

        // Header
        TextView title = new TextView(this);
        title.setText("⚙  Note Settings");
        title.setTextColor(Color.parseColor("#7B5FFF"));
        title.setTextSize(13f);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dpToPx(12));
        panel.addView(title);

        // ── Transparency ──
        panel.addView(makeLabel("Transparency"));
        SeekBar alphaBar = makeSeekBar((int)(globalAlpha * 100), 20, 100);
        TextView alphaVal = makeValueLabel((int)(globalAlpha * 100) + "%");
        alphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                float a = progress / 100f;
                globalAlpha = a;
                alphaVal.setText(progress + "%");
                // Apply to all notes
                for (FloatingNote n : activeNotes) n.updateAlpha(a);
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        LinearLayout alphaRow = makeSliderRow(alphaBar, alphaVal);
        panel.addView(alphaRow);

        // ── Text Size ──
        panel.addView(makeLabel("Text Size"));
        SeekBar sizeBar = makeSeekBar((int)globalTextSize, 10, 30);
        TextView sizeVal = makeValueLabel((int)globalTextSize + "sp");
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                globalTextSize = progress;
                sizeVal.setText(progress + "sp");
                for (FloatingNote n : activeNotes) n.updateTextSize(progress);
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        panel.addView(makeSliderRow(sizeBar, sizeVal));

        // ── Note Width ──
        panel.addView(makeLabel("Note Width"));
        int currentWidthDp = (int)(note.params.width / getResources().getDisplayMetrics().density);
        SeekBar widthBar = makeSeekBar(Math.min(currentWidthDp, 360), 150, 360);
        TextView widthVal = makeValueLabel(currentWidthDp + "dp");
        widthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                note.params.width = dpToPx(progress);
                widthVal.setText(progress + "dp");
                try { windowManager.updateViewLayout(note.rootView, note.params); } catch (Exception ignored) {}
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        panel.addView(makeSliderRow(widthBar, widthVal));

        // ── Buttons row ──
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        btnRow.setPadding(0, dpToPx(12), 0, 0);
        btnRow.setWeightSum(2f);

        // Close panel
        final WindowManager.LayoutParams[] panelParams = new WindowManager.LayoutParams[1];
        final View[] panelViewRef = new View[1];

        Button closePanel = makeButton("Done", Color.parseColor("#7B5FFF"));
        closePanel.setOnClickListener(v -> {
            try { if (panelViewRef[0] != null) windowManager.removeView(panelViewRef[0]); }
            catch (Exception ignored) {}
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btnLp.setMarginEnd(dpToPx(6));
        closePanel.setLayoutParams(btnLp);
        btnRow.addView(closePanel);

        // Delete this note
        Button deleteNote = makeButton("Delete Note", Color.parseColor("#FF4444"));
        deleteNote.setOnClickListener(v -> {
            try { if (panelViewRef[0] != null) windowManager.removeView(panelViewRef[0]); }
            catch (Exception ignored) {}
            note.remove();
        });
        LinearLayout.LayoutParams btnLp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        deleteNote.setLayoutParams(btnLp2);
        btnRow.addView(deleteNote);

        panel.addView(btnRow);

        // Show panel as floating window
        WindowManager.LayoutParams pp = new WindowManager.LayoutParams(
            dpToPx(280), WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        );
        pp.gravity = Gravity.CENTER;
        panelParams[0] = pp;
        panelViewRef[0] = panel;
        windowManager.addView(panel, pp);
    }

    // ─── Dragging helpers ───────────────────────────────────────────
    private void makeDraggableAndClickable(View view, WindowManager.LayoutParams params, Runnable onClick) {
        final int[] startXY = new int[2];
        final int[] startPos = new int[2];
        final boolean[] moved = {false};

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startXY[0] = (int) event.getRawX();
                    startXY[1] = (int) event.getRawY();
                    startPos[0] = params.x;
                    startPos[1] = params.y;
                    moved[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int)event.getRawX() - startXY[0];
                    int dy = (int)event.getRawY() - startXY[1];
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) moved[0] = true;
                    if (moved[0]) {
                        params.x = startPos[0] + dx;
                        params.y = startPos[1] + dy;
                        windowManager.updateViewLayout(view, params);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!moved[0]) onClick.run();
                    return true;
            }
            return false;
        });
    }

    private void makeDraggableByHeader(View header, View root, WindowManager.LayoutParams params) {
        final int[] startXY = new int[2];
        final int[] startPos = new int[2];

        header.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startXY[0] = (int)event.getRawX();
                    startXY[1] = (int)event.getRawY();
                    startPos[0] = params.x;
                    startPos[1] = params.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = startPos[0] + (int)event.getRawX() - startXY[0];
                    params.y = startPos[1] + (int)event.getRawY() - startXY[1];
                    windowManager.updateViewLayout(root, params);
                    return true;
            }
            return false;
        });
    }

    // ─── UI helpers ─────────────────────────────────────────────────
    private TextView makeLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.argb(150, 255, 255, 255));
        tv.setTextSize(10f);
        tv.setAllCaps(true);
        tv.setLetterSpacing(0.08f);
        tv.setPadding(0, dpToPx(8), 0, dpToPx(4));
        return tv;
    }

    private TextView makeValueLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#7B5FFF"));
        tv.setTextSize(10f);
        tv.setTypeface(null, Typeface.BOLD_ITALIC);
        tv.setMinWidth(dpToPx(36));
        tv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        return tv;
    }

    private SeekBar makeSeekBar(int progress, int min, int max) {
        SeekBar sb = new SeekBar(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) sb.setMin(min);
        sb.setMax(max);
        sb.setProgress(progress);
        sb.getProgressDrawable().setColorFilter(Color.parseColor("#7B5FFF"), PorterDuff.Mode.SRC_IN);
        sb.getThumb().setColorFilter(Color.parseColor("#FF5F9E"), PorterDuff.Mode.SRC_IN);
        return sb;
    }

    private LinearLayout makeSliderRow(SeekBar bar, TextView val) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bar.setLayoutParams(barLp);
        row.addView(bar);
        LinearLayout.LayoutParams valLp = new LinearLayout.LayoutParams(dpToPx(44), ViewGroup.LayoutParams.WRAP_CONTENT);
        val.setLayoutParams(valLp);
        row.addView(val);
        return row;
    }

    private Button makeButton(String text, int color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(11f);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(dpToPx(10));
        btnBg.setColor(color);
        btn.setBackground(btnBg);
        btn.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        return btn;
    }

    private GradientDrawable makeCircle(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    // ─── Notification ───────────────────────────────────────────────
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Float Notes", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Floating notes overlay service");
            channel.setShowBadge(false);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent stopIntent = new Intent(this, FloatingNoteService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPi = PendingIntent.getService(this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE);

        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Float Notes Active")
            .setContentText("Tap '+' bubble to create notes")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openPi)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { if (bubbleView != null) windowManager.removeView(bubbleView); } catch (Exception ignored) {}
        for (FloatingNote note : new ArrayList<>(activeNotes)) {
            try { windowManager.removeView(note.rootView); } catch (Exception ignored) {}
        }
        activeNotes.clear();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}
