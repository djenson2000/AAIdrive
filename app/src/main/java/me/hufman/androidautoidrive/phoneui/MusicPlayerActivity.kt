package me.hufman.androidautoidrive.phoneui

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import me.hufman.androidautoidrive.R
import me.hufman.androidautoidrive.databinding.MusicPlayerBinding
import me.hufman.androidautoidrive.music.MusicAppDiscovery
import me.hufman.androidautoidrive.music.MusicAppInfo
import me.hufman.androidautoidrive.phoneui.controllers.MusicPlayerController
import me.hufman.androidautoidrive.phoneui.fragments.*
import me.hufman.androidautoidrive.phoneui.viewmodels.MusicActivityIconsModel
import me.hufman.androidautoidrive.phoneui.viewmodels.MusicActivityModel
import me.hufman.androidautoidrive.phoneui.viewmodels.viewModels

class MusicPlayerActivity: AppCompatActivity() {

	companion object {
		const val TAG = "MusicPlayerActivity"
	}

	// the viewmodels used by the fragments
	val musicActivityModel by viewModels<MusicActivityModel> { MusicActivityModel.Factory(applicationContext, UIState.selectedMusicApp!!) }
	val musicActivityIconsModel by viewModels<MusicActivityIconsModel> { MusicActivityIconsModel.Factory(this) }
	val musicPlayerController by lazy { MusicPlayerController(null, musicActivityModel.musicController) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val musicApp = UIState.selectedMusicApp ?: return

		discoverApp(musicApp)

		val binding = MusicPlayerBinding.inflate(layoutInflater)
		binding.lifecycleOwner = this
		binding.viewModel = musicActivityModel
		binding.iconsModel = musicActivityIconsModel    // need to touch this so that it's ready for the fragments, even though we don't use it here
		setContentView(binding.root)

		val pgrMusicPlayer = findViewById<ViewPager>(R.id.pgrMusicPlayer)
		musicPlayerController.viewPager = pgrMusicPlayer

		// set up the paging
		val adapter = MusicPlayerPagerAdapter(supportFragmentManager)
		pgrMusicPlayer.adapter = adapter
		pgrMusicPlayer.offscreenPageLimit = 2
		findViewById<TabLayout>(R.id.tabMusicPlayer).setupWithViewPager(pgrMusicPlayer)
	}

	fun discoverApp(musicAppInfo: MusicAppInfo) {
		val musicAppDiscovery = MusicAppDiscovery(this, Handler())
		musicAppDiscovery.loadInstalledMusicApps()
		musicAppDiscovery.probeApp(musicAppInfo)
	}

	override fun onBackPressed() {
		val pager = findViewById<ViewPager>(R.id.pgrMusicPlayer)
		val currentItem = pager?.currentItem ?: 0
		if (currentItem == 0) {
			// pass through default behavior, to close the Activity
			super.onBackPressed()
			return
		}
		if (currentItem == 1) {
			val container = (pager.adapter as MusicPlayerPagerAdapter).getItem(1) as MusicBrowseFragment
			val popped = container.onBackPressed()
			if (!popped) {
				musicPlayerController.showNowPlaying()
			}
		}
		if (currentItem == 2) {
			// go back to the main playback page
			musicPlayerController.showNowPlaying()
		}
		if (currentItem == 3) {
			// go back to the main playback page
			musicPlayerController.showNowPlaying()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		UIState.selectedMusicApp = null
		musicPlayerController.viewPager = null
	}
}

class MusicPlayerPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
	val tabs = LinkedHashMap<String, Fragment>(4).apply {
		this["Now Playing"] = MusicNowPlayingFragment()
		this["Browse"] = MusicBrowseFragment.newInstance(MusicBrowsePageFragment.newInstance(null))
		this["Queue"] = MusicQueueFragment()
		this["Search"] = MusicSearchFragment()
	}

	override fun getCount(): Int {
		return tabs.size
	}

	override fun getPageTitle(position: Int): CharSequence {
		return tabs.keys.elementAt(position)
	}

	override fun getItem(index: Int): Fragment {
		return tabs.values.elementAt(index)
	}
}
