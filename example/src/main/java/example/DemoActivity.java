package example;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.android15.phlux.example.R;
import phlux.base.PhluxActivity;

public class DemoActivity extends PhluxActivity<DemoState> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        findViewById(R.id.start_demo_1).setOnClickListener(v -> scope().background(1, DemoTask1.create()));
        findViewById(R.id.stop_demo_1).setOnClickListener(v -> scope().drop(1));
    }

    @Override
    public DemoState initial() {
        return DemoState.create();
    }

    @Override
    public void update(DemoState state) {
        part("progress", state.progress(), progress -> {
            setWeight(R.id.padding_before, progress);
            setWeight(R.id.padding_after, 100 - progress);

            TextView text = (TextView) findViewById(R.id.progressText);
            text.setText(String.format("PROGRESS: %.0f%%", progress));
        });
    }

    private void setWeight(int id, float progress) {
        View before = findViewById(id);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) before.getLayoutParams();
        layoutParams.weight = progress;
        before.requestLayout();
    }
}
