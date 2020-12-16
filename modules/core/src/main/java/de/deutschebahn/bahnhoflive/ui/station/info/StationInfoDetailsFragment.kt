/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.IconMapper
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.ui.FragmentArgs
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.MapIntent
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.ServiceContents
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.PhoneIntent
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import kotlinx.android.synthetic.main.card_expandable_station_info.view.*
import kotlinx.android.synthetic.main.include_description_link_part.view.*
import java.util.*
import java.util.regex.Pattern

class StationInfoDetailsFragment :
    RecyclerFragment<StationInfoDetailsFragment.StationInfoAdapter>(R.layout.fragment_recycler_linear),
    MapPresetProvider {

    val stationViewModel: StationViewModel by activityViewModels()

    val dbActionButtonParser = DbActionButtonParser()

    lateinit var serviceContents: List<ServiceContent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceContents = arguments?.getParcelableArrayList(ARG_SERVICE_CONTENTS) ?: emptyList()
        setTitle(arguments?.getCharSequence(FragmentArgs.TITLE))

        stationViewModel.stationFeatures.observe(this) {
            // do nothing, just keep contents updated
        }

        adapter = StationInfoAdapter(
            serviceContents,
            TrackingManager.fromActivity(activity),
            dbActionButtonParser
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stationViewModel.selectedServiceContentType.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { serviceContentType ->
                if (serviceContentType != null) {
                    val serviceContentIndex = serviceContents.indexOfFirst { serviceContent ->
                        serviceContent.type == serviceContentType
                    }
                    if (serviceContentIndex < 0) {
                        HistoryFragment.parentOf(this).pop()
                    } else {
                        stationViewModel.selectedServiceContentType.value = null

                        adapter?.singleSelectionManager?.selection = serviceContentIndex
                    }
                }
            })

        stationViewModel.railwayMissionPoiLiveData.observe(viewLifecycleOwner) {
            // do nothing, just observe
        }
    }

    override fun onStart() {
        super.onStart()

        TrackingManager.fromActivity(activity).track(
            TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.tagFromArguments(
                arguments
            )
        )
    }

    class StationInfoAdapter(
        private val serviceContents: List<ServiceContent>,
        val trackingManager: TrackingManager,
        val dbActionButtonParser: DbActionButtonParser
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<SelectableItemViewHolder<ServiceContent>>() {
        val singleSelectionManager: SingleSelectionManager = SingleSelectionManager(this)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): SelectableItemViewHolder<ServiceContent> {
            return ServiceContentViewHolder(
                parent, singleSelectionManager, trackingManager, dbActionButtonParser
            )
        }

        override fun onBindViewHolder(
            holder: SelectableItemViewHolder<ServiceContent>,
            position: Int
        ) {
            holder.bind(serviceContents[position])
        }

        override fun getItemCount(): Int {
            return serviceContents.size
        }

        val selectedItem get() = singleSelectionManager.getSelectedItem(serviceContents)
    }

    class ServiceContentViewHolder(
        parent: ViewGroup,
        singleSelectionManager: SingleSelectionManager,
        val trackingManager: TrackingManager,
        val dbActionButtonParser: DbActionButtonParser
    ) : CommonDetailsCardViewHolder<ServiceContent>(
        parent,
        R.layout.card_expandable_station_info,
        singleSelectionManager
    ), View.OnClickListener {
        private val dbactionbuttonPattern =
            Pattern.compile("(.*)<dbactionbutton>(.*)</dbactionbutton>(.*)")

        protected val threeButtonsViewHolder: ThreeButtonsViewHolder = ThreeButtonsViewHolder(
            itemView,
            R.id.buttons_container,
            this
        )
        private val descriptionLayout: LinearLayout = itemView.findViewById(R.id.description_layout)
        private val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        private val onPhoneClickListener = View.OnClickListener { view ->
            if (view is TextView) {
                val text = view.text
                view.getContext().startActivity(PhoneIntent(text.toString()))
            }
        }
        private val contentHeaderImage = itemView.contentHeaderImage


        override fun onBind(item: ServiceContent) {
            super.onBind(item)

            threeButtonsViewHolder.reset()

            titleView.text = item.title
            iconView.setImageResource(IconMapper.contentIconForType(item))

            descriptionLayout.removeAllViews()

            contentHeaderImage.visibility = View.GONE

            when (item.type.toLowerCase()) {
                ServiceContent.Type.THREE_S -> {
                    val threeSComponents = ServiceContents.ThreeSComponents(item)

                    addTextPart(threeSComponents.description)

                    if (threeSComponents.phoneNumber != null) {
                        addPhonePart(threeSComponents.phoneNumber, item.title)
                    }
                }

                ServiceContent.Type.Local.LOST_AND_FOUND -> {
                    run {
                        FundserviceContentElement.render(
                            descriptionLayout,
                            item,
                            itemView.context,
                            null
                        )
                    }
                    run {
                        val descriptionText = item.descriptionText
                        val matcher = Patterns.PHONE.matcher(descriptionText)
                        var cursor = 0
                        while (matcher.find()) {
                            val start = matcher.start()
                            addHtmlPart(descriptionText.substring(cursor, start))
                            cursor = matcher.end()
                            addPhonePart(descriptionText.substring(start, cursor), item.title)
                        }
                        if (cursor < descriptionText.length) {
                            addHtmlPart(descriptionText.substring(cursor, descriptionText.length))
                        }

                        val additionalText = item.additionalText
                        if (!TextUtils.isEmpty(additionalText)) {
                            addHtmlPart(additionalText)
                        }
                    }
                }

                ServiceContent.Type.Local.TRAVEL_CENTER -> {
                    if (item.address == null) {
                        defaultRender(item)
                    } else {
                        val linkView = layoutInflater.inflate(
                            R.layout.include_description_link_part,
                            descriptionLayout,
                            false
                        )
                        linkView.text.text =
                            "<b>Nächstes Reisezentrum</b><br/>${item.address.toString()}".spannedHtml()
                        item.location?.also { location ->
                            linkView.linkButton.setOnClickListener {
                                layoutInflater.context.startActivity(
                                    MapIntent(
                                        location, item.address.toString().replace(
                                            "<br/>",
                                            ","
                                        )
                                    )
                                )
                            }
                        }
                        descriptionLayout.addView(linkView)
                        renderAdditionalText(item)
                        renderDescriptionText(item)
                    }
                }

                ServiceContent.Type.Local.CHATBOT -> {
                    addImagePart(R.drawable.padded_chatbot)
                    dbactionbuttonPattern.matcher(item.descriptionText).takeIf { it.matches() }
                        ?.run {
                            group(1).takeUnless { it.isBlank() }?.also {
                                addHtmlPart(it)
                            }

                            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 7 until 22)
                                addButtonPart(
                                    group(2),
                                    itemView.resources.getString(R.string.sr_chatbot),
                                    View.OnClickListener {
                                        trackingManager.track(
                                            TrackingManager.TYPE_ACTION,
                                            TrackingManager.Screen.D1,
                                            TrackingManager.Action.TAP,
                                            TrackingManager.UiElement.CHATBOT
                                        )
                                        it.context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://bahnhof-bot.deutschebahn.com/")
                                            )
                                        )
                                    })

                            group(3).takeUnless { it.isBlank() }?.also {
                                addHtmlPart(it)
                            }

                        } ?: addHtmlPart(item.descriptionText)
                }

                else -> {
                    defaultRender(item)
                }
            }
        }

        private fun addImagePart(imageResource: Int, backgroundColor: Int? = null) {
            contentHeaderImage.setImageResource(imageResource)
            backgroundColor?.let { color ->
                contentHeaderImage.setBackgroundColor(contentHeaderImage.resources.getColor(color))
            }
            contentHeaderImage.visibility = View.VISIBLE
        }

        private fun defaultRender(item: ServiceContent) {
            renderDescriptionText(item)
            renderAdditionalText(item)
        }

        private fun renderAdditionalText(item: ServiceContent) {
            item.additionalText
                ?.takeIf { it.isNotBlank() }
                ?.also {
                    addHtmlPart(it)
                }
        }

        private fun renderDescriptionText(item: ServiceContent) {
            val parts = dbActionButtonParser.parse(item.descriptionText)

            parts.forEach {
                it.button?.also { dbActionButton ->
                    dbActionButton.label?.also { label ->
                        addButtonPart(label, label) {
                            dbActionButton.href?.let { url ->
                                try {
                                    it.context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(url)
                                        )
                                    )
                                } catch (e: Exception) {
                                    BaseApplication.get().issueTracker.dispatchThrowable(e)
                                }
                            }
                        }
                    }
                } ?: it.text?.also { descriptionText ->
                    val matcher = Patterns.PHONE.matcher(descriptionText)
                    var cursor = 0
                    while (matcher.find()) {
                        val start = matcher.start()
                        addHtmlPart(descriptionText.substring(cursor, start))
                        cursor = matcher.end()
                        addPhonePart(descriptionText.substring(start, cursor), item.title)
                    }
                    if (cursor < descriptionText.length) {
                        addHtmlPart(descriptionText.substring(cursor, descriptionText.length))
                    }
                }
            }
        }

        private fun addHtmlPart(htmlString: String) {
            addTextPart(htmlString.spannedHtml())
        }

        private fun String.spannedHtml() = Html.fromHtml(this)

        private fun addPhonePart(text: String, serviceName: String) {
            addButtonPart(
                text, itemView.resources.getString(
                    R.string.sr_template_phone_button,
                    serviceName
                ), onPhoneClickListener
            )
        }

        private fun addButtonPart(
            text: String,
            contentDescription: String,
            onClickListener: View.OnClickListener
        ) {
            val textView = addPartView(R.layout.include_description_button_part)
            textView.setOnClickListener(onClickListener)
            textView.text = text
            textView.contentDescription = contentDescription
        }

        private fun addTextPart(description: CharSequence) {
            val partTextView = addPartView(R.layout.include_description_text_part)
            partTextView.movementMethod = LinkMovementMethod.getInstance()
            partTextView.text = description
            partTextView.contentDescription = description.toString().replace(
                "-24:00",
                " bis 24 Uhr"
            )
                .replace("◾", "/")
        }

        private fun addPartView(@LayoutRes layout: Int): TextView {
            val partTextView = layoutInflater.inflate(layout, descriptionLayout, false) as TextView
            descriptionLayout.addView(partTextView)
            return partTextView
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.button_right -> {
                    val item = item
                    if (ServiceContent.Type.THREE_S == item?.type?.toLowerCase(Locale.GERMAN)) {
                        val threeSComponents = ServiceContents.ThreeSComponents(item)

                        if (threeSComponents.phoneNumber != null) {
                            val intent = PhoneIntent(threeSComponents.phoneNumber)
                            v.context.startActivity(intent)
                        }
                    }
                }
            }
        }

    }

    companion object {

        val ARG_SERVICE_CONTENTS = "serviceContents"

        fun create(
            serviceContents: ArrayList<ServiceContent>,
            title: CharSequence,
            trackingTag: String
        ): StationInfoDetailsFragment {
            val fragment = StationInfoDetailsFragment()

            val args = Bundle()
            args.putParcelableArrayList(ARG_SERVICE_CONTENTS, serviceContents)
            args.putCharSequence(FragmentArgs.TITLE, title)

            TrackingManager.putTrackingTag(args, trackingTag)

            fragment.arguments = args

            return fragment
        }
    }

    override fun prepareMapIntent(intent: Intent?) = intent?.let {
        adapter?.selectedItem?.let { serviceContent ->
            stationViewModel.stationFeatures.value?.firstOrNull { stationFeature ->
                stationFeature.stationFeatureTemplate.definition.serviceContentType == serviceContent.type
            }?.let { stationFeature ->

                stationFeature.stationFeatureTemplate.definition.venueFeature?.let {
                    RimapFilter.putPreset(intent, it.mapPreset)
                }

                stationFeature.venues?.firstOrNull()?.rimapPOI?.let {
                    InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, it)
                }

                true
            } ?: serviceContent.takeIf {
                it.type == ServiceContent.Type.BAHNHOFSMISSION
            }?.let {
                RimapFilter.putPreset(intent, RimapFilter.PRESET_INFO_ON_SITE)

                stationViewModel.railwayMissionPoiLiveData.value?.let {
                    InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, it)
                }
            }
        }
    } != null

}
