package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val timeFormat = SimpleDateFormat("HH: mm: ss", Locale.getDefault())
    private var isTimePlayed = false
        set(value) {
            field = value
            binding.startStopBtn.text =
                if (value) getString(R.string.stop) else getString(R.string.start)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clockWidget.addUpdateListener {
            isTimePlayed = it.isPlayed
            binding.timeDigits.text = timeFormat.format(it.time)
        }
        binding.startStopBtn.setOnClickListener {
            if (isTimePlayed)
                binding.clockWidget.stop()
            else
                binding.clockWidget.start()
        }
        binding.resetBtn.setOnClickListener {
            binding.clockWidget.reset()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}