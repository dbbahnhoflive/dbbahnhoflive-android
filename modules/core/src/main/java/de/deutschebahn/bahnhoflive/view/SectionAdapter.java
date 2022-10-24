/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;

public class SectionAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class Section<VH extends RecyclerView.ViewHolder> {

        final RecyclerView.Adapter adapter;

        final int viewTypeCount;

        final CharSequence title;

        public Section(RecyclerView.Adapter adapter, int viewTypeCount, CharSequence title) {
            this.adapter = adapter;
            this.viewTypeCount = viewTypeCount;
            this.title = title;
        }

        public Section(RecyclerView.Adapter<VH> adapter) {
            this(adapter, 1, null);
        }
    }

    private static class TitleViewHolder extends ViewHolder<CharSequence> {

        private final TextView textView;

        public TitleViewHolder(ViewGroup parent) {
            super(parent, R.layout.item_section_title);
            textView = findTextView(R.id.text);
        }

        @Override
        protected void onBind(CharSequence item) {
            super.onBind(item);

            textView.setText(item);
        }
    }

    private class CachedSection<VH extends RecyclerView.ViewHolder> {
        final Section<VH> section;

        int itemCount;

        int positionOffset;

        private CachedSection(Section<VH> section, int offset) {
            this.section = section;

            updateCounts(offset);
        }

        public void updateCounts(int offset) {
            positionOffset = offset;
            itemCount = section.adapter.getItemCount() + 1;
        }
    }

    private class SectionPosition {
        Section<VH> section;
        int position;

        public SectionPosition(final int globalPosition) {
            position = globalPosition;

            for (CachedSection<VH> cachedSection : sectionCache) {
                final int itemCount = cachedSection.itemCount;
                if (itemCount > position) {
                    this.section = cachedSection.section;
                    break;
                } else {
                    position -= itemCount;
                }
            }
        }
    }

    private class SectionViewType {
        Section<VH> section;
        int viewType;

        SectionViewType(final int globalViewType) {
            viewType = globalViewType;

            for (CachedSection<VH> cachedSection : sectionCache) {
                if (viewType < cachedSection.section.viewTypeCount) {
                    this.section = cachedSection.section;
                    break;
                } else {
                    viewType -= cachedSection.section.viewTypeCount;
                }
            }
        }
    }

    private final List<CachedSection<VH>> sectionCache;

    public SectionAdapter(Section<VH> ... sections) {
        this(Arrays.asList(sections));
    }

    public SectionAdapter(List<Section<VH>> sections) {
        sectionCache = new ArrayList<>(sections.size());

        int offset = 0;
        for (Section<VH> section : sections) {
            final CachedSection<VH> cachedSection = new CachedSection<>(section, offset);
            sectionCache.add(cachedSection);
            offset += cachedSection.itemCount;

            section.adapter.registerAdapterDataObserver(new DelegateDataObserver(section));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new TitleViewHolder(parent);
        }

        final SectionViewType sectionViewType = new SectionViewType(viewType - 1);

        return sectionViewType.section.adapter.onCreateViewHolder(parent, sectionViewType.viewType);
    }


    @Override
    public int getItemCount() {
        final CachedSection cachedSection = sectionCache.get(sectionCache.size() - 1);

        return cachedSection.positionOffset + cachedSection.itemCount;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        for (CachedSection cachedSection : sectionCache) {
            cachedSection.section.adapter.onAttachedToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SectionPosition sectionPosition = new SectionPosition(position);

        if (sectionPosition.position == 0) {
            ((TitleViewHolder) holder).bind(sectionPosition.section.title);
        } else {
            sectionPosition.section.adapter.onBindViewHolder(holder, sectionPosition.position - 1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        final SectionPosition sectionPosition = new SectionPosition(position);
        if (sectionPosition.position == 0) {
            if (sectionPosition.section.title == "") {
                holder.itemView.setVisibility(View.GONE); // why ? not working
                holder.itemView.getLayoutParams().height = 0;
            } else
                ((TitleViewHolder) holder).bind(sectionPosition.section.title);

        } else {
            sectionPosition.section.adapter.onBindViewHolder(holder, sectionPosition.position - 1, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final SectionPosition sectionPosition = new SectionPosition(position);

        if (sectionPosition.position == 0) {
            return 0;
        }

        int viewType = sectionPosition.section.adapter.getItemViewType(sectionPosition.position - 1);

        for (CachedSection section : sectionCache) {
            if (section.section == sectionPosition.section) {
                break;
            } else {
                viewType += section.section.viewTypeCount;
            }
        }

        return viewType + 1;
    }

    @Override
    public long getItemId(int position) {
        final SectionPosition sectionPosition = new SectionPosition(position);

        if (sectionPosition.position == 0) {
            return -1;
        }

        return sectionPosition.section.adapter.getItemId(sectionPosition.position);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        // implement if needed
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        // implement if needed
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        // implement if needed
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        // implement if needed
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        for (CachedSection cachedSection : sectionCache) {
            cachedSection.section.adapter.onDetachedFromRecyclerView(recyclerView);
        }
    }

    private class DelegateDataObserver extends RecyclerView.AdapterDataObserver {

        private final Section<VH> section;

        public DelegateDataObserver(Section<VH> section) {
            this.section = section;
        }

        @Override
        public void onChanged() {
            int offset = 0;
            for (CachedSection<VH> cachedSection : sectionCache) {
                cachedSection.updateCounts(offset);
                offset += cachedSection.itemCount;
            }

            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(getGlobalPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyItemRangeChanged(getGlobalPosition(positionStart), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(getGlobalPosition(positionStart), itemCount);
        }

        private int getGlobalPosition(int position) {
            for (CachedSection cachedSection : sectionCache) {
                if (cachedSection.section == this.section) {
                    return position;
                } else {
                    position -= cachedSection.itemCount;
                }
            }

            throw new IndexOutOfBoundsException();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(getGlobalPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemMoved(getGlobalPosition(fromPosition), getGlobalPosition(toPosition));
        }
    }
}
