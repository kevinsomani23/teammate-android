/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.fragments.main

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.gameAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidx.recyclerview.diff.Differentiable

/**
 * Lists [tournaments][Event]
 */

class GamesChildFragment : TeammatesBaseFragment(R.layout.fragment_games_child) {

    private var round: Int = 0
    private lateinit var tournament: Tournament
    private lateinit var items: List<Differentiable>

    override val stableTag: String
        get() {
            val superResult = super.stableTag
            val tempTournament = arguments!!.getParcelable<Tournament>(ARG_TOURNAMENT)
            val round = arguments!!.getInt(ARG_ROUND)

            return if (tempTournament != null) superResult + "-" + tempTournament.hashCode() + "-" + round
            else superResult
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        round = arguments!!.getInt(ARG_ROUND)
        tournament = arguments!!.getParcelable(ARG_TOURNAMENT)!!
        items = gameViewModel.getGamesForRound(tournament, round)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fragment = parentFragment
        val recycledViewPool =
                if (fragment is TournamentDetailFragment) fragment.gamesRecycledViewPool
                else RecyclerView.RecycledViewPool()

        scrollManager = ScrollManager.with<RecyclerView.ViewHolder>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_trophy_white_24dp, R.string.no_tournaments))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.onRefresh() }
                .withEndlessScroll { fetchTournaments(false) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(gameAdapter(::items, this::onGameClicked))
                .withRecycledViewPool(recycledViewPool)
                .withLinearLayoutManager()
                .build()

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint)
    }

    private fun onRefresh() {
        disposables.add(gameViewModel.fetchGamesInRound(tournament, round).subscribe(this::onGamesUpdated, defaultErrorHandler::invoke))
    }

    override fun onResume() {
        super.onResume()
        fetchTournaments(true)
    }

    override fun togglePersistentUi() = Unit /* Do nothing */

    private fun onGameClicked(game: Game) {
        navigator.push(GameFragment.newInstance(game))
    }

    private fun fetchTournaments(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        onRefresh()
    }

    private fun onGamesUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    companion object {

        private const val ARG_TOURNAMENT = "tournament"
        private const val ARG_ROUND = "round"

        fun newInstance(tournament: Tournament, round: Int): GamesChildFragment {
            val fragment = GamesChildFragment()
            val args = Bundle()

            args.putParcelable(ARG_TOURNAMENT, tournament)
            args.putInt(ARG_ROUND, round)
            fragment.arguments = args
            return fragment
        }
    }
}
