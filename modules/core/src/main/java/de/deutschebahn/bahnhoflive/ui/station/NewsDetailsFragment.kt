package de.deutschebahn.bahnhoflive.ui.station

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.IssueTracker
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.UncriticalIssueException
import de.deutschebahn.bahnhoflive.ui.station.news.groupIcon
import de.deutschebahn.bahnhoflive.util.startSafely
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_news_details.view.*

class NewsDetailsFragment : FullBottomSheetDialogFragment() {

    val stationViewModel by activityViewModels<StationViewModel>()

    val newsEntry by lazy {
        requireArguments().getInt(ARG_NEWS_INDEX).let { newsIndex ->
            Transformations.map(stationViewModel.newsLiveData) { newsList ->
                newsList?.getOrNull(newsIndex)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_news_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsEntry.observe(viewLifecycleOwner, Observer { news ->
            view.headline.text = news?.title

            view.copy.text = news?.content

            view.btnExternalLink?.apply {
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
                            if (linkUri.takeIf { linkUri.scheme == null }?.buildUpon()
                                    ?.scheme("http")?.build().let { fixedUri ->
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            fixedUri
                                        ).startSafely(context)
                                    }
                            ) {
                                issueTracker.dispatchThrowable(UncriticalIssueException("News link url was lacking scheme"))
                            } else {
                                issueTracker.dispatchThrowable(UncriticalIssueException("Could not handle news link url"))
                            }
                        }

                    }
                    setText(news.groupIcon()?.linkButtonText ?: R.string.button_news_external_link)
                    visibility = View.VISIBLE
                } ?: kotlin.run {
                    visibility = View.GONE
                }
            }
        })

        view.btnClose.setOnClickListener { dismiss() }
    }

    companion object {
        fun create(newsIndex: Int) = NewsDetailsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_NEWS_INDEX, newsIndex)
            }
        }

        const val ARG_NEWS_INDEX = "newsIndex"
    }
}
