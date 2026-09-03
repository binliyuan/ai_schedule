package com.solunis.schedule.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.solunis.schedule.R
import com.solunis.schedule.data.network.RetrofitClient
import com.solunis.schedule.data.repository.UserRepository
import com.solunis.schedule.databinding.DialogLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogLoginBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository(RetrofitClient.apiService)
    private var isLoginMode = true
    var onLoginSuccess: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                isLoginMode = tab.position == 0
                binding.tilNickname.visibility = if (isLoginMode) View.GONE else View.VISIBLE
                binding.btnSubmit.text = if (isLoginMode) getString(R.string.btn_login) else getString(R.string.btn_register)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSubmit.isEnabled = false

            if (isLoginMode) {
                doLogin(username, password)
            } else {
                val nickname = binding.etNickname.text.toString().trim()
                doRegister(username, password, nickname)
            }
        }
    }

    private fun doLogin(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userRepository.login(username, password)
            withContext(Dispatchers.Main) {
                binding.btnSubmit.isEnabled = true
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
                    onLoginSuccess?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), result.exceptionOrNull()?.message ?: "登录失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun doRegister(username: String, password: String, nickname: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userRepository.register(username, password, nickname)
            withContext(Dispatchers.Main) {
                binding.btnSubmit.isEnabled = true
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), R.string.register_success, Toast.LENGTH_SHORT).show()
                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
                } else {
                    Toast.makeText(requireContext(), result.exceptionOrNull()?.message ?: "注册失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
