/*
 * Copyright (c) 2011, Polidea
 * Copyright © 2013–2016 Michael von Glasow.
 * 
 * This file is part of LSRN Tools.
 *
 * LSRN Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSRN Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSRN Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vonglasow.michael.satstat.utils;

import com.vonglasow.michael.satstat.R;

import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * simple item description.
 * 
 */
public class DownloadTreeViewAdapter extends AbstractTreeViewAdapter<RemoteFile> {

    public DownloadTreeViewAdapter(final Activity activity,
            final TreeStateManager<RemoteFile> treeStateManager,
            final int numberOfLevels) {
        super(activity, treeStateManager, numberOfLevels);
    }

    @Override
    public View getNewChildView(final TreeNodeInfo<RemoteFile> treeNodeInfo) {
    	final LinearLayout viewLayout;
    	viewLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.download_list_item, null);
    	return updateView(viewLayout, treeNodeInfo);
    }

    @Override
    public LinearLayout updateView(final View view,
            final TreeNodeInfo<RemoteFile> treeNodeInfo) {
        final LinearLayout viewLayout = (LinearLayout) view;
        RemoteFile rfile = treeNodeInfo.getId();
        final String rfileName = rfile.name;
        
        /*
        final TextView descriptionView = (TextView) viewLayout
                .findViewById(R.id.demo_list_item_description);
        final TextView levelView = (TextView) viewLayout
                .findViewById(R.id.demo_list_item_level);
        descriptionView.setText(getDescription(treeNodeInfo.getId()));
        levelView.setText(Integer.toString(treeNodeInfo.getLevel()));
        final CheckBox box = (CheckBox) viewLayout
                .findViewById(R.id.demo_list_checkbox);
        box.setTag(treeNodeInfo.getId());
        if (treeNodeInfo.isWithChildren()) {
            box.setVisibility(View.GONE);
        } else {
            box.setVisibility(View.VISIBLE);
            box.setChecked(selected.contains(treeNodeInfo.getId()));
        }
        box.setOnCheckedChangeListener(onCheckedChange);
        */

        TextView downloadListItem = (TextView) viewLayout.findViewById(R.id.downloadListItem);
        downloadListItem.setText(rfileName);

        return viewLayout;
    }

    @Override
    public void handleItemClick(final View view, final Object id) {
        final RemoteFile rfile = (RemoteFile) id;
        final TreeNodeInfo<RemoteFile> info = getManager().getNodeInfo(rfile);
        if (rfile.isDirectory) {
        	if (rfile.children != null)
        		super.handleItemClick(view, id);
        	else {
        		// TODO load subdir
        	}
        } else {
        	// TODO download file
        }
    }

    @Override
    public long getItemId(final int position) {
        return getTreeId(position).hashCode();
    }
}