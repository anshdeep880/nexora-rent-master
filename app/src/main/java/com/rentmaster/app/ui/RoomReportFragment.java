package com.rentmaster.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.ui.adapter.RoomReportAdapter;
import com.rentmaster.app.ui.viewmodel.RoomViewModel;

public class RoomReportFragment extends Fragment {

    private RoomViewModel roomViewModel;
    private RoomReportAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_report, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rv_room_report);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomReportAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.view.View emptyView = view.findViewById(R.id.tv_empty);
        RecyclerView recyclerView = view.findViewById(R.id.rv_room_report);

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        roomViewModel.getRoomsWithOutstandingRent().observe(getViewLifecycleOwner(), roomReports -> {
            adapter.setRoomReports(roomReports);
            if (roomReports == null || roomReports.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}
