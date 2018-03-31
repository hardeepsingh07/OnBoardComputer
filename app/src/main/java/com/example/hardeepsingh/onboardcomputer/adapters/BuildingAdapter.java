package com.example.hardeepsingh.onboardcomputer.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hardeepsingh.onboardcomputer.BR;
import com.example.hardeepsingh.onboardcomputer.R;
import com.example.hardeepsingh.onboardcomputer.pathHandlers.OnItemClickListener;
import com.example.hardeepsingh.onboardcomputer.models.Building;

import java.util.ArrayList;

/**
 * Building data adapter with custom filter
 *
 * @author by Hardeep Singh (hardeepsingh@cpp.edu)
 *         Januar 23, 2018
 */
public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.BindingViewHolder> {

    private ArrayList<Building> originalList;
    private ArrayList<Building> filteredList;
    private OnItemClickListener onItemClickListener;

    public BuildingAdapter(ArrayList<Building> buildingArrayList, OnItemClickListener onItemClickListener) {
        this.originalList = buildingArrayList;
        this.filteredList = new ArrayList<>();
        filteredList.addAll(originalList);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public BuildingAdapter.BindingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        ViewDataBinding viewDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.getContext()), R.layout.building_info_card, viewGroup, false);
        return new BindingViewHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(BuildingAdapter.BindingViewHolder holder, int position) {
        final Building building = filteredList.get(position);
        ViewDataBinding viewDataBinding = holder.getViewDataBinding();
        viewDataBinding.setVariable(BR.building, building);
        viewDataBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onClick(building);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class BindingViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding viewDataBinding;

        public BindingViewHolder(final ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            this.viewDataBinding = viewDataBinding;
        }

        public ViewDataBinding getViewDataBinding() {
            return viewDataBinding;
        }
    }

    public void filter(String text) {
        filteredList.clear();
        if(text.isEmpty()){
            filteredList.addAll(originalList);
        } else{
            text = text.toLowerCase();
            for(Building b: originalList) {
                if(b.getNumber().trim().toLowerCase().contains(text)){
                    filteredList.add(b);
                }
            }
        }
        notifyDataSetChanged();
    }
}
