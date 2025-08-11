package com.skye.hrms.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.OnboardingFormData
import com.skye.hrms.data.viewmodels.OnboardingViewModel
import com.skye.hrms.data.viewmodels.SubmissionState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OnBoardingScreen(
    onboardingViewModel: OnboardingViewModel = viewModel(),
    onFormSubmitted: () -> Unit
) {
    val currentStep by onboardingViewModel.currentStep.collectAsState()
    val formData by onboardingViewModel.formData.collectAsState()
    val submissionState: SubmissionState by onboardingViewModel.submissionState.collectAsState()

    when (val state = submissionState) {
        is SubmissionState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SubmissionState.Success -> {
            LaunchedEffect(Unit) {
                onFormSubmitted()
            }
        }
        is SubmissionState.Error -> {
            val context = LocalContext.current
            LaunchedEffect(state) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
        }
        else -> { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .offset(x = (-100).dp, y = (-150).dp)
                    .blur(100.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(400.dp)
                    .offset(x = (120).dp, y = (100).dp)
                    .blur(100.dp)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f), shape = CircleShape)
            )
        }
    }

    Scaffold(
        topBar = {
            ProgressStepper(currentStep = currentStep, totalSteps = 5)
        },
        bottomBar = {
            FormNavigationBar(
                currentStep = currentStep,
                onPrevious = onboardingViewModel::onPreviousStep,
                onNext = onboardingViewModel::onNextStep,
                onSubmit = onFormSubmitted
            )
        },
        containerColor = Color.Transparent
    ) {
            paddingValues ->
        AnimatedContent(
            targetState = currentStep,
            modifier = Modifier.padding(paddingValues),
            transitionSpec = {
                slideInHorizontally { width -> width } togetherWith
                        slideOutHorizontally { width -> -width }
            },
            label = "Onboarding Step Animation"
        ) { step ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    when (step) {
                        1 -> PersonalDetailsStep(formData = formData, viewModel = onboardingViewModel)
                        2 -> ContactDetailsStep(formData = formData, viewModel = onboardingViewModel)
                        3 -> EmploymentDetailsStep(formData = formData, viewModel = onboardingViewModel)
                        4 -> AcademicDetailsStep(formData = formData, viewModel = onboardingViewModel)
                        5 -> DependentsDetailsStep(formData = formData, viewModel = onboardingViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressStepper(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (step in 1..totalSteps) {
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            val color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = when {
                isCompleted -> MaterialTheme.colorScheme.onPrimary
                isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check, contentDescription = "Completed", tint = contentColor)
                } else {
                    Text(text = "$step", color = contentColor, fontWeight = FontWeight.Bold)
                }
            }

            if (step < totalSteps) {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FormNavigationBar(
    currentStep: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                    Spacer(Modifier.width(8.dp))
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f)) // Placeholder
            }

            Button(
                onClick = if (currentStep == 5) onSubmit else onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (currentStep == 5) "Submit" else "Next")
            }
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            viewModel.updateDateOfBirth(format.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(modifier = Modifier.padding(24.dp)) {
        StepHeader(title = "Personal Details", subtitle = "Let's start with the basics.")

        OutlinedTextField(
            value = formData.fullName,
            onValueChange = viewModel::updateFullName,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = formData.dateOfBirth,
            onValueChange = { /* Handled by dialog */ },
            label = { Text("Date of Birth") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            shape = RoundedCornerShape(16.dp),
            trailingIcon = { Icon(Icons.Default.CalendarToday, "Date Picker") }
        )
        Spacer(Modifier.height(16.dp))

        var genderExpanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Male", "Female", "Other")
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded },
        ) {
            OutlinedTextField(
                value = formData.gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.updateGender(option)
                            genderExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.padding(24.dp)) {
        StepHeader(title = "Contact Information", subtitle = "How can we reach you?")
        // Add TextFields for addresses and phone numbers
    }
}

@Composable
private fun EmploymentDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.padding(24.dp)) {
        StepHeader(title = "Employment Details", subtitle = "Tell us about your role.")
        // Add TextFields/Dropdowns for Employee ID, Department, etc.
    }
}

@Composable
private fun AcademicDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.padding(24.dp)) {
        StepHeader(title = "Academic Background", subtitle = "Add your qualifications.")

        formData.educationalHistory.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(value = item.degree, onValueChange = { viewModel.updateEducationItem(item.copy(degree = it)) }, label = {Text("Degree/Certificate")}, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = item.university, onValueChange = { viewModel.updateEducationItem(item.copy(university = it)) }, label = {Text("University/Board")}, modifier = Modifier.fillMaxWidth())
                    // ... other fields ...
                    IconButton(onClick = { viewModel.removeEducationItem(item) }, modifier = Modifier.align(Alignment.End)) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Button(onClick = viewModel::addEducationItem, modifier = Modifier.padding(top = 16.dp)) {
            Icon(Icons.Default.Add, "Add")
            Spacer(Modifier.width(8.dp))
            Text("Add Another Qualification")
        }
    }
}


@Composable
private fun DependentsDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.padding(24.dp)) {
        StepHeader(title = "Dependents & Emergency", subtitle = "Who should we contact in an emergency?")
        // Implement this step similar to AcademicDetailsStep
    }
}