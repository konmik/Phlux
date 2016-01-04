package example;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import base.PhluxActivity;
import base.ServerAPI;
import info.android15.phluxexample.R;

public class MainActivity extends PhluxActivity<MainState> {

    private static final int REQUEST_ID = 1;

    CheckedTextView check1;
    CheckedTextView check2;
    ArrayAdapter<ServerAPI.Item> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpdateAllOnResume(true);

        check1 = (CheckedTextView) findViewById(R.id.check1);
        check2 = (CheckedTextView) findViewById(R.id.check2);

        check1.setText(MainState.NAME_1);
        check2.setText(MainState.NAME_2);

        check1.setOnClickListener(v -> switchTo(MainState.NAME_1));
        check2.setOnClickListener(v -> switchTo(MainState.NAME_2));

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter = new ArrayAdapter<>(this, R.layout.item));

        if (savedInstanceState == null)
            background(REQUEST_ID, Request.create(MainState.DEFAULT_NAME), true);
    }

    private void switchTo(final String name) {
        apply(state -> state.toBuilder()
            .name(name)
            .build());
        background(REQUEST_ID, Request.create(name), true);
    }

    @Override
    protected MainState initial() {
        return MainState.create();
    }

    @Override
    protected void update(MainState state) {
        part("tabs", state.name(), name -> {
            check1.setChecked(name.equals(MainState.NAME_1));
            check2.setChecked(name.equals(MainState.NAME_2));
        });
        part("items", state.items(), items -> {
            adapter.clear();
            if (items.isPresent())
                adapter.addAll(items.get());
        });
        part("error", state.error(), error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                post(this::removeError);
            }
        });
    }

    private void removeError() {
        apply(s -> s.toBuilder()
            .error(null)
            .build());
    }
}
