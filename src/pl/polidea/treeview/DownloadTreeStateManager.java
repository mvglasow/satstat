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

package pl.polidea.treeview;

import java.util.List;

import com.vonglasow.michael.satstat.utils.RemoteFile;

/**
 * In-memory manager of tree state, adapted for use with {@link com.conglasow.michael.satstat.utils.RemoteFile}
 * and branches loaded dynamically.
 */
public class DownloadTreeStateManager extends
		InMemoryTreeStateManager<RemoteFile> {
    private static final long serialVersionUID = 1L;

    @Override
    public synchronized TreeNodeInfo<RemoteFile> getNodeInfo(final RemoteFile id) {
        final InMemoryTreeNode<RemoteFile> node = getNodeFromTreeOrThrow(id);
        final List<InMemoryTreeNode<RemoteFile>> children = node.getChildren();
        boolean expanded = false;
        if (!children.isEmpty() && children.get(0).isVisible()) {
            expanded = true;
        }
        boolean hasChildren = id.isDirectory | !children.isEmpty();
        return new TreeNodeInfo<RemoteFile>(id, node.getLevel(), hasChildren,
                node.isVisible(), expanded);
    }

}
