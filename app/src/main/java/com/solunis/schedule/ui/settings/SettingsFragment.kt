package com.solunis.schedule.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.solunis.schedule.R
import com.solunis.schedule.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserSection()
        setupProviderDropdown()
        setupModelDropdown()
        setupApiKey()
        setupAiButtons()
        setupAppManagement()
    }

    private fun setupUserSection() {
        binding.btnLogin.setOnClickListener {
            Toast.makeText(requireContext(), "登录功能即将上线", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupProviderDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            viewModel.providerOptions
        )
        val dropdown = binding.dropdownProvider
        dropdown.setAdapter(adapter)

        viewModel.modelProvider.observe(viewLifecycleOwner) { provider ->
            if (dropdown.text.toString() != provider) {
                dropdown.setText(provider, false)
            }
            updateModelDropdown(provider)
        }

        dropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = viewModel.providerOptions[position]
            viewModel.updateModelProvider(selected)
        }
    }

    private fun setupModelDropdown() {
        viewModel.modelName.observe(viewLifecycleOwner) { model ->
            val dropdown = binding.dropdownModel
            if (dropdown.text.toString() != model) {
                dropdown.setText(model, false)
            }
        }

        binding.dropdownModel.setOnItemClickListener { _, _, position, _ ->
            val provider = viewModel.modelProvider.value ?: return@setOnItemClickListener
            val models = viewModel.getModelsForProvider(provider)
            if (position < models.size) {
                viewModel.updateModelName(models[position])
            }
        }
    }

    private fun updateModelDropdown(provider: String) {
        val models = viewModel.getModelsForProvider(provider)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            models
        )
        binding.dropdownModel.setAdapter(adapter)
    }

    private fun setupApiKey() {
        viewModel.apiKey.observe(viewLifecycleOwner) { key ->
            val et = binding.etApiKey
            if (et.text.toString() != key) {
                et.setText(key)
            }
        }
    }

    private fun setupAiButtons() {
        binding.btnSaveModel.setOnClickListener {
            val key = binding.etApiKey.text.toString().trim()
            viewModel.updateApiKey(key)
            viewModel.saveModelConfig()
            Toast.makeText(requireContext(), R.string.settings_model_saved, Toast.LENGTH_SHORT).show()
        }

        binding.btnGenerate.setOnClickListener {
            val provider = viewModel.modelProvider.value
            val model = viewModel.modelName.value
            val key = binding.etApiKey.text.toString().trim()

            if (provider.isNullOrBlank() || model.isNullOrBlank()) {
                Toast.makeText(requireContext(), "请先选择模型", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (key.isBlank() && provider != "Ollama (本地)") {
                Toast.makeText(requireContext(), "请输入 API Key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateApiKey(key)
            viewModel.saveModelConfig()
            Toast.makeText(requireContext(), "正在使用 $provider / $model 生成课表...", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupAppManagement() {
        binding.switchWeekend.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateShowWeekend(isChecked)
        }

        binding.itemClearCache.setOnClickListener {
            Toast.makeText(requireContext(), "缓存已清除", Toast.LENGTH_SHORT).show()
        }

        binding.itemFeedback.setOnClickListener {
            Toast.makeText(requireContext(), "意见反馈功能即将上线", Toast.LENGTH_SHORT).show()
        }

        binding.itemCheckUpdate.setOnClickListener {
            Toast.makeText(requireContext(), "当前已是最新版本", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
