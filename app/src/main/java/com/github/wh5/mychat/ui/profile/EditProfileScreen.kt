package com.github.wh5.mychat.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.wh5.mychat.viewmodel.ProfileViewModel
import com.github.wh5.mychat.viewmodel.UserProfile

@Composable
fun EditProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(context) as T
        }
    })

    val uniqueId by viewModel.uniqueIdFlow.collectAsState(initial = "")
    val userProfile by viewModel.userProfile.collectAsState()

    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(0) }
    var birthday by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            nickname = it.nickname
            gender = it.gender
            birthday = it.birthday
            location = it.location
            bio = it.bio
        }
    }

    val transparentTextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("编辑资料", style = MaterialTheme.typography.headlineMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("性别：")
                GenderSelector(gender) { gender = it }
            }

            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                label = { Text("生日 (YYYY-MM-DD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("地区（国家/城市）") },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("个性签名") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Button(
                    onClick = {
                        if (uniqueId.isNotBlank()) {
                            val updated = UserProfile(
                                nickname = nickname,
                                bio = bio,
                                gender = gender,
                                birthday = birthday,
                                location = location,
                            )
                            try {
                                viewModel.updateProfile(uniqueId, updated)
                                navController.popBackStack()
                            } catch (e: Exception) {
                                errorMessage = "保存失败: ${e.message}"
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "保存")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存")
                }
            }
        }
    }

    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage!!)
            errorMessage = null
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun GenderSelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RadioButton(selected = selected == 0, onClick = { onSelect(0) })
        Text("未知")
        RadioButton(selected = selected == 1, onClick = { onSelect(1) })
        Text("男")
        RadioButton(selected = selected == 2, onClick = { onSelect(2) })
        Text("女")
    }
}