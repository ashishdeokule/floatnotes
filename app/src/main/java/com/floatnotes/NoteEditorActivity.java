package com.floatnotes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Reserved for future full-screen edit mode
public class NoteEditorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish(); // Editing happens inline in the floating window
    }
}
