/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.IssueTracker
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.UncriticalIssueException
import de.deutschebahn.bahnhoflive.databinding.FragmentNewsDetailsBinding
import de.deutschebahn.bahnhoflive.ui.station.news.groupIcon
import de.deutschebahn.bahnhoflive.util.startSafely
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment

class NewsDetailsFragment : FullBottomSheetDialogFragment() {

    val stationViewModel by activityViewModels<StationViewModel>()

    private val newsEntry by lazy {
        requireArguments().getInt(ARG_NEWS_INDEX).let { newsIndex ->
            stationViewModel.newsLiveData.map { newsList ->
                newsList?.getOrNull(newsIndex)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        FragmentNewsDetailsBinding.inflate(inflater, container, false).apply {

            newsEntry.observe(viewLifecycleOwner, Observer { news ->
                headline.text = news?.title

                with(subtitle) {
                    val subtitle = news?.subtitle?.takeUnless { it.isBlank() }
                    text = subtitle
                    visibility = if (subtitle == null) View.GONE else View.VISIBLE
                }

                copy.text = news?.content

                image.visibility = if (news?.decodedImage?.let { imageByteArray ->
                        try {
                            image.setImageBitmap(
                                BitmapFactory.decodeByteArray(
                                    imageByteArray,
                                    0,
                                    imageByteArray.size
                                )
                            )
                            true
                        } catch (e: Exception) {
                            false
                        }
                    } == true) View.VISIBLE else View.GONE

                btnExternalLink.apply {
                    news?.linkUri?.also { linkUri ->
                        setOnClickListener { _ ->
                            TrackingManager.fromActivity(activity).run {
                                track(
                                    TrackingManager.TYPE_ACTION,
                                    TrackingManager.Screen.H1,
                                    TrackingManager.Action.TAP,
                                    TrackingManager.Entity.NEWS_BOX,
                                    TrackingManager.Entity.LINK
                                )
                                news.group.id.let { groupId ->
                                    track(
                                        TrackingManager.TYPE_ACTION,
                                        TrackingManager.Screen.H1,
                                        TrackingManager.Action.TAP,
                                        TrackingManager.Entity.NEWS_TYPE,
                                        groupId.toString(),
                                        TrackingManager.Entity.LINK
                                    )
                                }
                        }

                        if (!Intent(Intent.ACTION_VIEW, linkUri).startSafely(context)) {
                            val issueTracker = IssueTracker.instance
                            issueTracker.log("Could not handle original news link url $linkUri")
                            if (linkUri.scheme == null) {
                                issueTracker.dispatchThrowable(UncriticalIssueException("News link url was lacking scheme, trying to prepend http://"))

                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://$linkUri")
                                ).startSafely(context)
                            } else {
                                issueTracker.dispatchThrowable(UncriticalIssueException("Could not handle news link url"))
                            }
                        }

                        }
                        setText(
                            news.groupIcon()?.linkButtonText ?: R.string.button_news_external_link
                        )
                        visibility = View.VISIBLE
                    } ?: kotlin.run {
                        visibility = View.GONE
                    }
                }
            })

            btnClose.setOnClickListener { dismiss() }
        }.root

    companion object {
        fun create(newsIndex: Int) = NewsDetailsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_NEWS_INDEX, newsIndex)
            }
        }

        const val ARG_NEWS_INDEX = "newsIndex"
    }
}
