package com.mycollege.schedule.feature.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.feature.onboarding.ui.state.OnboardingViewModel
import com.mycollege.schedule.feature.schedule.ui.components.utils.Loader

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Scaffold(
        containerColor = background,
        bottomBar = {
            Button(
                onClick = {
                    viewModel.setFirstStartup()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(70.dp, 30.dp, 70.dp, 80.dp)
                    .size(0.dp, 65.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttons),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(text = "Закрыть", fontSize = 17.sp, color = Color.White)
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner)
        ) {
            Spacer(modifier = Modifier.height(150.dp))
            Loader(resource = R.raw.focus, height = 300.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = LocalContext.current.getString(R.string.onboarding),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

