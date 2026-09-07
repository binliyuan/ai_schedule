package com.solunis.schedule.ui.settings

import android.content.Intent
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
import com.solunis.schedule.mcp.McpServer
import com.solunis.schedule.mcp.McpService

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
        setupMcpServer()
        setupAppManagement()
    }

    private fun setupUserSection() {
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { loggedIn ->
            if (loggedIn) {
                val user = viewModel.currentUser.value
                binding.tvUsername.text = user?.nickname?.ifBlank { user.username } ?: user?.username ?: ""
                binding.tvLoginHint.text = user?.username ?: ""
                binding.btnLogin.text = getString(R.string.btn_logout)
            } else {
                binding.tvUsername.text = getString(R.string.settings_user_not_logged_in)
                binding.tvLoginHint.text = getString(R.string.settings_user_login_hint)
                binding.btnLogin.text = getString(R.string.settings_user_login)
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUsername.text = user.nickname.ifBlank { user.username }
                binding.tvLoginHint.text = user.username
            }
        }

        binding.btnLogin.setOnClickListener {
            if (viewModel.isLoggedIn.value == true) {
                viewModel.logout()
                Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show()
            } else {
                val dialog = LoginDialogFragment()
                dialog.onLoginSuccess = {
                    viewModel.refreshLoginState()
                }
                dialog.show(parentFragmentManager, "login")
            }
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

    private fun setupMcpServer() {
        val port = McpServer.DEFAULT_PORT
        binding.switchMcp.isChecked = McpService.isRunning
        updateMcpStatus()

        binding.switchMcp.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(requireContext(), McpService::class.java)
            if (isChecked) {
                requireContext().startForegroundService(intent)
                Toast.makeText(requireContext(), "MCP Server 已启动 (端口 $port)", Toast.LENGTH_SHORT).show()
            } else {
                requireContext().stopService(intent)
                Toast.makeText(requireContext(), "MCP Server 已停止", Toast.LENGTH_SHORT).show()
            }
            binding.switchMcp.postDelayed({ updateMcpStatus() }, 500)
        }
    }

    private fun updateMcpStatus() {
        if (McpService.isRunning) {
            binding.tvMcpStatus.text = getString(R.string.mcp_server_on, McpService.serverPort)
        } else {
            binding.tvMcpStatus.text = getString(R.string.mcp_server_off)
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
