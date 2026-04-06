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
import com.rentmaster.app.ui.adapter.TenantReportAdapter;
import com.rentmaster.app.ui.viewmodel.TenantViewModel;

public class TenantReportFragment extends Fragment {

    private TenantViewModel tenantViewModel;
    private TenantReportAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_report, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rv_tenant_report);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TenantReportAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.view.View emptyView = view.findViewById(R.id.tv_empty);
        RecyclerView recyclerView = view.findViewById(R.id.rv_tenant_report);

        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);
        tenantViewModel.getTenantsWithTotalPayments().observe(getViewLifecycleOwner(), tenantReports -> {
            adapter.setTenantReports(tenantReports);
            if (tenantReports == null || tenantReports.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}
