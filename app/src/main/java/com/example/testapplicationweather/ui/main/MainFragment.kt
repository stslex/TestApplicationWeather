package com.example.testapplicationweather.ui.main

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.testapplicationweather.R
import com.example.testapplicationweather.databinding.FragmentMainBinding
import com.example.testapplicationweather.model.MainRepository
import com.example.testapplicationweather.ui.pager.PagerSharedViewModel
import com.example.testapplicationweather.utilites.*
import com.example.testapplicationweather.utilites.Resources.internetConnection
import com.example.testapplicationweather.viewmodel.MainViewModel
import com.example.testapplicationweather.viewmodel.MainViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private val repository = MainRepository()
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    private val sharedViewModel: PagerSharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setIconsAndTitles()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.mainHeadLayoutInclude.headerProgressBar.visibility = View.VISIBLE
        initDataUpdate()
        initPager()
        return binding.root
    }

    private fun initPager() {
        viewPager = binding.mainViewPager
        viewPager.adapter = MainAdapter(this)

        val mapOfCity = mapOf(0 to getString(R.string.msk_name), 1 to getString(R.string.spb_name))

        TabLayoutMediator(binding.mainTabLayout, binding.mainViewPager) { tab, position ->
            tab.text = mapOfCity[position]
            binding.mainViewPager.setCurrentItem(tab.position, true)
        }.attach()

        setChangePagerListener()
    }

    private fun setChangePagerListener() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        internetConnection = isNetworkConnected()
                        mainViewModel.getWeatherFromRetrofit(GET_MSK, true)
                    }
                    1 -> {
                        mainViewModel.getWeatherFromRetrofit(GET_SPB, true)
                        internetConnection = isNetworkConnected()
                    }
                }
            }
        })
    }

    private fun initDataUpdate() {
        mainViewModel.responseData.observe(viewLifecycleOwner) {
            when (it.failure) {
                "" -> {
                    val model = it.success.currently
                    binding.mainHeadLayoutInclude.headerProgressBar.visibility = View.GONE
                    binding.mainHeadLayoutInclude.mainFragmentTemperature.text =
                        model.temperature.convertToCelsius()
                    binding.mainHeadLayoutInclude.mainFragmentWeather.text =
                        getWeatherTitle(model.summary)
                    binding.mainHeadLayoutInclude.mainFragmentIcon.setIcon(model.icon)
                    val listOfDays = it.success.daily
                    sharedViewModel.listOfDays.value = listOfDays
                }
                else -> {
                    Snackbar.make(binding.root, it.failure, Snackbar.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

