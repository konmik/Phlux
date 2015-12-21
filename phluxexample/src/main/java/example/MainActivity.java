package example;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import base.ServerAPI;
import info.android15.phluxexample.R;
import phlux.PhluxFunction;
import base.PhluxActivity;

public class MainActivity extends PhluxActivity<MainState> {

    private static final String NAME_1 = "Chuck Norris";
    private static final String NAME_2 = "Jackie Chan";
    private static final String DEFAULT_NAME = NAME_1;
    private static final int REQUEST_ID = 1;

    CheckedTextView check1;
    CheckedTextView check2;
    ArrayAdapter<ServerAPI.Item> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        check1 = (CheckedTextView) findViewById(R.id.check1);
        check2 = (CheckedTextView) findViewById(R.id.check2);

        check1.setText(NAME_1);
        check2.setText(NAME_2);

        check1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTo(NAME_1);
            }
        });
        check2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTo(NAME_2);
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter = new ArrayAdapter<>(this, R.layout.item));

        if (savedInstanceState == null)
            background(REQUEST_ID, Request.create(DEFAULT_NAME), true);
    }

    private void switchTo(final String name) {
        apply(new PhluxFunction<MainState>() {
            @Override
            public MainState call(MainState state) {
                return state.toBuilder()
                    .name(name)
                    .build();
            }
        });
        background(REQUEST_ID, Request.create(name), true);
    }

    @Override
    protected MainState initial() {
        return MainState.create(DEFAULT_NAME);
    }

    @Override
    protected void update(MainState state) {
        check1.setChecked(state.name().equals(NAME_1));
        check2.setChecked(state.name().equals(NAME_2));

        adapter.clear();
        if (state.items().isPresent())
            adapter.addAll(state.items().get());
        if (state.error().isPresent())
            Toast.makeText(this, state.error().get(), Toast.LENGTH_LONG).show();
    }
}
