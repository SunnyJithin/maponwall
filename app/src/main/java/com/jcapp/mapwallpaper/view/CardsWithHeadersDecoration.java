// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jcapp.mapwallpaper.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.jcapp.mapwallpaper.models.SkuRowData;

/**
 * A separator for RecyclerView that keeps the specified spaces between headers and the cards.
 */
public class CardsWithHeadersDecoration extends RecyclerView.ItemDecoration {

    private final RowDataProvider mRowDataProvider;
    private final int mRowGap;

    public CardsWithHeadersDecoration(RowDataProvider rowDataProvider, int rowGap) {
        this.mRowDataProvider = rowDataProvider;
        this.mRowGap = rowGap;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {

        final int position = parent.getChildAdapterPosition(view);
        final SkuRowData data = mRowDataProvider.getData(position);

        outRect.bottom = mRowGap;
    }
}
