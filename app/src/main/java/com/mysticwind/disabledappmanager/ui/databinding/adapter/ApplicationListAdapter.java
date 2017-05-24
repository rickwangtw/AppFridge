package com.mysticwind.disabledappmanager.ui.databinding.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.BR;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;

import java.util.List;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ApplicationListAdapter extends RecyclerView.Adapter<ApplicationListAdapter.ViewHolder> {

    @Value
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding viewDataBinding;

        public ViewHolder(final View view) {
            super(view);
            viewDataBinding = DataBindingUtil.bind(view);
        }
    }

    private final List<ApplicationModel> applicationModels;

    public ApplicationListAdapter(List<ApplicationModel> applicationModels) {
        this.applicationModels = Preconditions.checkNotNull(applicationModels);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.perspective_state_app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        ApplicationModel applicationModel = applicationModels.get(position);
        viewHolder.getViewDataBinding().setVariable(BR.application, applicationModel);
        viewHolder.getViewDataBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return applicationModels.size();
    }
}
