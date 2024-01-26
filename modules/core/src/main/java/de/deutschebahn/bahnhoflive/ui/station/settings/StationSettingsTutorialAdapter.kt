package de.deutschebahn.bahnhoflive.ui.station.settings

import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialPreferenceStore
import de.deutschebahn.bahnhoflive.util.inflateLayout
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class StationSettingsTutorialItemViewHolder(
    parent: View,
    selectionManager: SingleSelectionManager?
) : SelectableItemViewHolder<Any?>(
    parent,
    selectionManager
), CompoundButton.OnCheckedChangeListener {

    private val toggleView: CompoundButtonChecker =
        CompoundButtonChecker(itemView.findViewById(R.id.show_tips_switch), this)

    override fun onBind(item: Any?) {
        super.onBind(item)
        val manager = TutorialManager.getInstance(BaseApplication.get())
        toggleView.isChecked = manager.doesUserWantToSeeTutorials()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        TutorialPreferenceStore.getInstance(BaseApplication.get()).setUserWantsTutorials(isChecked)
    }
}

class StationSettingsTutorialAdapter(private val selectionManager: SingleSelectionManager?) :
    RecyclerView.Adapter<StationSettingsTutorialItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StationSettingsTutorialItemViewHolder {
        return StationSettingsTutorialItemViewHolder(parent.inflateLayout(R.layout.card_expandable_setting_tutorial), selectionManager)
    }

    override fun onBindViewHolder(holder: StationSettingsTutorialItemViewHolder, position: Int) {
        holder.bind(null)
    }

    override fun getItemCount(): Int {
        return 1
    }
}