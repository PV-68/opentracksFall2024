package de.dennisguse.opentracks.ui.aggregatedStatistics;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.ActivityType;
import de.dennisguse.opentracks.data.models.DistanceFormatter;
import de.dennisguse.opentracks.data.models.SpeedFormatter;
import de.dennisguse.opentracks.databinding.AggregatedStatsListItemBinding;
import de.dennisguse.opentracks.settings.PreferencesUtils;
import de.dennisguse.opentracks.settings.UnitSystem;
import de.dennisguse.opentracks.util.StringUtils;

public class AggregatedStatisticsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AggregatedStatistics aggregatedStatistics;
    private final Context context;

    public AggregatedStatisticsAdapter(Context context, AggregatedStatistics aggregatedStatistics) {
        this.context = context;
        this.aggregatedStatistics = aggregatedStatistics;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AggregatedStatsListItemBinding.inflate(LayoutInflater.from(parent.getContext())));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;

        AggregatedStatistics.AggregatedStatistic aggregatedStatistic = aggregatedStatistics.getItem(position);

        String type = aggregatedStatistic.getActivityTypeLocalized();
        boolean isPace = !ActivityType.findByLocalizedString(context, type).isShowSpeedPreferred();

        viewHolder.setSpeedOrPace(aggregatedStatistic, isPace);
    }


    @Override
    public int getItemCount() {
        if (aggregatedStatistics == null) {
            return 0;
        }
        return aggregatedStatistics.getCount();
    }

    public void swapData(AggregatedStatistics aggregatedStatistics) {
        this.aggregatedStatistics = aggregatedStatistics;
        this.notifyDataSetChanged();
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < aggregatedStatistics.getCount(); i++) {
            categories.add(aggregatedStatistics.getItem(i).getActivityTypeLocalized());
        }
        return categories;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private final AggregatedStatsListItemBinding viewBinding;
        private UnitSystem unitSystem = UnitSystem.defaultUnitSystem();
        private boolean reportSpeed;

        public ViewHolder(AggregatedStatsListItemBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }



        public void setSpeedOrPace(AggregatedStatistics.AggregatedStatistic aggregatedStatistic, boolean isPace) {
            setCommonValues(aggregatedStatistic);

            SpeedFormatter formatter = SpeedFormatter.Builder().setUnit(unitSystem).setReportSpeedOrPace(reportSpeed).build(context);

            int avgLabelResId = isPace ? R.string.stats_average_moving_pace : R.string.stats_average_moving_speed;
            int maxLabelResId = isPace ? R.string.stats_fastest_pace : R.string.stats_max_speed;

            Pair<String, String> avgParts = formatter.getSpeedParts(aggregatedStatistic.getTrackStatistics().getAverageMovingSpeed());
            viewBinding.aggregatedStatsAvgRate.setText(avgParts.first);
            viewBinding.aggregatedStatsAvgRateUnit.setText(avgParts.second);
            viewBinding.aggregatedStatsAvgRateLabel.setText(context.getString(avgLabelResId));

            Pair<String, String> maxParts = formatter.getSpeedParts(aggregatedStatistic.getTrackStatistics().getMaxSpeed());
            viewBinding.aggregatedStatsMaxRate.setText(maxParts.first);
            viewBinding.aggregatedStatsMaxRateUnit.setText(maxParts.second);
            viewBinding.aggregatedStatsMaxRateLabel.setText(context.getString(maxLabelResId));
        }


        //TODO Check preference handling.
        private void setCommonValues(AggregatedStatistics.AggregatedStatistic aggregatedStatistic) {
            String activityType = aggregatedStatistic.getActivityTypeLocalized();

            reportSpeed = PreferencesUtils.isReportSpeed(activityType);
            unitSystem = PreferencesUtils.getUnitSystem();

            viewBinding.activityIcon.setImageResource(getIcon(aggregatedStatistic));
            viewBinding.aggregatedStatsTypeLabel.setText(activityType);
            viewBinding.aggregatedStatsNumTracks.setText(StringUtils.valueInParentheses(String.valueOf(aggregatedStatistic.getCountTracks())));

            Pair<String, String> parts = DistanceFormatter.Builder()
                    .setUnit(unitSystem)
                    .build(context).getDistanceParts(aggregatedStatistic.getTrackStatistics().getTotalDistance());
            viewBinding.aggregatedStatsDistance.setText(parts.first);
            viewBinding.aggregatedStatsDistanceUnit.setText(parts.second);

            viewBinding.aggregatedStatsTime.setText(StringUtils.formatElapsedTime(aggregatedStatistic.getTrackStatistics().getMovingTime()));
        }

        private int getIcon(AggregatedStatistics.AggregatedStatistic aggregatedStatistic) {
            String localizedActivityType = aggregatedStatistic.getActivityTypeLocalized();
            return ActivityType.findByLocalizedString(context, localizedActivityType)
                    .getIconDrawableId();
        }
    }
}
