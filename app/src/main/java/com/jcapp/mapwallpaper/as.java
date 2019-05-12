//package com.omronhealthcare.foresight.view.history.vitals.adapter;
//
//import android.content.Context;
//import android.content.pm.ActivityInfo;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.daimajia.swipe.SwipeLayout;
//import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
//import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
//import com.omronhealthcare.foresight.R;
//import com.omronhealthcare.foresight.app.ForesightPreferenceManager;
//import com.omronhealthcare.foresight.commons.AppMessageDialogFragment;
//import com.omronhealthcare.foresight.commons.AppOkCancelDialogFragment;
//import com.omronhealthcare.foresight.commons.BpColorCodes;
//import com.omronhealthcare.foresight.dataSource.database.realm.BPData;
//import com.omronhealthcare.foresight.dataSource.database.realm.TagItem;
//import com.omronhealthcare.foresight.utils.Constants;
//import com.omronhealthcare.foresight.utils.ForeSightUtils;
//import com.omronhealthcare.foresight.utils.SegmentsAnalytics;
//import com.omronhealthcare.foresight.view.AppMessagePopUpDialogFragment;
//import com.omronhealthcare.foresight.view.HomeActivity;
//import com.omronhealthcare.foresight.view.dashboard.AddNotesFragment;
//import com.omronhealthcare.foresight.view.history.vitals.data_source.data_models.Tag;
//import com.omronhealthcare.foresight.view.history.vitals.data_source.data_models.VitalsViewContentModel;
//import com.omronhealthcare.foresight.view.history.vitals.graph.MyVitalsChartManger;
//import com.omronhealthcare.foresight.view.history.vitals.view.BloodPressureHistoryFragment;
//import com.omronhealthcare.foresight.view.history.vitals.viewholder.BpDailyItemHeaderViewHolder;
//import com.omronhealthcare.foresight.view.history.vitals.viewholder.BpDailyItemViewHolder;
//import com.omronhealthcare.foresight.view.history.vitals.viewholder.GraphViewHolder;
//import com.omronhealthcare.foresight.view.history.vitals.viewholder.TagsViewHolder;
//import com.omronhealthcare.foresight.view.history.vitals.viewholder.UnderstandReadingViewHolder;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class VitalsContentAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {
//    public static final int TYPE_GRAPH_PRESSURE = 7;
//    public static final int TYPE_GRAPH_PULSE = 8;
//    public static final int TYPE_GRAPH = 0;
//    public static final int TYPE_HEADER_DATE = 1;
//    public static final int TYPE_CONTENT = 2;
//    public static final int TYPE_FOOTER = 3;
//    public static final int TYPE_BP_VALUE = 4;
//    private boolean isPressureChecked;
//    private List<BPData> bpList;
//    private BloodPressureHistoryFragment bloodPressureHistoryFragment;
//    private boolean isExpand;
//    private boolean isTableEmpty;
//    private boolean checkIfToday;
//    private SwipeItemRecyclerMangerImpl swipeItemRecyclerManger;
//    private ArrayList<Tag> tagList = ForesightPreferenceManager.getInstance().getSelectedTags();
//
//    public VitalsContentAdapter(ArrayList<VitalsViewContentModel> vitalsViewContentModels,
//                                List<BPData> bpList, Context context,
//                                boolean isPressureChecked, BloodPressureHistoryFragment fragment,
//                                boolean isExpand, boolean isTableEmpty, boolean checkIfToday) {
//        this.vitalsViewContentModels = vitalsViewContentModels;
//        this.context = context;
//        this.isPressureChecked = isPressureChecked;
//        this.bpList = bpList;
//        this.bloodPressureHistoryFragment = fragment;
//        this.isExpand = isExpand;
//        this.isTableEmpty = isTableEmpty;
//        this.checkIfToday = checkIfToday;
//        swipeItemRecyclerManger = new SwipeItemRecyclerMangerImpl(this);
//    }
//
//    private ArrayList<VitalsViewContentModel> vitalsViewContentModels;
//    private Context context;
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        switch (viewType) {
//            case TYPE_CONTENT: {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(
//                                R.layout.blood_pressure_history_recyclerview_item,
//                                parent, false);
//                SwipeLayout item = view.findViewById(R.id.swipe_item);
//                item.setShowMode(SwipeLayout.ShowMode.PullOut);
//                item.addDrag(SwipeLayout.DragEdge.Right, item.findViewById(R.id.swipe_section));
//                return new BpDailyItemViewHolder(view);
//            }
//            case TYPE_HEADER_DATE: {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(
//                                R.layout.blood_pressure_history_recyclerview_item_header,
//                                parent, false);
//                return new BpDailyItemHeaderViewHolder(view);
//            }
//
//            case TYPE_GRAPH: {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(
//                                R.layout.blood_pressure_history_recyclerview_graph_item,
//                                parent, false);
//                RelativeLayout layout = view.findViewById(R.id.rl_graph_container);
//                RelativeLayout emptyContainer = view.findViewById(R.id.rl_empty_graph_view);
//                ImageView expand = view.findViewById(R.id.iv_expand);
//                if (bpList.size() > 0) {
//                    if (layout != null) {
//                        layout.setVisibility(View.VISIBLE);
//                    }
//                    if (emptyContainer != null) {
//                        emptyContainer.setVisibility(View.GONE);
//                    }
//                } else {
//                    if (layout != null) {
//                        layout.setVisibility(View.GONE);
//                    }
//                    TextView emptyTextView = view.findViewById(R.id.tv_empty_view);
//                    TextView emptyTextHeader = view.findViewById(R.id.tv_empty_view_header);
//                    if (emptyContainer != null) {
//                        if (isTableEmpty) {
//                            emptyTextHeader.setVisibility(View.VISIBLE);
//                            emptyTextView.setText(context.getResources()
//                                    .getString(R.string.bp_empty_text_type_1));
//                            emptyContainer.setBackgroundResource(R.drawable.emptyview_background);
//                            setEmptyTextColor(emptyTextView, false);
//                            expand.setVisibility(View.GONE);
//                        } else if (!isTableEmpty && checkIfToday &&
//                                bloodPressureHistoryFragment.getBloodPressureHistoryViewModel()
//                                        .getCurrentSelectedTab() ==
//                                        R.id.rb_daily) {
//                            emptyTextHeader.setVisibility(View.GONE);
//                            emptyTextView.setText(context.getResources()
//                                    .getString(R.string.bp_empty_text_type_2));
//                            emptyContainer.setBackgroundResource(0);
//                            setEmptyTextColor(emptyTextView, true);
//                            if (bloodPressureHistoryFragment.getBloodPressureHistoryViewModel()
//                                    .getAvailableDataCount() == 0) {
//                                expand.setVisibility(View.GONE);
//                            }
//                        } else {
//                            emptyTextHeader.setVisibility(View.GONE);
//                            emptyTextView.setText(context.getResources()
//                                    .getString(R.string.bp_empty_text_type_3));
//                            emptyContainer.setBackgroundResource(0);
//                            setEmptyTextColor(emptyTextView, true);
//                            if (bloodPressureHistoryFragment.getBloodPressureHistoryViewModel()
//                                    .getAvailableDataCount() == 0) {
//                                expand.setVisibility(View.GONE);
//                            }
//
//                        }
//                    }
//
//                }
//                if (isExpand) {
//                    view.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
//                }
//                return new GraphViewHolder(view);
//            }
//
//            case TYPE_BP_VALUE: {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(
//                                R.layout.blood_pressure_history_recyclerview_item_know_reading,
//                                parent, false);
//                return new UnderstandReadingViewHolder(view);
//            }
//            case TYPE_FOOTER: {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(
//                                R.layout.blood_pressure_history_recyclerview_item_tags,
//                                parent, false);
//                return new TagsViewHolder(view);
//            }
//        }
//        return new GraphViewHolder(parent);
//    }
//
//    private void setEmptyTextColor(TextView tvEmptyView, boolean isDarkBg) {
//        if (tvEmptyView != null) {
//            tvEmptyView.setTextColor(
//                    isDarkBg ? context.getResources().getColor(R.color.landscape_dark_text_color) :
//                            context.getResources().getColor(R.color.color_FF0A1F44));
//        }
//
//    }
//
//
//    private void setBpValuesTextColor(TextView tvSystolic, TextView tvDiastolic, int systolic,
//                                      int diastolic) {
//        int textColor =
//                ForesightPreferenceManager.getInstance().isNightModeEnabled() ? R.color.white :
//                        R.color.color_FF0A1F44;
//        if (systolic > 0 && diastolic > 0) {
//            textColor = BpColorCodes
//                    .getColorForPressureValues(systolic, diastolic);
//        }
//        if (tvSystolic != null && tvDiastolic != null) {
//            tvSystolic.setTextColor(ContextCompat.getColor(context, textColor));
//            tvDiastolic.setTextColor(ContextCompat.getColor(context, textColor));
//        }
//
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof BpDailyItemViewHolder) {
//            BpDailyItemViewHolder dailyItemViewHolder = (BpDailyItemViewHolder) holder;
//            if (vitalsViewContentModels.get(position).getSystolicValue() > 0) {
//                dailyItemViewHolder.tvSystolic
//                        .setText(String.valueOf(
//                                vitalsViewContentModels.get(position).getSystolicValue()));
//            } else {
//                dailyItemViewHolder.tvTime.setVisibility(View.GONE);
//                dailyItemViewHolder.tvSystolic
//                        .setText(Constants.EMPTY);
//            }
//            setNotesVisibility(vitalsViewContentModels.get(position), dailyItemViewHolder.ivNote,
//                    position);
//
//            //********  change text color according to input text range ********//
//            setBpValuesTextColor(dailyItemViewHolder.tvSystolic, dailyItemViewHolder.tvDiastolic,
//                    (int) vitalsViewContentModels.get(position).getSystolicValue(),
//                    (int) vitalsViewContentModels.get(position).getDiastolicValue());
//            if (vitalsViewContentModels.get(position).getDiastolicValue() > 0) {
//                dailyItemViewHolder.tvDiastolic
//                        .setText(String.valueOf(
//                                vitalsViewContentModels.get(position).getDiastolicValue()));
//            } else {
//                dailyItemViewHolder.tvDiastolic
//                        .setText(Constants.EMPTY);
//            }
//            if (vitalsViewContentModels.get(position).getPulseValue() > 0) {
//                dailyItemViewHolder.tvPulse
//                        .setText(String.valueOf(
//                                vitalsViewContentModels.get(position).getPulseValue()));
//            } else {
//                dailyItemViewHolder.tvPulse
//                        .setText(Constants.EMPTY);
//            }
//            dailyItemViewHolder.rlDelete.setOnClickListener(
//                    view -> loadDelete(vitalsViewContentModels.get(position).getStarDate()));
//            dailyItemViewHolder.rlEdit.setOnClickListener(
//                    view -> editNotes(vitalsViewContentModels.get(position).getStarDate(),
//                            vitalsViewContentModels.get(position).getNotes()));
//            if (vitalsViewContentModels.get(position).isDataMovementErrorFlag()) {
//                dailyItemViewHolder.ivBodyMovement.setVisibility(View.VISIBLE);
//                dailyItemViewHolder.ivBodyMovement.setOnClickListener(
//                        view -> showMovementErrorDialog());
//            } else {
//                dailyItemViewHolder.ivBodyMovement.setVisibility(View.GONE);
//            }
//            if (vitalsViewContentModels.get(position).isIhbFlag()) {
//                dailyItemViewHolder.ivIhb.setVisibility(View.VISIBLE);
//                dailyItemViewHolder.ivIhb.setOnClickListener(
//                        view -> showIHBDialog());
//            } else {
//                dailyItemViewHolder.ivIhb.setVisibility(View.GONE);
//            }
//
//            if (vitalsViewContentModels.get(position).isManual()) {
//                dailyItemViewHolder.tvManual
//                        .setVisibility(View.VISIBLE);
//            } else {
//                dailyItemViewHolder.tvManual
//                        .setVisibility(View.GONE);
//
//            }
//            // Disable Swipe for empty data model
//            if (vitalsViewContentModels.get(position).getStarDate() == 0) {
//                dailyItemViewHolder.sLSwipe.setSwipeEnabled(false);
//            } else {
//                dailyItemViewHolder.sLSwipe.setSwipeEnabled(true);
//            }
//
//            dailyItemViewHolder.tvTime
//                    .setText(vitalsViewContentModels.get(position).getTime());
//            swipeItemRecyclerManger.bindView(holder.itemView, position);
//            dailyItemViewHolder.sLSwipe.addSwipeListener(new SwipeLayout.SwipeListener() {
//                @Override
//                public void onStartOpen(SwipeLayout layout) {
//                    swipeItemRecyclerManger.closeAllExcept(layout);
//
//                }
//
//                @Override
//                public void onOpen(SwipeLayout layout) {
//
//                }
//
//                @Override
//                public void onStartClose(SwipeLayout layout) {
//
//                }
//
//                @Override
//                public void onClose(SwipeLayout layout) {
//
//                }
//
//                @Override
//                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
//
//                }
//
//                @Override
//                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
//
//                }
//            });
//
//        } else if (holder instanceof BpDailyItemHeaderViewHolder) {
//            BpDailyItemHeaderViewHolder dailyItemHeaderViewHolder =
//                    (BpDailyItemHeaderViewHolder) holder;
//            dailyItemHeaderViewHolder.tvHeader
//                    .setText(vitalsViewContentModels.get(position).getDate());
//
//        } else if (holder instanceof UnderstandReadingViewHolder) {
//            UnderstandReadingViewHolder understandReadingViewHolder =
//                    (UnderstandReadingViewHolder) holder;
//            understandReadingViewHolder.rlBpValue.setOnClickListener(
//                    view -> ((HomeActivity) context).startUnderstandReadingActivity());
//        } else if (holder instanceof GraphViewHolder) {
//            GraphViewHolder graphViewHolder = (GraphViewHolder) holder;
//            if (isExpand) {
//                if (((GraphViewHolder) holder).expand != null) {
//                    ((GraphViewHolder) holder).expand.setVisibility(View.GONE);
//                }
//                if (((GraphViewHolder) holder).collapse != null) {
//                    ((GraphViewHolder) holder).collapse.setVisibility(View.VISIBLE);
//                }
//            } else {
//                if (((GraphViewHolder) holder).expand != null) {
//                    ((GraphViewHolder) holder).expand.setVisibility(View.VISIBLE);
//                }
//                if (((GraphViewHolder) holder).collapse != null) {
//                    ((GraphViewHolder) holder).collapse.setVisibility(View.GONE);
//                }
//            }
//            if (isTableEmpty || bloodPressureHistoryFragment.getBloodPressureHistoryViewModel()
//                    .getAvailableDataCount() == 0) {
//                if (((GraphViewHolder) holder).expand != null) {
//                    ((GraphViewHolder) holder).expand.setVisibility(View.GONE);
//                }
//                if (((GraphViewHolder) holder).collapse != null) {
//                    ((GraphViewHolder) holder).collapse.setVisibility(View.GONE);
//                }
//            }
//            if (((GraphViewHolder) holder).expand != null) {
//                ((GraphViewHolder) holder).expand.setOnClickListener(view -> {
//                    if (((HomeActivity) context).isTablet()) {
//                        bloodPressureHistoryFragment
//                                .handleTabFullScreenForRecyclerView(true);
//
//                    } else {
//                        ((HomeActivity) context).setRequestedOrientation(
//                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                    }
//                });
//            }
//            if (((GraphViewHolder) holder).collapse != null) {
//                ((GraphViewHolder) holder).collapse.setOnClickListener(view -> {
//                    if (((HomeActivity) context).isTablet()) {
//
//                        bloodPressureHistoryFragment
//                                .handleTabFullScreenForRecyclerView(false);
//                    }
//                });
//            }
//            if (bpList != null && bpList.size() > 0) {
//
//                MyVitalsChartManger manager =
//                        new MyVitalsChartManger(context, graphViewHolder.chart, null,
//                                bloodPressureHistoryFragment
//                                        .getBloodPressureHistoryViewModel()
//                                        .getStart(), bloodPressureHistoryFragment
//                                .getBloodPressureHistoryViewModel()
//                                .getEnd(),
//                                false, BloodPressureHistoryFragment.TAG);
//
//                int type;
//                if (isPressureChecked) {
//                    type = MyVitalsChartManger.TYPE_PRESSURE;
//                } else {
//                    type = MyVitalsChartManger.TYPE_PULSE;
//                }
//                if (bloodPressureHistoryFragment.getBloodPressureHistoryViewModel()
//                        .getCurrentSelectedTab() == R.id.rb_daily) {
//                    manager.setDuration(MyVitalsChartManger.DURATION_DAILY);
//                } else {
//                    manager.setDuration(MyVitalsChartManger.DURATION_WEEKLY);
//                }
//                manager.loadGraph(type);
//
//            }
//
//
//        } else if (holder instanceof TagsViewHolder) {
//            TagsViewHolder tagsViewHolder = (TagsViewHolder) holder;
//            if (tagsViewHolder.llTags != null) {
//                tagsViewHolder.llTags.removeAllViews();
//            }
//            if (vitalsViewContentModels.get(position).getTagsList().size() > 0) {
//                for (TagItem item : vitalsViewContentModels.get(position).getTagsList()
//                        .get(0)
//                        .getTagsList()) {
//                    if (item.isStatus() && !checkIfExcluded(item.getLabel())) {
//                        ImageView imageView = new ImageView(context);
//                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.WRAP_CONTENT,
//                                LinearLayout.LayoutParams.WRAP_CONTENT);
//                        params.setMargins(20, 10, 20, 10);
//                        imageView.setLayoutParams(params);
//                        imageView.setImageResource(getIconForEntityId(item.getId()));
//                        tagsViewHolder.llTags.addView(imageView);
//                    }
//                }
//            }
//        }
//    }
//
//    private boolean checkIfExcluded(String label) {
//        for (Tag tag : tagList) {
//            if (tag.getName().equals(label) && tag.getSelectedId() == Tag.SELECTED_NO) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//    private int getIconForEntityId(long id) {
//        return ForeSightUtils.getIcon((int) id);
//    }
//
//    private void loadDelete(long id) {
//        if (id != 0) {
//            SegmentsAnalytics.getInstance().trackEvent(context, true, SegmentsAnalytics.HistoryBPScreen, SegmentsAnalytics.HistoryBPDeleteAction);
//           /* DeleteValueFragment fragment = new DeleteValueFragment();
//            fragment.setDeleteListener(() -> bloodPressureHistoryFragment.deleteItem(id));
//            fragment.show(((HomeActivity) context).getSupportFragmentManager(), "");*/
//            AppOkCancelDialogFragment appOkCancelDialogFragment =
//                    AppOkCancelDialogFragment
//                            .newInstance(context.getString(R.string.delete_note_confirm_message),
//                                    context.getString(R.string.yes),
//                                    context.getString(R.string.no), "");
//            appOkCancelDialogFragment
//                    .show(((AppCompatActivity) context).getSupportFragmentManager(), "");
//            appOkCancelDialogFragment.setOkCancelClickListener(
//                    new AppOkCancelDialogFragment.okCancelClickListener() {
//                        @Override
//                        public void onOkClick(String calledFrom) {
//                            bloodPressureHistoryFragment.deleteItem(id);
//                        }
//
//                        @Override
//                        public void onCancelClick(String calledFrom) {
//
//                        }
//
//                        @Override
//                        public void onCloseClick() {
//
//                        }
//                    });
//        } else {
//            showPopUp();
//        }
//
//    }
//
//    private void showPopUp() {
//        AppMessagePopUpDialogFragment fragment =
//                AppMessagePopUpDialogFragment
//                        .newInstance(context.getResources().getString(
//                                R.string.unable_to_do_this_operation_now));
//        fragment.show(((HomeActivity) context).getSupportFragmentManager(), "");
//    }
//
//    private void editNotes(long id, String notes) {
//        if (id != 0) {
//            SegmentsAnalytics.getInstance().trackEvent(context, true, SegmentsAnalytics.HistoryBPScreen, SegmentsAnalytics.HistoryBPEditAction);
//            AddNotesFragment fragment = new AddNotesFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString(Constants.KEY_NOTES, notes);
//            fragment.setArguments(bundle);
//            fragment.setAddListener(value -> bloodPressureHistoryFragment.editNote(id, value));
//            fragment.show(((HomeActivity) context).getSupportFragmentManager(), "");
//        } else {
//            showPopUp();
//        }
//    }
//
//    private void showMovementErrorDialog() {
//        SegmentsAnalytics.getInstance().trackEvent(context, true, SegmentsAnalytics.HistoryBPScreen, SegmentsAnalytics.HistoryBPMovementErrorAction);
//        AppMessageDialogFragment appMessageDialogFragment =
//                AppMessageDialogFragment
//                        .newInstance(context.getResources().getString(R.string.movement_error),
//                                context.getResources().getString(R.string.movement_error_mess),
//                                R.drawable.movement);
//        appMessageDialogFragment.show(((HomeActivity) context).getSupportFragmentManager(), "");
//    }
//
//    private void showIHBDialog() {
//        SegmentsAnalytics.getInstance().trackEvent(context, true, SegmentsAnalytics.HistoryBPScreen, SegmentsAnalytics.HistoryBPIrregularHeartbeatAction);
//        AppMessageDialogFragment appMessageDialogFragment =
//                AppMessageDialogFragment
//                        .newInstance(context.getResources().getString(R.string.irregular_heartbeat),
//                                context.getResources().getString(R.string.irregular_heartbeat_mess),
//                                R.drawable.irregular_heartbeat_big);
//        appMessageDialogFragment.show(((HomeActivity) context).getSupportFragmentManager(), "");
//    }
//
//    @Override
//    public int getItemCount() {
//        if (isExpand) {
//            return 1;
//        }
//        return vitalsViewContentModels.size();
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return vitalsViewContentModels.get(position).getType();
//
//    }
//
//    @Override
//    public int getSwipeLayoutResourceId(int position) {
//        return R.id.swipe_item;
//    }
//
//    private void setNotesVisibility(VitalsViewContentModel data, ImageView ivNote, int position) {
//        if (data != null) {
//            if (data.getNotes() != null && !data.getNotes().isEmpty()) {
//                ivNote.setVisibility(View.VISIBLE);
//                ivNote.setOnClickListener(
//                        view -> editNotes(vitalsViewContentModels.get(position).getStarDate(),
//                                vitalsViewContentModels.get(position).getNotes()));
//            } else {
//                ivNote.setVisibility(View.GONE);
//            }
//        } else {
//            ivNote.setVisibility(View.GONE);
//        }
//    }
//}
//
