package example;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import base.ServerAPI;
import info.android15.phlux.example.R;
import phlux.PhluxScope;
import phlux.base.PhluxActivity;

public class MainActivity extends PhluxActivity<MainState> {

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

        check1.setText(MainState.NAME_1);
        check2.setText(MainState.NAME_2);

        check1.setOnClickListener(v -> switchTo(MainState.NAME_1));
        check2.setOnClickListener(v -> switchTo(MainState.NAME_2));

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter = new ArrayAdapter<>(this, R.layout.item));
    }

    @Override
    public void onScopeCreated(PhluxScope<MainState> scope) {
        super.onScopeCreated(scope);
        scope.background(REQUEST_ID, Request.create(MainState.DEFAULT_NAME));
    }

    private void switchTo(final String name) {
        scope().apply(state -> state.toBuilder()
            .name(name)
            .build());
        scope().background(REQUEST_ID, Request.create(name));
    }

    @Override
    public MainState initial() {
        return MainState.create();
    }

    @Override
    public void update(MainState state) {
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
        scope().apply(s -> s.toBuilder()
            .error(null)
            .build());
    }
}
